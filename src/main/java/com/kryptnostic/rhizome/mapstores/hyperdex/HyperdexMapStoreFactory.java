package com.kryptnostic.rhizome.mapstores.hyperdex;

import com.geekbeast.rhizome.pods.RegistryBasedMappersPod;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.AbstractMapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.KryptnosticMapStoreFactory;
import com.kryptnostic.rhizome.mapstores.MapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

/**
 *
 * @author Drew Bailey
 *
 */
public class HyperdexMapStoreFactory implements KryptnosticMapStoreFactory {

    private final HyperdexClientPool pool;
    private final String             dbName;

    public HyperdexMapStoreFactory( Builder builder ) {
        super();
        this.pool = builder.getPool();
        this.dbName = builder.getDbName();
    }

    @Override
    public <K, C extends SetProxy<K, V>, V> MapStoreBuilder<K, C> buildSetProxy( Class<K> keyType, Class<V> valType ) {
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType ) {
        KeyMapper<K> keyMapper = (KeyMapper<K>) RegistryBasedMappersPod.getKeyMapper( keyType );
        ValueMapper<V> valueMapper = (ValueMapper<V>) RegistryBasedMappersPod.getValueMapper( valType );
        if ( valueMapper == null ) {
            throw new RuntimeException( "There is no ValueMapper registered for type " + valType );
        }
        if ( keyMapper == null ) {
            throw new RuntimeException( "There is no KeyMapper registered for type " + keyType );
        }
        return new HyperdexMapStoreBuilder<>( pool, dbName, keyMapper, valueMapper );
    }

    public static class HyperdexMapStoreBuilder<K, V> extends AbstractMapStoreBuilder<K, V> {
        private final HyperdexClientPool pool;
        private final String                             dbName;

        public HyperdexMapStoreBuilder(
                HyperdexClientPool pool,
                String dbName,
                KeyMapper<K> keyMapper,
                ValueMapper<V> valueMapper ) {
            super( keyMapper, valueMapper );
            this.dbName = dbName;
            this.pool = pool;
        }

        @Override
        public TestableSelfRegisteringMapStore<K, V> build() {
            HyperdexBaseJacksonKeyValueMapStore<K, V> rethinkDbBaseMapStoreAlternateDriver = new HyperdexBaseJacksonKeyValueMapStore<K, V>(
                    mapName,
                    tableName,
                    pool,
                    keyMapper,
                    valueMapper) {

                @Override
                public MapConfig getMapConfig() {
                    MapConfig mapConfig = super.getMapConfig();
                    if ( objectFormat ) {
                        mapConfig.setInMemoryFormat( InMemoryFormat.OBJECT );
                    }
                    return mapConfig;
                }

                @Override
                public K generateTestKey() {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME drew" );
                }

                @Override
                public V generateTestValue() throws Exception {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME drew" );
                }
            };

            return rethinkDbBaseMapStoreAlternateDriver;
        }
    }

    public static class Builder {
        private HyperdexClientPool pool;
        private String                             dbName;

        public Builder() {}

        public Builder withPool( HyperdexClientPool pool ) {
            this.pool = pool;
            return this;
        }

        public Builder withDbName( String name ) {
            this.dbName = name;
            return this;
        }

        /**
         * @return the pool
         */
        public HyperdexClientPool getPool() {
            return pool;
        }

        /**
         * @return the dbName
         */
        public String getDbName() {
            return dbName;
        }

        public HyperdexMapStoreFactory build() {
            return new HyperdexMapStoreFactory( this );
        }

    }

}
