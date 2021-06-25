package com.kryptnostic.rhizome.pods;

import com.kryptnostic.rhizome.core.Cutting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

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
@Import( { LocalConfigurationPod.class, AwsConfigurationPod.class, AwsRhizomeConfigurationPod.class, KubernetesConfigurationPod.class } )
public class ConfigurationPod {
    @Inject
    private Environment environment;

    @Bean
    public Cutting getCutting() {
        return new Cutting( environment.getActiveProfiles() );
    }
}
