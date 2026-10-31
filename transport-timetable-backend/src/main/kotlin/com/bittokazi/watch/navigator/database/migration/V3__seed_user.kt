package com.bittokazi.watch.navigator.database.migration

import at.favre.lib.crypto.bcrypt.BCrypt
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

class V3__seed_user : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        if (context == null) throw RuntimeException()
        val schema = context.connection.schema
        val password = "password"
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        context.connection.createStatement().use { insert ->
            insert.execute(
                "INSERT INTO $schema.\"user\"\n" +
                    "(email, first_name, last_name, \"password\")\n" +
                    "VALUES('bitto.kazi@gmail.com', 'Bitto', 'Kazi', '$hashedPassword');",
            )
        }
    }
}
