package com.kryptnostic.rhizome.pods

import com.amazonaws.services.s3.AmazonS3
import com.kryptnostic.rhizome.configuration.ConfigurationConstants
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration
import com.openlattice.ResourceConfigurationLoader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


interface ConfigurationLoader {
    companion object {
        val logger = LoggerFactory.getLogger(ConfigurationLoader::class.java)
    }
    fun <T> load(clazz : Class<T>): T
    fun <T> logAndLoad( name: String, clazz: Class<T> ): T {
        logger.debug( "Loading {} configuration from {}", name, this.type() )
        val t = this.load( clazz )
        logger.info( "Using {} {} configuration: {}", this.type(), name, t.toString() )
        return t
    }
    fun type(): String
}

class AwsConfigurationLoader(private val awsS3: AmazonS3, private val awsLaunchConfig: AmazonLaunchConfiguration) : ConfigurationLoader {
    override fun <T> load(clazz: Class<T>): T {
        return ResourceConfigurationLoader.loadConfigurationFromS3(awsS3,
                awsLaunchConfig.bucket,
                awsLaunchConfig.folder,
                clazz)
    }

    override fun type() = "aws"
}

class LocalConfigurationLoader : ConfigurationLoader {
    override fun <T> load(clazz: Class<T>): T {
        return ResourceConfigurationLoader.loadConfiguration( clazz );
    }

    override fun type() = "local"
}
/**
 * Loads configuration from kubernetes secrets.
 *
 * Note that there is nothing specifically about Kubernetes here. We're just assuming that Kubernetes will mount
 * the configuration files at a specific path. To make this work:
 * * Put all the configuration files in a directory
 * * Run `kubectl [-n namespace] create secret generic service-secret --from-file path/to/dir`
 * * In the deployment for the service, add the secret volume: `{"name": "config-volume", "secret": {"secretName": "service-secret"}}`
 * * In the deployment for the service, add the container volumeMount: `{"mountPath": "/etc/openlattice", "name": "config-volume", "readOnly": true}`
 */
class KubernetesConfigurationLoader : ConfigurationLoader {
    companion object {
        const val rootPath = "/etc/openlattice"
    }
    override fun <T> load(clazz: Class<T>): T {
        return ResourceConfigurationLoader.loadConfigurationFromFile(rootPath, clazz)
    }
    override fun type() = "kubernetes"
}

@Configuration
class ConfigurationLoaderPod {
    @Autowired(required = false)
    private lateinit var awsS3: AmazonS3

    @Autowired(required = false)
    private lateinit var awsLaunchConfig: AmazonLaunchConfiguration


    @Bean(name = ["configurationLoader"])
    @Profile(ConfigurationConstants.Profiles.KUBERNETES_CONFIGURATION_PROFILE)
    fun kubernetesConfigurationLoader() : ConfigurationLoader {
        return KubernetesConfigurationLoader()
    }

    @Bean(name = ["configurationLoader"])
    @Profile(
            ConfigurationConstants.Profiles.LOCAL_CONFIGURATION_PROFILE
    )
    fun localConfigurationLoader() : ConfigurationLoader {
        return LocalConfigurationLoader()
    }

    @Bean(name = ["configurationLoader"])
    @Profile(
            ConfigurationConstants.Profiles.AWS_CONFIGURATION_PROFILE,
            ConfigurationConstants.Profiles.AWS_TESTING_PROFILE
    )
    fun awsConfigurationLoader() : ConfigurationLoader {
        return AwsConfigurationLoader(awsS3, awsLaunchConfig);
    }
}