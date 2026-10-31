package com.bittokazi.watch.navigator.persistence.repository

import com.bittokazi.watch.navigator.config.AppConfig
import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.persistence.entity.DtoConverter
import com.bittokazi.watch.navigator.persistence.entity.PaginatedResponse
import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class BaseRepository(
    private val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun <T> query(
        identifier: String = "default",
        call: ApplicationCall?,
        block: () -> T,
    ): T {
        val schema = AttributeKey<String>(AppConfig.TENANT_ATTRIBUTE_KEY)
        return query(identifier, call!!.attributes[schema], block)
    }

    fun <T> query(
        identifier: String = "default",
        schema: String = "public",
        block: () -> T,
    ): T {
        return transaction(multiTenantDatabaseConfiguration.getTenantDatabase(identifier, schema)) {
            addLogger(Slf4jSqlDebugLogger)
            block()
        }
    }

    fun <E : Any, T : Entity<E>> createPaginationQuery(
        entityClass: EntityClass<E, T>,
        query: Query,
        paginationRequest: PaginationRequest,
        distinct: Boolean = false,
        logic: (k: String, v: String) -> Op<Boolean>?
    ): SizedIterable<T> {
        var q = query
        var condition: Op<Boolean>? = null

        paginationRequest.query
            ?.split(" ")
            ?.forEach { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size != 2) return@forEach

                log.info("Processing query part: ${parts[0]} = ${parts[1]}")

                val op = logic(parts[0], parts[1])

                if (op != null) {
                    condition = condition?.and(op) ?: op
                }
            }

        condition?.let {
            q = q.andWhere { it }
        }

        if (distinct) {
            q = q.withDistinct()
        }

        return entityClass.wrapRows(q)
    }

    fun <T, E> getPaginatedResponse(
        iterable: SizedIterable<T>,
        limit: Long,
        page: Long,
        fn: (SizedIterable<T>) -> SizedIterable<T>,
    ): PaginatedResponse<E> where T : DtoConverter<E> {
        // 1. Calculate the total matching count before slicing
        val totalCount = iterable.count()

        // 2. Safe calculation of total pages (ensuring at least 1 page if empty)
        val totalPages = if (totalCount == 0L) 1L else (totalCount + limit - 1) / limit

        // 3. Prevent negative offsets if someone passes page <= 0
        val sanitizedPage = page.coerceAtLeast(1L)
        val offset = (sanitizedPage - 1) * limit

        // 4. CRITICAL FIX: Apply ordering (fn) FIRST, then apply pagination limits
        val orderedIterable = fn(iterable)
        val paginatedData =
            orderedIterable
                .limit(limit.toInt())
                .offset(offset)
                .toList()
                .map { it.toDto() }

        val paginationResponse =
            PaginatedResponse(
                totalCount = totalCount,
                totalPage = totalPages,
                data = paginatedData,
            )

        // Keep your side-effect logic if needed (e.g., population/logging hooks)
        paginationResponse.getAllPages(sanitizedPage)

        return paginationResponse
    }
}
