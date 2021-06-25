/*
 * Copyright (C) 2017. OpenLattice, Inc
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
 */

package com.openlattice.postgres;

import com.kryptnostic.rhizome.configuration.ConfigurationConstants;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.openlattice.jdbc.JdbcPod;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Configuration
@Profile( ConfigurationConstants.Profiles.POSTGRES_DB_PROFILE )
@Import( JdbcPod.class )
public class PostgresPod {
    @Inject
    private RhizomeConfiguration rhizomeConfiguration;

    @Inject
    private HikariDataSource hds;

    @Autowired( required = false )
    private Set<PostgresTables> spt;

    @Bean
    public PostgresTableManager tableManager() throws SQLException {

        if ( rhizomeConfiguration.getPostgresConfiguration().isPresent() ) {
            final var pgConfig = rhizomeConfiguration.getPostgresConfiguration().get();
            PostgresTableManager ptm = new PostgresTableManager( hds,
                    pgConfig.getUsingCitus(),
                    pgConfig.getInitializeIndices(),
                    pgConfig.getInitializeTables() );
            if ( spt != null ) {
                ptm.registerTables( spt.stream().flatMap( PostgresTables::tables )::iterator );
            }
            return ptm;
        } else {
            throw new IllegalStateException( "Postgres configuration enabled, but no configuration found." );
        }
    }
}