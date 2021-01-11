/*
 * Copyright (C) 2020. OpenLattice, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 *
 */

package com.geekbeast.rhizome.jobs

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import com.hazelcast.map.IMap
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread.interrupted
import java.util.*
import java.util.concurrent.TimeUnit

const val PAUSE_POLLING_MILLIS = 5000L
const val INITIALIZE_TASK_ID_POLLING_MILLIS = 500L

/**
 * This class allows the execution of long running background jobs on the Hazelcast cluster. The main thing to keep in
 * mind when using this as the base class is that the j
 *
 * @param state The state of the job that can be retrieved by any hazelcast client.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */

@SuppressFBWarnings(value = ["NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "SA_FIELD_SELF_ASSIGNMENT"], justification = "Im avoiding the question right now")
abstract class AbstractDistributedJob<R, S : JobState>(
        state: S,
        val maxBatchDuration: Long = 1,
        val maxBatchDurationTimeUnit: TimeUnit = TimeUnit.HOURS
) : DistributableJob<R>, HazelcastInstanceAware {
    override val resumable = false
    private var initialized: Boolean = false

    /**
     * Protected helper function to assist with adding a secondary constructor for jackson serialization.
     */
    @Synchronized
    protected fun initialize(
            id: UUID?,
            taskId: Long?,
            status: JobStatus,
            progress: Byte,
            hasWorkRemaining: Boolean,
            result: R?
    ) {
        require(!initialized) { "You can only initialize a job once." }

        if (id != null) initId(id)
        if (taskId != null) initTaskId(taskId)
        this.hasWorkRemaining = hasWorkRemaining
        this.progress = progress
        this.status = status
        this.initialized = true
        this.result = result
    }

    var state: S = state
        protected set

    var id: UUID? = null
        private set

    var taskId: Long? = null
        private set

    var progress: Byte = 0
        protected set

    var status: JobStatus = JobStatus.PENDING
        internal set

    var hasWorkRemaining: Boolean = true
        protected set

    protected var result: R? = null

    @Transient
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transient
    protected lateinit var hazelcastInstance: HazelcastInstance
        private set

    @Transient
    private lateinit var jobs: IMap<UUID, AbstractDistributedJob<*, *>>

    /**
     * Internal API for doing a one time assignment of the hazelcast generated task id.
     * @param taskId The id that was assigned to this task.
     */
    internal fun initTaskId(taskId: Long) {
        this.taskId = if (this.taskId == null) taskId else throw IllegalStateException("Task id can only be assigned once.")
    }

    /**
     * Internal API for doing a one time assignment of the randomly generated uuid for the task.
     * @param id The key of the job in the jobs maps.
     */
    internal fun initId(id: UUID) {
        this.id = if (this.id == null) id else throw IllegalStateException("Job  id can only be assigned once.")
    }

    /**
     * When overriding this function, make sure to invoke super otherwise your job won't work.
     */
    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance
        this.jobs = hazelcastInstance.getMap(JOBS_MAP)
    }

    /**
     *
     */
    fun abort(msg: () -> String = { "Job $id with $taskId was aborted." }) {
        if (this.status != JobStatus.FINISHED) {
            this.status = JobStatus.CANCELED
        }
        publishJobState()
        logger.error(msg())
    }

    fun pause(msg: () -> String = { "Job $id with $taskId was aborted." }) {
        if (this.status != JobStatus.FINISHED) {
            this.status = JobStatus.PAUSED
        }
        publishJobState()
        logger.info(msg())
    }

    /**
     * This function can be override to specify setup behavior that occurs before task starts running.
     */
    protected open fun initialize() {}

    abstract fun processNextBatch()

    private fun initializeOrResumeJob() {
        /**
         * There are two resumption cases we need to handle at initialization
         *
         * 1. Node failure and task is resumed on another node and task only has original state
         * 2. Cluster restart and task is being resumed and task has last previously published state.
         *
         * In the first case we have to pull state from map to resume the job. In the second case we
         * will already have the state from map and just need to resume the job.
         */

        updateJobStatus()

        if (resumable && status == JobStatus.RUNNING) {
            //Case 1
            val v = jobs.executeOnKey(id!!) { it.value }!!
            this.state = v.state as S
            this.status = v.status
            this.taskId = v.taskId
            this.hasWorkRemaining = v.hasWorkRemaining
            this.progress = progress
            handleResumeFromSavedState()
        } else if (status == JobStatus.PENDING) {
            //If status is pending then job is new and we just need to mark as running.
            status = JobStatus.RUNNING
            initialize()
            logger.info("Task $id is initialized and running!")
        } else {
            logger.debug("Task $id (resumable=$resumable) is currently $status.")
        }
    }

    open fun handleResumeFromSavedState() {

    }

    protected fun updateJobStatus() {
        status = jobs.executeOnKey(id!!) { it.value.status }
    }

    private fun processBatches() {
        while (!interrupted() && hasWorkRemaining && status == JobStatus.RUNNING) {
            processNextBatch()
            if (!hasWorkRemaining) {
                status = JobStatus.FINISHED
            }
            publishJobState()
        }
    }

    final override fun call(): R? {
        //Wait until task id has been assigned and can be retrieved.
        pollForTaskId()

        /**
         * If the job status is paused we poll for a job status update every five seconds, otherwise we process
         * that batches, which publishes its state on every loop. If we get interrupted, we abort the processing
         * and publish the aborted job state.
         */
        try {
            do {
                if (status == JobStatus.PAUSED) {
                    Thread.sleep(PAUSE_POLLING_MILLIS)
                } else {
                    initializeOrResumeJob()
                    processBatches()
                }
            } while (resumable && status == JobStatus.PAUSED)
        } catch (ex: InterruptedException) {
            logger.warn("Distributed job $taskId was interrupted.", ex)
            abort()
        } finally {
            publishJobState()
        }

        return if (hasWorkRemaining && status == JobStatus.CANCELED) {
            logger.warn("Distributed job $id with task id $taskId was interrupted or canceled.")
            null
        } else if (hasWorkRemaining && resumable && status == JobStatus.PAUSED) {
            logger.info("Distributed job $id with task id $taskId was pasused")
            null
        } else if (!hasWorkRemaining && status == JobStatus.FINISHED) {
            result
        } else {
            throw IllegalStateException("Distributed job $id is an illegal state.")
        }
    }

    private fun pollForTaskId() {
        if (taskId != null) return

        require(id != null) { "Cannot initialize task when id has not been initialized." }

        while (taskId == null) {
            logger.info("Attempting to retrieve task id for task $id")
            val maybeTaskId = jobs.executeOnKey(id!!) { it.value.taskId } //TODO: Make offloadable
            if (maybeTaskId == null) Thread.sleep(INITIALIZE_TASK_ID_POLLING_MILLIS) else initTaskId(maybeTaskId)
        }
    }

    /**
     * Overridable function that allows updating progress before job state is published.
     */
    protected open fun updateProgress() {}

    protected fun publishJobState() {
        updateProgress()
        //Do not replace with indexing operator
        jobs.set(id, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractDistributedJob<*, *>) return false

        if (state != other.state) return false
        if (id != other.id) return false
        if (taskId != other.taskId) return false
        if (progress != other.progress) return false
        if (status != other.status) return false
        if (hasWorkRemaining != other.hasWorkRemaining) return false

        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (taskId?.hashCode() ?: 0)
        result = 31 * result + progress
        result = 31 * result + status.hashCode()
        result = 31 * result + hasWorkRemaining.hashCode()
        return result
    }
}
