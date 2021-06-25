/*
 * Copyright (C) 2018. OpenLattice, Inc.
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

package com.kryptnostic.rhizome.pods;

import com.amazonaws.services.s3.AmazonS3;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;
import com.openlattice.ResourceConfigurationLoader;
import com.openlattice.aws.AwsS3Pod;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;


@Profile( { Profiles.AWS_CONFIGURATION_PROFILE, Profiles.AWS_TESTING_PROFILE } )
@Configuration
@Import(AwsS3Pod.class)
public class AwsRhizomeConfigurationPod implements RootConfigurationSource {
    @Inject
    private AmazonLaunchConfiguration awsConfig;

    @Inject
    private AmazonS3 s3;

    @Bean
    @NotNull
    public RhizomeConfiguration rhizomeConfiguration() {
        return ResourceConfigurationLoader.loadConfigurationFromS3( s3,
                awsConfig.getBucket(),
                awsConfig.getFolder(),
                RhizomeConfiguration.class );
    }

    @Bean
    @NotNull
    public JettyConfiguration jettyConfiguration() {
        return ResourceConfigurationLoader.loadConfigurationFromS3( s3,
                awsConfig.getBucket(),
                awsConfig.getFolder(),
                JettyConfiguration.class );
    }
}
