package com.example.first_app_0_0_1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    var isCompleted: Boolean = false,
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val category: String? = null,
    val tags: String? = null, // Comma separated tags
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
