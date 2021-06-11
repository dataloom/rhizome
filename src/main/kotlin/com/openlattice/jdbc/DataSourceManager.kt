package com.openlattice.jdbc

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.geekbeast.configuration.postgres.PostgresConfiguration
import com.openlattice.postgres.PostgresTableDefinition
import com.openlattice.postgres.PostgresTableManager
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Component
class DataSourceManager(
        dataSourceConfigurations: Map<String, PostgresConfiguration>,
        healthCheckRegistry: HealthCheckRegistry,
        metricRegistry: MetricRegistry
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DataSourceManager::class.java)
        const val DEFAULT_DATASOURCE = "default"
    }

    private val dataSources = dataSourceConfigurations.mapValues { (dataSourceName, postgresConfiguration) ->

        val hc = HikariConfig(postgresConfiguration.hikariConfiguration)

        hc.healthCheckRegistry = healthCheckRegistry
        hc.metricRegistry = metricRegistry

        logger.info("JDBC URL = {}", hc.jdbcUrl)

        return@mapValues HikariDataSource(hc)
    }
    private val tableManagers = dataSources.mapValues { (dataSourceName, dataSource) ->
        val dataSourceConfiguration = dataSourceConfigurations.getValue(dataSourceName)
        PostgresTableManager(
                dataSource, dataSourceConfiguration.usingCitus, dataSourceConfiguration.initializeIndices,
                dataSourceConfiguration.initializeTables
        )
    }

    fun getDefaultDataSource() = dataSources.getValue(DEFAULT_DATASOURCE)
    fun getDataSource(name: String) = dataSources.getValue(name)

    fun registerTables(name: String, vararg tableDefinitions: PostgresTableDefinition) {
        val tm = tableManagers.getValue(name)
        tm.registerTables(*tableDefinitions)
    }

    fun registerTablesWithAllDatasources(vararg tableDefinitions: PostgresTableDefinition) {
        tableManagers.values.forEach { it.registerTables(*tableDefinitions) }
    }
}