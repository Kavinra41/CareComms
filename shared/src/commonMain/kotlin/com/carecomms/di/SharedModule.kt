package com.carecomms.di

import com.carecomms.accessibility.AccessibilityPreferences
import com.carecomms.data.database.DatabaseDriverFactory
import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.DocumentUploadService
import com.carecomms.data.models.MockDocumentUploadService
import com.carecomms.data.repository.*
import com.carecomms.data.utils.DeepLinkHandler
import com.carecomms.data.validation.*
import com.carecomms.database.CareCommsDatabase
import com.carecomms.domain.repository.*
import com.carecomms.domain.usecase.*
import com.carecomms.presentation.analytics.*
import com.carecomms.presentation.auth.*
import com.carecomms.presentation.chat.*
import com.carecomms.presentation.details.*
import com.carecomms.presentation.error.ErrorHandler
import com.carecomms.presentation.invitation.*
import com.carecomms.presentation.notification.NotificationViewModel
import com.carecomms.presentation.registration.*
import com.carecomms.presentation.state.LoadingStateManager
import com.carecomms.security.*
import com.carecomms.performance.performanceModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val sharedModule = module {
    // JSON serializer
    single { Json { ignoreUnknownKeys = true } }
    
    // Include performance module
    includes(performanceModule(get()))
    
    // Database
    single { get<DatabaseDriverFactory>().createDriver() }
    single { CareCommsDatabase(get()) }
    single { DatabaseManager(get()) }
    
    // Local repositories
    single<LocalUserRepository> { LocalUserRepositoryImpl(get(), get()) }
    single<LocalCacheRepository> { LocalCacheRepositoryImpl(get(), get()) }
    single<ChatRepository> { LocalChatRepository(get(), get()) }
    single<InvitationRepository> { LocalInvitationRepository(get(), get(), get()) }
    single<com.carecomms.domain.repository.AnalyticsRepository> { MockAnalyticsRepository() }
    
    // Note: NotificationRepository and AccessibilityPreferences are provided by platform-specific modules
    
    // Offline-first repository
    single { OfflineFirstRepository(get(), get()) }
    
    // Utilities
    single { DeepLinkHandler() }
    single { com.carecomms.data.utils.RetryMechanism() }
    single { ErrorHandler(get()) }
    single { LoadingStateManager() }
    
    // Offline sync
    single { com.carecomms.data.sync.OfflineSyncManager(get(), get(), get(), get()) }
    
    // Services
    single<DocumentUploadService> { MockDocumentUploadService() }
    
    // Coroutine scope for ViewModels
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    
    // Validators
    single { CarerRegistrationValidator() }
    single { CareeRegistrationValidator() }
    
    // Security components (platform-specific implementations provided by platform modules)
    single { SessionManager(get(), get()) }
    single { SecureRepository(get(), get()) }
    
    // Use cases
    single { AuthUseCase(get()) }
    single { NotificationUseCase(get(), get()) }
    single { ChatUseCase(get(), get()) }
    single { InvitationUseCase(get(), get()) }
    single { CarerRegistrationUseCase(get(), get()) }
    single { CareeRegistrationUseCase(get(), get(), get()) }
    single { AnalyticsUseCase(get()) }
    
    // ViewModels (factories - actual instances created with parameters)
    factory { (currentUserId: String) -> 
        ChatViewModel(get(), currentUserId) 
    }
    factory { (carerId: String) -> 
        ChatListViewModel(get(), carerId) 
    }
    single { 
        AuthViewModel(get()) 
    }
    factory { (carerId: String) -> 
        AnalyticsViewModel(get(), carerId) 
    }
    factory { (carerId: String) -> 
        DetailsTreeViewModel(get(), carerId) 
    }
    factory { 
        InvitationViewModel(get()) 
    }
    factory { 
        CarerRegistrationViewModel(get(), get(), get()) 
    }
    factory { 
        CareeRegistrationViewModel(get()) 
    }
    factory {
        NotificationViewModel(get())
    }
}