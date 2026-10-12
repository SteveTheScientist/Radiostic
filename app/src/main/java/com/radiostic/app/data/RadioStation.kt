package com.radiostic.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radio_stations")
data class RadioStation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val frequency: String, // e.g. "91.8 MHz" or "104.0 MHz"
    val isFavorite: Boolean = false,
    val iconName: String = "Podcasts",
    val displayOrder: Int = 0,
    val isOnline: Boolean = false,
    val streamUrl: String? = null
)
