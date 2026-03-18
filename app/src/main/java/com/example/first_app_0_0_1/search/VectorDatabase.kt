package com.example.first_app_0_0_1.search

import java.util.concurrent.ConcurrentHashMap

/**
 * A simple in-memory vector database.
 */
object VectorDatabase {
    private val index = ConcurrentHashMap<String, FloatArray>()

    fun upsert(id: String, vector: FloatArray) {
        index[id] = vector
    }

    fun getVector(id: String): FloatArray? {
        return index[id]
    }

    fun getallVectors(): Map<String, FloatArray> {
        return index.toMap()
    }

    fun delete(id: String) {
        index.remove(id)
    }

    fun clear() {
        index.clear()
    }
}
