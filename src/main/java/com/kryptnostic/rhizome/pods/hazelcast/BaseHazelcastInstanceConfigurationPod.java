package com.kryptnostic.rhizome.pods.hazelcast;

import com.geekbeast.configuration.hazelcast.DurableExecutorConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.*;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfigurationContainer;
import com.kryptnostic.rhizome.configuration.hazelcast.ScheduledExecutorConfiguration;
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.HazelcastPod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This pod provides a basic hazelcast configuration without stream serializers, map stores, or queue stores. If
 * auto-registering of said object is desired use RegistryBasedHazelcastInstanceConfigurationPod.
 *
 * @author Drew Bailey &lt;drew@kryptnostic.com&gt;
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
@Configuration
@Import( { HazelcastPod.class, ConfigurationPod.class } )
public class BaseHazelcastInstanceConfigurationPod {
    public static final  String               defaultQueueName = "default";
    private static final Logger               logger           = LoggerFactory
            .getLogger( BaseHazelcastInstanceConfigurationPod.class );
    @Inject
    protected            RhizomeConfiguration rhizomeConfiguration;

    @Bean
    public HazelcastConfigurationContainer getHazelcastConfiguration() {
        return new HazelcastConfigurationContainer(
                getHazelcastServerConfiguration(),
                getHazelcastClientConfiguration() );
    }

    public Config getHazelcastServerConfiguration() {
        Optional<HazelcastConfiguration> maybeConfiguration = rhizomeConfiguration.getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        if ( hzConfiguration.isServer() ) {
            Config config = new Config( hzConfiguration.getInstanceName() );

            config
                    .setProperty( "hazelcast.logging.type", "slf4j" )
                    .setProperty( "hazelcast.slow.operation.detector.stacktrace.logging.enabled", "true" )
                    .setProperty( "hazelcast.map.load.chunk.size", "100000" )
                    .setClusterName( hzConfiguration.getGroup() )
                    .setSerializationConfig( serializationConfig() )
                    .setMapConfigs( mapConfigs() )
                    .setQueueConfigs( queueConfigs( config.getQueueConfig( defaultQueueName ) ) )
                    .setNetworkConfig( networkConfig( hzConfiguration ) );

            hzConfiguration
                    .getScheduledExecutors()
                    .ifPresent( scheduledExecutors ->
                            config.setScheduledExecutorConfigs( scheduledExecutorConfigs( scheduledExecutors ) ) );

            hzConfiguration
                    .getDurableExecutors()
                    .ifPresent( durableExecutors ->
                            config.setDurableExecutorConfigs( durableExecutorConfigs( durableExecutors ) ) );

            config.getCPSubsystemConfig().setCPMemberCount( hzConfiguration.getCPMemberCount() );
            return config;
        }
        return null;
    }

    private Map<String, DurableExecutorConfig> durableExecutorConfigs( List<DurableExecutorConfiguration> durableExecutors ) {
        return durableExecutors.stream()
                .map( durableExecutor -> {
                    final var dec = new DurableExecutorConfig( durableExecutor.getName() )
                            .setPoolSize( durableExecutor.getPoolSize() )
                            .setCapacity( durableExecutor.getCapacity() )
                            .setDurability( durableExecutor.getDurability() );
                    if ( StringUtils.isNotBlank( durableExecutor.getSplitBrainProtectionName() ) ) {
                        dec.setSplitBrainProtectionName( durableExecutor.getSplitBrainProtectionName() );
                    }
                    return dec;
                } )
                .collect( Collectors.toMap( DurableExecutorConfig::getName, Function.identity() ) );
    }

    protected Map<String, ScheduledExecutorConfig> scheduledExecutorConfigs( List<ScheduledExecutorConfiguration> scheduledExecutors ) {
        return scheduledExecutors.stream()
                .map( scheduledExecutor -> {
                    final var sec = new ScheduledExecutorConfig( scheduledExecutor.getName() )
                            .setPoolSize( scheduledExecutor.getPoolSize() )
                            .setCapacity( scheduledExecutor.getCapacity() )
                            .setDurability( scheduledExecutor.getDurability() );
                    if ( StringUtils.isNotBlank( scheduledExecutor.getSplitBrainProtectionName() ) ) {
                        sec.setSplitBrainProtectionName( scheduledExecutor.getSplitBrainProtectionName() );
                    }
                    return sec;
                } )
                .collect( Collectors.toMap( ScheduledExecutorConfig::getName, Function.identity() ) );
    }

    public ClientConfig getHazelcastClientConfiguration() {
        java.util.Optional<HazelcastConfiguration> maybeConfiguration = rhizomeConfiguration
                .getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        SerializationConfig serializationConfig = serializationConfig();

        logger.info( "Registering the following serializers: {}", serializationConfig );

        return hzConfiguration.isServer() ? null : new ClientConfig()
                .setNetworkConfig( clientNetworkConfig( hzConfiguration ) )
                .setClusterName( hzConfiguration.getGroup() )
                .setSerializationConfig( serializationConfig )
                .setProperty( "hazelcast.logging.type", "slf4j" )
                .setNearCacheConfigMap( nearCacheConfigs() );

    }

    @Bean
    public SerializationConfig serializationConfig() {
        SerializationConfig config = new SerializationConfig()
                .setSerializerConfigs( serializerConfigs() )
                .setAllowUnsafe( true )
                .setUseNativeByteOrder( true );
        return config;
    }

    protected Map<String, NearCacheConfig> nearCacheConfigs() {
        //As of Hz 3.12 there is no default near cache. If it is added in the future we may have to handle default case
        return ImmutableMap.of();
    }

    protected Map<String, MapConfig> mapConfigs() {
        return ImmutableMap.of();
    }

    protected Map<String, QueueConfig> queueConfigs( QueueConfig defaultConfig ) {
        return queueConfigs( ImmutableMap.of( defaultQueueName, defaultConfig ) );
    }

    protected Map<String, QueueConfig> queueConfigs( Map<String, QueueConfig> configs ) {
        return ImmutableMap.copyOf( configs );
    }

    protected Collection<SerializerConfig> serializerConfigs() {
        return ImmutableList.of();
    }

    public static TcpIpConfig tcpIpConfig( List<String> nodes ) {
        return new TcpIpConfig().setMembers( nodes ).setEnabled( true );
    }

    public static ClientNetworkConfig clientNetworkConfig( HazelcastConfiguration hzConfiguration ) {
        return new ClientNetworkConfig().setAddresses( hzConfiguration.getHazelcastSeedNodes() );
    }

    protected static NetworkConfig networkConfig( HazelcastConfiguration hzConfiguration ) {
        return new NetworkConfig()
                .setPort( hzConfiguration.getPort() )
                .setJoin( getJoinConfig( hzConfiguration.getHazelcastSeedNodes() ) )
                .setRestApiConfig( getRestApiConfig(hzConfiguration) );

    }
    protected static RestApiConfig getRestApiConfig( HazelcastConfiguration configuration ) {
        return new RestApiConfig().setEnabled( configuration.isHealthcheckEnabled() );
    }

    protected static JoinConfig getJoinConfig( List<String> nodes ) {
        return new JoinConfig().setMulticastConfig( new MulticastConfig().setEnabled( false ).setLoopbackModeEnabled(
                false ) ).setTcpIpConfig( tcpIpConfig( nodes ) );
    }
}
