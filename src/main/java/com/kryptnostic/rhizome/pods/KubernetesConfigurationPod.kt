package com.kryptnostic.rhizome.pods

import com.kryptnostic.rhizome.configuration.ConfigurationConstants
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration
import com.openlattice.ResourceConfigurationLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Used to load configuration from files mounted in the container
 *
 * @author Drew Bailey (drew@openlattice.com)
 */
@Configuration
@Profile(ConfigurationConstants.Profiles.KUBERNETES_CONFIGURATION_PROFILE)
class KubernetesConfigurationPod: RootConfigurationSource {
    val logger: Logger = LoggerFactory.getLogger( KubernetesConfigurationPod::class.java )

    @Bean
    override fun rhizomeConfiguration(): RhizomeConfiguration {
        logger.error("Loading rhizome configuration from /etc/secrets")
        return ResourceConfigurationLoader.loadConfigurationFromFile(
                KubernetesConfigurationLoader.rootPath,
                RhizomeConfiguration::class.java
        )
    }

    @Bean
    override fun jettyConfiguration(): JettyConfiguration {
        logger.error("Loading jetty configuration from /etc/secrets")
        return ResourceConfigurationLoader.loadConfigurationFromFile(
                KubernetesConfigurationLoader.rootPath,
                JettyConfiguration::class.java
        )
    }
}