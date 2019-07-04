package com.geekbeast.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.GroupConfig
import com.hazelcast.config.SerializationConfig
import com.hazelcast.core.HazelcastInstance
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration
import com.kryptnostic.rhizome.pods.hazelcast.BaseHazelcastInstanceConfigurationPod

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class HazelcastClientProvider(
        private val clients: Map<String, HazelcastConfiguration>,
        private val serializationConfig: SerializationConfig
) {
    init {
        check(clients.values.all { !it.isServer }) { "Cannot specify server configuration for clients" }
    }

    private val hazelcastClients = clients.mapValues { (name, clientConfig) ->
        HazelcastClient.newHazelcastClient(
                ClientConfig()
                        .setNetworkConfig(BaseHazelcastInstanceConfigurationPod.clientNetworkConfig(clientConfig))
                        .setGroupConfig(GroupConfig(clientConfig.group, clientConfig.password))
                        .setSerializationConfig(serializationConfig)
                        .setProperty("hazelcast.logging.type", "slf4j")
        )
    }

    fun getClient(name: String): HazelcastInstance {
        return hazelcastClients.getValue(name)
    }

}