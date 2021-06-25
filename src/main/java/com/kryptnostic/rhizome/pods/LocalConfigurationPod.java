package com.kryptnostic.rhizome.pods;

import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Objects;

/**
 * The configuration pod is responsible for bootstrapping the initial environment. It sets up component scanning
 * <ul>
 * <li>Component Scanning</li>
 * <li>Configurations</li>
 * </ul>
 * By default it does not scan com.geekbeast.rhizome.pods. Each pod should be registered as required in the
 * {@code RhizomeService.initialize(...)} method.
 * 
 * @author Matthew Tamayo-Rios
 */
@Configuration
@Profile( Profiles.LOCAL_CONFIGURATION_PROFILE )
public class LocalConfigurationPod implements RootConfigurationSource {
    private static final Logger               logger = LoggerFactory.getLogger( LocalConfigurationPod.class );
    private static final RhizomeConfiguration rhizomeConfiguration;
    private static final JettyConfiguration   jettyConfiguration;

    static {
        try {
            rhizomeConfiguration = ConfigurationService.StaticLoader.loadConfiguration( RhizomeConfiguration.class );
            jettyConfiguration = ConfigurationService.StaticLoader.loadConfiguration( JettyConfiguration.class );
        } catch ( Exception e ) {
            logger.error( "Error loading configuration!", e );
            throw new Error( "Configuration failure." );
        }
    }

    @Bean
    @NotNull
    public RhizomeConfiguration rhizomeConfiguration() {
        return Objects.requireNonNull( rhizomeConfiguration );
    }

    @Bean
    @NotNull
    public JettyConfiguration jettyConfiguration() {
        return Objects.requireNonNull( jettyConfiguration );
    }
}
