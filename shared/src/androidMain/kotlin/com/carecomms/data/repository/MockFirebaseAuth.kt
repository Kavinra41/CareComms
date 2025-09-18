package com.carecomms.data.repository

// Mock Firebase Auth classes to avoid compilation errors
class FirebaseAuth {
    companion object {
        fun getInstance(): FirebaseAuth = FirebaseAuth()
    }
    
    fun signInWithEmailAndPassword(email: String, password: String): MockTask<MockAuthResult> {
        return MockTask.success(MockAuthResult(MockFirebaseUser(email)))
    }
    
    fun createUserWithEmailAndPassword(email: String, password: String): MockTask<MockAuthResult> {
        return MockTask.success(MockAuthResult(MockFirebaseUser(email)))
    }
    
    val currentUser: MockFirebaseUser? = null
    
    fun signOut() {}
}

class MockFirebaseUser(val email: String) {
    val uid: String = "mock-uid-${email.hashCode()}"
}

class MockAuthResult(val user: MockFirebaseUser?)

class MockTask<T>(private val result: T?, private val exception: Exception? = null) {
    companion object {
        fun <T> success(result: T): MockTask<T> = MockTask(result)
        fun <T> failure(exception: Exception): MockTask<T> = MockTask(null, exception)
    }
    
    fun isSuccessful(): Boolean = exception == null
    fun getResult(): T? = result
    fun getException(): Exception? = exception
    
    suspend fun await(): T {
        if (exception != null) throw exception
        return result!!
    }
}