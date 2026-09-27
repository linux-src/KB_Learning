package dev.kbwallet.app.notifications.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
)
