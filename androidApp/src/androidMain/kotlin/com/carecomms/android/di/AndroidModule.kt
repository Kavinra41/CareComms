package com.carecomms.android.di

import com.carecomms.android.ui.viewmodels.ChatListViewModel
import com.carecomms.android.ui.viewmodels.EditProfileViewModel
import com.carecomms.android.ui.viewmodels.ChatViewModel
import com.carecomms.android.ui.viewmodels.InvitationViewModel
import com.carecomms.android.data.local.database.ChatDatabase
import com.carecomms.android.data.repository.LocalChatRepository
import com.carecomms.android.data.repository.FirestoreChatRepository
import com.carecomms.android.data.repository.FirebaseInvitationRepository
import com.carecomms.android.data.repository.EmailService
import com.carecomms.data.repository.ChatRepository
import com.carecomms.data.repository.UserRepository
import com.carecomms.data.repository.InvitationRepository
import com.carecomms.data.repository.FirebaseUserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    // Room Database (for future offline caching)
    single { ChatDatabase.getDatabase(androidContext()) }
    single { get<ChatDatabase>().chatDao() }
    single { get<ChatDatabase>().messageDao() }
    
    // Email Service (configure with your Gmail credentials in EmailConfig)
    single { 
        EmailService(
            senderEmail = com.carecomms.android.config.EmailConfig.SENDER_EMAIL,
            senderPassword = com.carecomms.android.config.EmailConfig.SENDER_APP_PASSWORD
        ) 
    }
    
    // Repositories
    single<UserRepository> { FirebaseUserRepository(get()) }
    single<InvitationRepository> { FirebaseInvitationRepository(get(), get(), get()) }
    single<ChatRepository> { FirestoreChatRepository(get(), get(), get()) }
    
    // ViewModels
    viewModel { ChatListViewModel(get(), get()) }
    viewModel { EditProfileViewModel(get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { InvitationViewModel(get(), get()) }
}