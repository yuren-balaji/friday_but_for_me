package com.example.first_app_0_0_1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "automations")
data class Automation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val script: String, // Basic script or JSON representation of a flow
    val triggerType: String, // e.g., "TIME", "EVENT", "VOICE"
    val triggerConfig: String, // JSON config for the trigger
    val isActive: Boolean = true,
    val lastRun: Long? = null
)
