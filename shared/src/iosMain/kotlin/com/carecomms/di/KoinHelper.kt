package com.carecomms.di

import com.carecomms.data.database.DatabaseDriverFactory
import com.carecomms.data.database.IOSDatabaseDriverFactory
import com.carecomms.domain.usecase.AuthUseCase
import com.carecomms.domain.usecase.ChatUseCase
import com.carecomms.domain.usecase.InvitationUseCase
import com.carecomms.domain.usecase.CarerRegistrationUseCase
import com.carecomms.domain.usecase.CareeRegistrationUseCase
import com.carecomms.domain.usecase.AnalyticsUseCase
import com.carecomms.presentation.registration.CarerRegistrationViewModel
import com.carecomms.presentation.registration.CareeRegistrationViewModel
import com.carecomms.data.models.DocumentUploadService
import com.carecomms.data.validation.CareeRegistrationValidator
import kotlinx.coroutines.CoroutineScope
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun doInitKoin() {
    startKoin {
        modules(sharedModule, iosModule, iosAccessibilityModule, iosNotificationModule)
    }
}

val iosModule = module {
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
}

fun getAuthUseCase(): AuthUseCase {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getChatUseCase(): ChatUseCase {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getInvitationUseCase(): InvitationUseCase {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getCarerRegistrationUseCase(): CarerRegistrationUseCase {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getCareeRegistrationUseCase(): CareeRegistrationUseCase {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getCareeRegistrationValidator(): CareeRegistrationValidator {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getAnalyticsUseCase(): AnalyticsUseCase {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getDocumentUploadService(): DocumentUploadService {
    return org.koin.core.context.GlobalContext.get().get()
}

fun getCoroutineScope(): CoroutineScope {
    return org.koin.core.context.GlobalContext.get().get()
}

fun createCarerRegistrationViewModel(): CarerRegistrationViewModel {
    val useCase = getCarerRegistrationUseCase()
    val documentService = getDocumentUploadService()
    val scope = getCoroutineScope()
    return CarerRegistrationViewModel(useCase, documentService, scope)
}

fun createCareeRegistrationViewModel(): CareeRegistrationViewModel {
    val useCase = getCareeRegistrationUseCase()
    val validator = getCareeRegistrationValidator()
    val scope = getCoroutineScope()
    return CareeRegistrationViewModel(useCase, validator, scope)
}

fun createChatViewModel(currentUserId: String): com.carecomms.presentation.chat.ChatViewModel {
    val chatUseCase = getChatUseCase()
    return com.carecomms.presentation.chat.ChatViewModel(chatUseCase, currentUserId)
}

class KoinHelper {
    fun getAnalyticsUseCase(): AnalyticsUseCase {
        return org.koin.core.context.GlobalContext.get().get()
    }
    
    fun getAuthUseCase(): AuthUseCase {
        return org.koin.core.context.GlobalContext.get().get()
    }
    
    fun getChatUseCase(): ChatUseCase {
        return org.koin.core.context.GlobalContext.get().get()
    }
}