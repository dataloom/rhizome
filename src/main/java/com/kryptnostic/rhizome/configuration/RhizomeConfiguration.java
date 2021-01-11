package com.kryptnostic.rhizome.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geekbeast.configuration.postgres.PostgresConfiguration;
import com.kryptnostic.rhizome.configuration.annotation.ReloadableConfiguration;
import com.kryptnostic.rhizome.configuration.graphite.GraphiteConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastSessionFilterConfiguration;
import com.kryptnostic.rhizome.configuration.spark.SparkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * @author Matthew Tamayo-Rios
 */
@ReloadableConfiguration( uri = "rhizome.yaml" )
public class RhizomeConfiguration implements Configuration {
    protected static final String PERSISTENCE_ENABLED_PROPERTY                    = "enable-persistence";
    protected static final String SESSION_CLUSTERING_ENABLED_PROPERTY             = "session-clustering-enabled";
    protected static final String CORS_ACCESS_CONTROL_ALLOW_ORIGIN_URL            = "cors-access-control-allow-origin-url";
    protected static final String POSTGRES_CONFIGURATION                          = "postgres";
    protected static final String SPARK_CONFIGURATION_PROPERTY                    = "spark";
    protected static final String GRAPHITE_CONFIGURATION_PROPERTY                 = "graphite";
    protected static final String HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY = "hazelcast-session-filter";
    protected static final String HAZELCAST_CONFIGURATION_PROPERTY                = "hazelcast";
    protected static final String HAZELCAST_CLIENTS_PROPERTY                      = "hazelcast-clients";
    protected static final String NAME_PROPERTY                                   = "name";

    protected static final boolean                                       PERSISTENCE_ENABLED_DEFAULT        = true;
    protected static final boolean                                       SESSION_CLUSTERING_ENABLED_DEFAULT = false;
    private static final   long                                          serialVersionUID                   = -8444209890618166001L;
    protected static final ConfigurationKey                                  key                                = new SimpleConfigurationKey(
            "rhizome.yaml" );
    protected final  Logger                                                  logger                             = LoggerFactory
            .getLogger(
                    getClass() );
    protected final  boolean                                                 persistData;
    protected final  boolean                                                 sessionClusteringEnabled;
    protected final  String                                                  corsAccessControlAllowOriginUrl;
    protected final  Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration;
    protected final  Optional<GraphiteConfiguration>               graphiteConfiguration;
    protected final  Optional<PostgresConfiguration>               postgresConfiguration;

    protected final Optional<HazelcastConfiguration>              hazelcastConfiguration;
    protected final Optional<Map<String, HazelcastConfiguration>> hazelcastClients;

    protected final Optional<SparkConfiguration> sparkConfiguration;

    protected final String name;

    @JsonCreator
    public RhizomeConfiguration(
            @JsonProperty( PERSISTENCE_ENABLED_PROPERTY ) Optional<Boolean> persistData,
            @JsonProperty( SESSION_CLUSTERING_ENABLED_PROPERTY ) Optional<Boolean> sessionClusteringEnabled,
            @JsonProperty( CORS_ACCESS_CONTROL_ALLOW_ORIGIN_URL )
                    Optional<String> corsAccessControlAllowOriginUrl,
            @JsonProperty( POSTGRES_CONFIGURATION ) Optional<PostgresConfiguration> postgresConfiguration,
            @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY )
                    Optional<GraphiteConfiguration> graphiteConfiguration,
            @JsonProperty( HAZELCAST_CONFIGURATION_PROPERTY )
                    Optional<HazelcastConfiguration> hazelcastConfiguration,
            @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY )
                    Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration,
            @JsonProperty( SPARK_CONFIGURATION_PROPERTY ) Optional<SparkConfiguration> sparkConfig,
            @JsonProperty( NAME_PROPERTY ) Optional<String> name,
            @JsonProperty( HAZELCAST_CLIENTS_PROPERTY )
                    Optional<Map<String, HazelcastConfiguration>> hazelcastClients ) {
        this.persistData = persistData.orElse( PERSISTENCE_ENABLED_DEFAULT );
        this.sessionClusteringEnabled = sessionClusteringEnabled.orElse( SESSION_CLUSTERING_ENABLED_DEFAULT );
        this.corsAccessControlAllowOriginUrl = corsAccessControlAllowOriginUrl.orElse( "" );
        this.postgresConfiguration = postgresConfiguration;
        this.graphiteConfiguration = graphiteConfiguration;
        this.hazelcastConfiguration = hazelcastConfiguration;
        this.hazelcastSessionFilterConfiguration = hazelcastSessionFilterConfiguration;
        this.sparkConfiguration = sparkConfig;
        this.hazelcastClients = hazelcastClients;
        this.name = name.orElse( "rhizome" );
    }

    @JsonProperty( PERSISTENCE_ENABLED_PROPERTY )
    public boolean isPersistenceEnabled() {
        return persistData;
    }

    @JsonProperty( SESSION_CLUSTERING_ENABLED_PROPERTY )
    public boolean isSessionClusteringEnabled() {
        return sessionClusteringEnabled;
    }

    @JsonProperty( CORS_ACCESS_CONTROL_ALLOW_ORIGIN_URL )
    public String getCORSAccessControlAllowOriginUrl() {
        return corsAccessControlAllowOriginUrl;
    }

    @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY )
    public Optional<GraphiteConfiguration> getGraphiteConfiguration() {
        return graphiteConfiguration;
    }

    @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY )
    public Optional<HazelcastSessionFilterConfiguration> getHazelcastSessionFilterConfiguration() {
        return hazelcastSessionFilterConfiguration;
    }

    @JsonProperty( HAZELCAST_CONFIGURATION_PROPERTY )
    public Optional<HazelcastConfiguration> getHazelcastConfiguration() {
        return hazelcastConfiguration;
    }

    @JsonProperty( SPARK_CONFIGURATION_PROPERTY )
    public Optional<SparkConfiguration> getSparkConfiguration() {
        return sparkConfiguration;
    }

    @JsonProperty( POSTGRES_CONFIGURATION )
    public Optional<PostgresConfiguration> getPostgresConfiguration() {
        return postgresConfiguration;
    }

    @JsonProperty(HAZELCAST_CLIENTS_PROPERTY)
    public Optional<Map<String, HazelcastConfiguration>> getHazelcastClients() {
        return hazelcastClients;
    }

    @JsonProperty( NAME_PROPERTY )
    public String getName() {
        return name;
    }

    @Override public String toString() {
        return "RhizomeConfiguration{" +
                "persistData=" + persistData +
                ", sessionClusteringEnabled=" + sessionClusteringEnabled +
                ", corsAccessControlAllowOriginUrl='" + corsAccessControlAllowOriginUrl + '\'' +
                ", hazelcastSessionFilterConfiguration=" + hazelcastSessionFilterConfiguration +
                ", graphiteConfiguration=" + graphiteConfiguration +
                ", postgresConfiguration=" + postgresConfiguration +
                ", hazelcastConfiguration=" + hazelcastConfiguration +
                ", sparkConfiguration=" + sparkConfiguration +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key;
    }

    public static ConfigurationKey key() {
        return key;
    }
}
