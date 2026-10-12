package com.radiostic.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RadioStationDao {

    @Query("SELECT * FROM radio_stations ORDER BY displayOrder ASC, id ASC")
    fun getAllStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllOnce(): List<RadioStation>

    @Query("SELECT * FROM radio_stations WHERE isOnline = 0 ORDER BY displayOrder ASC, id ASC")
    fun getOfflineStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE isOnline = 0 ORDER BY displayOrder ASC, id ASC")
    suspend fun getOfflineOnce(): List<RadioStation>

    @Query("SELECT * FROM radio_stations WHERE isOnline = 0 AND isFavorite = 1 ORDER BY displayOrder ASC, id ASC")
    fun getOfflineFavoriteStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE isOnline = 1 ORDER BY displayOrder ASC, id ASC")
    fun getOnlineStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE isOnline = 1 ORDER BY displayOrder ASC, id ASC")
    suspend fun getOnlineOnce(): List<RadioStation>

    @Query("SELECT * FROM radio_stations WHERE isOnline = 1 AND isFavorite = 1 ORDER BY displayOrder ASC, id ASC")
    fun getOnlineFavoriteStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE isFavorite = 1 ORDER BY displayOrder ASC, id ASC")
    fun getFavoriteStations(): Flow<List<RadioStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: RadioStation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<RadioStation>)

    @Update
    suspend fun updateStation(station: RadioStation)

    @Update
    suspend fun updateStations(stations: List<RadioStation>)

    @Delete
    suspend fun deleteStation(station: RadioStation)

    @Query("DELETE FROM radio_stations WHERE isOnline = 0 AND isFavorite = 0")
    suspend fun deleteNonFavoriteOfflineStations()

    @Query("DELETE FROM radio_stations WHERE isFavorite = 0")
    suspend fun deleteNonFavoriteStations()

    @Query("DELETE FROM radio_stations")
    suspend fun deleteAllStations()

    @Query("SELECT COUNT(*) FROM radio_stations")
    suspend fun getStationCount(): Int
}
