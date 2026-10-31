package com.bittokazi.watch.navigator.persistence

import com.bittokazi.watch.navigator.persistence.repository.MonthlyCountRepository
import com.bittokazi.watch.navigator.persistence.repository.PasswordResetTokenRepository
import com.bittokazi.watch.navigator.persistence.repository.RoleRepository
import com.bittokazi.watch.navigator.persistence.repository.UserRepository
import com.bittokazi.watch.navigator.persistence.repository.FeedbackRepository
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configurePersistenceModule() {
    dependencies {
        provide(UserRepository::class)
        provide(FeedbackRepository::class)
        provide(RoleRepository::class)
        provide(MonthlyCountRepository::class)
        provide(PasswordResetTokenRepository::class)
    }
}
