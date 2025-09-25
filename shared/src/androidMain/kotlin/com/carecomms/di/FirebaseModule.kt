package com.carecomms.di

import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.FirebaseAuthRepository
import com.carecomms.data.repository.FirebaseFirestoreRepository
import com.carecomms.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val firebaseModule = module {
    // Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    
    // Repositories
    single<FirestoreRepository> { FirebaseFirestoreRepository(get()) }
    single<AuthRepository> { FirebaseAuthRepository(get(), get()) }
}