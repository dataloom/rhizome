package com.kryptnostic.rhizome.pods;

import org.hyperdex.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.kryptnostic.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mapstores.hyperdex.HyperdexConfigurationMapstore;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexResizingClientPool;

@Configuration
@Profile( "hyperdex" )
public class HyperdexPod {
    private static final Logger               logger        = LoggerFactory.getLogger( HyperdexPod.class );

    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();

    static {
        HyperdexPreconfigurer.configure();
    }

    @Bean
    public HyperdexConfiguration hyperdexConfiguration() {
        if ( configuration.getHyperdexConfiguration().isPresent() ) {
            return configuration.getHyperdexConfiguration().get();
        }
        logger.error( "Hyperdex configuration is missing. Please add a hyperdex configuration to rhizome.yaml" );
        return null;
    }

    @Bean
    @Scope(
        value = ConfigurableBeanFactory.SCOPE_PROTOTYPE )
    public Client hyperdexClient() {
        return hyperdexClientPool().acquire();
    }

    @Bean
    public HyperdexClientPool hyperdexClientPool() {
        return new HyperdexResizingClientPool( hyperdexConfiguration() );
    }

    @Bean
    public HyperdexConfigurationMapstore configurationMapStore() {
        HyperdexConfiguration hyperdexConfiguration = hyperdexConfiguration();
        Optional<String> configurationKeyspace;
        if ( hyperdexConfiguration != null ) {
            configurationKeyspace = hyperdexConfiguration().getConfigurationKeyspace();
            if ( configurationKeyspace.isPresent() ) {
                return new HyperdexConfigurationMapstore(
                        ConfigurationConstants.HZ.MAPS.CONFIGURATION,
                        configurationKeyspace.get(),
                        hyperdexClientPool() );
            }
        }
        return null;
    }

}