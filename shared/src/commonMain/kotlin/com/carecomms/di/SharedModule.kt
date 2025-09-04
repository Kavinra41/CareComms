package com.carecomms.di

import com.carecomms.data.database.DatabaseDriverFactory
import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.repository.*
import com.carecomms.database.CareCommsDatabase
import com.carecomms.domain.repository.ChatRepository
import com.carecomms.domain.repository.InvitationRepository
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val sharedModule = module {
    // JSON serializer
    single { Json { ignoreUnknownKeys = true } }
    
    // Database
    single { get<DatabaseDriverFactory>().createDriver() }
    single { CareCommsDatabase(get()) }
    single { DatabaseManager(get()) }
    
    // Local repositories
    single<LocalUserRepository> { LocalUserRepositoryImpl(get(), get()) }
    single<LocalCacheRepository> { LocalCacheRepositoryImpl(get(), get()) }
    single<ChatRepository> { LocalChatRepository(get(), get()) }
    single<InvitationRepository> { LocalInvitationRepository(get(), get(), get()) }
    
    // Use cases will be added in later tasks
    // ViewModels will be added in later tasks
}