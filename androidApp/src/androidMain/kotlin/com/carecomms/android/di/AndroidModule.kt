package com.carecomms.android.di

import com.carecomms.android.ui.viewmodels.ChatListViewModel
import com.carecomms.android.ui.viewmodels.EditProfileViewModel
import com.carecomms.android.ui.viewmodels.ChatViewModel
import com.carecomms.android.data.local.database.ChatDatabase
import com.carecomms.android.data.repository.LocalChatRepository
import com.carecomms.android.data.repository.FirestoreChatRepository
import com.carecomms.data.repository.ChatRepository
import com.carecomms.data.repository.UserRepository
import com.carecomms.data.repository.FirebaseUserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    // Room Database (for future offline caching)
    single { ChatDatabase.getDatabase(androidContext()) }
    single { get<ChatDatabase>().chatDao() }
    single { get<ChatDatabase>().messageDao() }
    
    // Repositories
    single<ChatRepository> { FirestoreChatRepository(get(), get()) }
    single<UserRepository> { FirebaseUserRepository(get()) }
    
    // ViewModels
    viewModel { ChatListViewModel(get()) }
    viewModel { EditProfileViewModel(get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
}