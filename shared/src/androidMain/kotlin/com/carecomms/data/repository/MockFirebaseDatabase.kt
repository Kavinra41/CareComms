package com.carecomms.data.repository

// Mock Firebase Database classes to avoid compilation errors
class FirebaseDatabase {
    companion object {
        fun getInstance(): FirebaseDatabase = FirebaseDatabase()
    }
    
    fun getReference(path: String): DatabaseReference = DatabaseReference(path)
}

class DatabaseReference(private val path: String) {
    fun child(childPath: String): DatabaseReference = DatabaseReference("$path/$childPath")
    
    fun orderByChild(key: String): Query = Query(this)
    
    fun push(): DatabaseReference = DatabaseReference("$path/mock-push-key")
    
    fun setValue(value: Any?): MockTask<Void?> = MockTask.success(null)
    
    fun get(): MockTask<DataSnapshot> = MockTask.success(DataSnapshot(emptyMap()))
    
    fun addValueEventListener(listener: ValueEventListener) {
        // Mock implementation - do nothing
    }
    
    fun removeEventListener(listener: ValueEventListener) {
        // Mock implementation - do nothing
    }
}

class Query(private val ref: DatabaseReference) {
    fun equalTo(value: String): Query = this
    fun get(): MockTask<DataSnapshot> = MockTask.success(DataSnapshot(emptyMap()))
}

class DataSnapshot(private val data: Map<String, Any?>) {
    val children: List<DataSnapshot> = emptyList()
    
    fun getValue(clazz: Class<*>): Any? = null
    
    inline fun <reified T> getValue(): T? = null
    
    fun exists(): Boolean = false
}

interface ValueEventListener {
    fun onDataChange(snapshot: DataSnapshot)
    fun onCancelled(error: DatabaseError)
}

class DatabaseError(val message: String)

class Void