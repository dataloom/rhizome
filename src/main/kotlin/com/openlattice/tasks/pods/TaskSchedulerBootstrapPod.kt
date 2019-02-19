/*
 * Copyright (C) 2019. OpenLattice, Inc.
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

package com.openlattice.tasks.pods

import com.openlattice.tasks.HazelcastFixedRateTask
import com.openlattice.tasks.HazelcastTaskDependencies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */

internal const val NO_OP_TASK_NAME = "_rhizome:no-op"

@Configuration
internal class TaskSchedulerBootstrapPod {
    @Bean
    fun getNoOpFixedRateTask(): NoOpFixedRateTask {
        return NoOpFixedRateTask()
    }

    class NoOpFixedRateTask : HazelcastFixedRateTask {
        override fun getInitialDelay(): Long {
            return 0
        }

        override fun getPeriod(): Long {
            return 0
        }

        override fun getTimeUnit(): TimeUnit {
            return TimeUnit.MILLISECONDS
        }

        override fun getDependencies(): Class<out HazelcastTaskDependencies> {
            return NoOpFixedRateTaskDependency::class.java
        }

        override fun run() {

        }

        override fun getName(): String {
            return NO_OP_TASK_NAME
        }

    }

    class NoOpFixedRateTaskDependency : HazelcastTaskDependencies {

    }

}