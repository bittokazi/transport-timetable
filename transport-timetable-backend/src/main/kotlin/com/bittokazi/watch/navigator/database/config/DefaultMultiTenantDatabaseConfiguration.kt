package com.bittokazi.watch.navigator.database.config

import com.bittokazi.watch.navigator.database.migration.V3__seed_user
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class DefaultMultiTenantDatabaseConfiguration(
    val databaseConfigurationHolder: DatabaseConfigurationHolder,
) : MultiTenantDatabaseConfiguration {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[MultiTenantDatabaseConfiguration] -> init")
    }

    private val tenantDatabase: ConcurrentMap<String, MutableMap<String, Database>> = ConcurrentHashMap()

    private val pools: ConcurrentMap<String, MutableMap<String, HikariDataSource>> = ConcurrentHashMap()

    override fun init() {
        databaseConfigurationHolder.databases.forEach {
            connect(
                key = it.key,
                url = it.value.url,
                username = it.value.username,
                password = it.value.password,
                schema = it.value.schema,
                driver = it.value.driver,
            )

            setUpDatabase(it.key)
        }
    }

    private fun init(
        key: String,
        url: String,
        username: String,
        password: String,
        schema: String,
        driver: String,
    ): HikariDataSource {
        val config: HikariConfig = hikariConfigGenerator(url, username, password, schema, driver)
        val existing: HikariDataSource? = pools[key]?.get(schema)
        if (existing != null && existing.isClosed) {
            existing.close()
        }
        val ds = HikariDataSource(config)
        pools[key]?.set(schema, ds) ?: run {
            pools[key] = mutableMapOf()
            pools[key]?.set(schema, ds)
        }
        return ds
    }

    private fun hikariConfigGenerator(
        url: String,
        username: String,
        password: String,
        schema: String,
        driver: String,
    ): HikariConfig {
        val config = HikariConfig()
        config.jdbcUrl = url
        config.username = username
        config.password = password
        config.schema = schema
        config.driverClassName = driver
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return config
    }

    fun dataSource(
        key: String,
        name: String = "public",
    ): HikariDataSource {
        val ds: HikariDataSource? = pools[key]?.get(name)
        if (ds != null && !ds.isClosed) {
            return ds
        } else {
            throw IllegalStateException("DataSource ($name) is absent.")
        }
    }

    private fun connect(
        key: String,
        url: String?,
        username: String?,
        password: String?,
        schema: String?,
        driver: String?,
    ) {
        if (url.isNullOrEmpty() || username.isNullOrEmpty() || password.isNullOrEmpty() || schema.isNullOrEmpty() ||
            driver.isNullOrEmpty()
        ) {
            throw RuntimeException("Database does not exist")
        }
        val dbConfig =
            DatabaseConfig {
                sqlLogger = Slf4jSqlDebugLogger
                useNestedTransactions = true
                defaultFetchSize = null
                defaultIsolationLevel = -1
                warnLongQueriesDuration = null
                maxEntitiesToStoreInCachePerEntity = Int.MAX_VALUE
                keepLoadedReferencesOutOfTransaction = true
            }
        tenantDatabase[key]?.set(
            schema,
            Database.connect(init(key, url, username, password, schema, driver), databaseConfig = dbConfig),
        ) ?: run {
            tenantDatabase[key] = mutableMapOf()
            tenantDatabase[key]?.set(
                schema,
                Database.connect(
                    datasource = init(key, url, username, password, schema, driver),
                    databaseConfig = dbConfig,
                ),
            )
        }
    }

    override fun getTenantDatabase(
        key: String,
        schema: String,
    ): Database {
        return tenantDatabase[key]?.get(schema) ?: throw RuntimeException("Database does not exist")
    }

    fun setUpDatabase(key: String) {
        Flyway.configure().dataSource(dataSource(key)).defaultSchema("public")
            .locations("classpath:oauth_db")
            .load()
            .migrate()

        Flyway.configure().dataSource(dataSource(key)).defaultSchema("public")
            .locations("classpath:oauth_db", "classpath:db")
            .javaMigrations(V3__seed_user()).load().migrate()
    }
}
