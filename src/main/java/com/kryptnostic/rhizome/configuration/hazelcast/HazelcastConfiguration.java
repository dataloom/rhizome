package com.kryptnostic.rhizome.configuration.hazelcast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geekbeast.configuration.hazelcast.DurableExecutorConfiguration;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class HazelcastConfiguration {
    public static final    String DEFAULT_GROUP_NAME       = "rhizome-dev";
    public static final    String DEFAULT_INSTANCE_NAME    = "rhizome-default";
    public static final    String DEFAULT_PASSWORD         = "reticulating splines";
    protected static final String CP_MEMBER_COUNT_PROPERTY = "cp-member-count";
    private static final   String REPLICATION_FACTOR       = "replication-factor";
    private static final   String SEED_NODES_PROPERTY      = "seed-nodes";
    private static final   String NAME_PROPERTY            = "instance-name";
    private static final   String GROUP_PROPERTY           = "group";
    private static final   String PASSWORD_PROPERTY        = "password";
    private static final   String PORT_PROPERTY            = "port";
    private static final   String SERVER_ROLE_PROPERTY     = "server";
    private static final   String DURABLE_EXECUTORS        = "durable-executors";
    private static final   String SCHEDULED_EXECUTORS      = "scheduled-executors";
    private static final   String HZ_HEALTH_CHECK_PROPERTY = "healthcheck";

    // @formatter:off
    public static final    boolean      DEFAULT_HEALTHCHECK        = true;
    public static final    int          DEFAULT_PORT               = 5701;
    public static final    boolean      DEFAULT_SERVER_ROLE        = true;
    protected static final Integer      CP_MEMBER_COUNT_DEFAULT    = 3;
    private static final   List<String> SEED_DEFAULT               = ImmutableList.of( "127.0.0.1" );
    private static final   int          REPLICATION_FACTOR_DEFAULT = 2;
    // @formatter:on

    private final List<String>                                   hazelcastSeedNodes;
    private final int                                            replicationFactor;
    private final String                                         instanceName;
    private final String                                         group;
    private final String                                         password;
    private final int                                            port;
    private final boolean                                        server;
    private final Optional<List<ScheduledExecutorConfiguration>> scheduledExecutors;
    private final Integer                                      cpMemberCount;
    private final Optional<List<DurableExecutorConfiguration>> durableExecutors;
    private final boolean                                        healthcheckEnabled;

    @JsonCreator
    public HazelcastConfiguration(
            @JsonProperty( NAME_PROPERTY ) Optional<String> instanceName,
            @JsonProperty( GROUP_PROPERTY ) Optional<String> group,
            @JsonProperty( PASSWORD_PROPERTY ) Optional<String> password,
            @JsonProperty( PORT_PROPERTY ) Optional<Integer> port,
            @JsonProperty( SERVER_ROLE_PROPERTY ) Optional<Boolean> role,
            @JsonProperty( SEED_NODES_PROPERTY ) Optional<List<String>> hazelcastSeedNodes,
            @JsonProperty( REPLICATION_FACTOR ) Optional<Integer> replicationFactor,
            @JsonProperty( SCHEDULED_EXECUTORS ) Optional<List<ScheduledExecutorConfiguration>> scheduledExecutors,
            @JsonProperty( DURABLE_EXECUTORS ) Optional<List<DurableExecutorConfiguration>> durableExecutors,
            @JsonProperty( HZ_HEALTH_CHECK_PROPERTY ) Optional<Boolean> healthcheck,
            @JsonProperty( CP_MEMBER_COUNT_PROPERTY ) Optional<Integer> cpMemberCount ) {

        this.group = group.orElse( DEFAULT_GROUP_NAME );
        this.password = password.orElse( DEFAULT_PASSWORD );
        this.port = port.orElse( DEFAULT_PORT );
        this.server = role.orElse( DEFAULT_SERVER_ROLE );
        this.hazelcastSeedNodes = hazelcastSeedNodes.orElse( SEED_DEFAULT );
        this.replicationFactor = replicationFactor.orElse( REPLICATION_FACTOR_DEFAULT );
        this.instanceName = instanceName.orElse( DEFAULT_INSTANCE_NAME );
        this.scheduledExecutors = scheduledExecutors;
        this.cpMemberCount = cpMemberCount.orElse( CP_MEMBER_COUNT_DEFAULT );
        this.durableExecutors = durableExecutors;
        this.healthcheckEnabled = healthcheck.orElse( DEFAULT_HEALTHCHECK );
    }

    @JsonProperty( SEED_NODES_PROPERTY )
    public List<String> getHazelcastSeedNodes() {
        return hazelcastSeedNodes;
    }

    @JsonProperty( REPLICATION_FACTOR )
    public int getReplicationFactor() {
        return replicationFactor;
    }

    @JsonProperty( NAME_PROPERTY )
    public String getInstanceName() {
        return instanceName;
    }

    @JsonProperty( GROUP_PROPERTY )
    public String getGroup() {
        return group;
    }

    @JsonProperty( PASSWORD_PROPERTY )
    public String getPassword() {
        return password;
    }

    @JsonProperty( PORT_PROPERTY )
    public int getPort() {
        return port;
    }

    @JsonProperty( SERVER_ROLE_PROPERTY )
    public boolean isServer() {
        return server;
    }

    @JsonProperty( HZ_HEALTH_CHECK_PROPERTY )
    public boolean isHealthcheckEnabled() {
        return healthcheckEnabled;
    }

    @JsonProperty( CP_MEMBER_COUNT_PROPERTY )
    public Integer getCPMemberCount() {
        return cpMemberCount;
    }

    @JsonProperty( SCHEDULED_EXECUTORS )
    public Optional<List<ScheduledExecutorConfiguration>> getScheduledExecutors() {
        return scheduledExecutors;
    }

    @JsonProperty( DURABLE_EXECUTORS )
    public Optional<List<DurableExecutorConfiguration>> getDurableExecutors() {
        return durableExecutors;
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof HazelcastConfiguration ) ) { return false; }
        HazelcastConfiguration that = (HazelcastConfiguration) o;
        return replicationFactor == that.replicationFactor &&
                port == that.port &&
                server == that.server &&
                healthcheckEnabled == that.healthcheckEnabled &&
                Objects.equals( hazelcastSeedNodes, that.hazelcastSeedNodes ) &&
                Objects.equals( instanceName, that.instanceName ) &&
                Objects.equals( group, that.group ) &&
                Objects.equals( password, that.password ) &&
                Objects.equals( scheduledExecutors, that.scheduledExecutors ) &&
                Objects.equals( cpMemberCount, that.cpMemberCount ) &&
                Objects.equals( durableExecutors, that.durableExecutors );
    }

    @Override public int hashCode() {
        return Objects.hash( hazelcastSeedNodes,
                replicationFactor,
                instanceName,
                group,
                password,
                port,
                server,
                scheduledExecutors,
                cpMemberCount,
                durableExecutors,
                healthcheckEnabled );
    }

    @Override public String toString() {
        return "HazelcastConfiguration{" +
                "hazelcastSeedNodes=" + hazelcastSeedNodes +
                ", replicationFactor=" + replicationFactor +
                ", instanceName='" + instanceName + '\'' +
                ", group='" + group + '\'' +
                ", password='" + password + '\'' +
                ", port=" + port +
                ", server=" + server +
                ", scheduledExecutors=" + scheduledExecutors +
                ", cpMemberCount=" + cpMemberCount +
                ", durableExecutors=" + durableExecutors +
                ", healthcheckEnabled=" + healthcheckEnabled +
                '}';
    }
}
