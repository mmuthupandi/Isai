/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * PersistenceDatabase.kt is part of Isai.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package com.muthupandi.isai.playback.persist

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.muthupandi.isai.playback.state.RepeatMode
import com.muthupandi.musikr.Music

/**
 * Provides raw access to the database storing the persisted playback state.
 *
 * @author Alexander Capehart
 */
@Database(
    entities =
        [
            PlaybackState::class,
            QueueHeapItem::class,
            QueueShuffledMappingItem::class,
            PlayHistory::class,
        ],
    version = 39,
    exportSchema = false,
)
@TypeConverters(Music.UID.TypeConverters::class)
abstract class PersistenceDatabase : RoomDatabase() {
    /**
     * Get the current [PlaybackStateDao].
     *
     * @return A [PlaybackStateDao] providing control of the database's playback state tables.
     */
    abstract fun playbackStateDao(): PlaybackStateDao

    /**
     * Get the current [QueueDao].
     *
     * @return A [QueueDao] providing control of the database's queue tables.
     */
    abstract fun queueDao(): QueueDao

    /**
     * Get the current [PlayHistoryDao].
     *
     * @return A [PlayHistoryDao] providing control of the database's play history tables.
     */
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        val MIGRATION_27_32 =
            Migration(27, 32) {
                // Switched from custom names to just letting room pick the names
                it.execSQL("ALTER TABLE playback_state RENAME TO PlaybackState")
                it.execSQL("ALTER TABLE queue_heap RENAME TO QueueHeapItem")
                it.execSQL("ALTER TABLE queue_mapping RENAME TO QueueMappingItem")
            }

        val MIGRATION_38_39 =
            Migration(38, 39) {
                it.execSQL(
                    "CREATE TABLE IF NOT EXISTS `PlayHistory` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `songUid` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `album` TEXT NOT NULL, `durationListenedMs` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
    }
}

/**
 * Provides control of the persisted playback state table.
 *
 * @author Alexander Capehart (Muthupandi)
 */
@Dao
interface PlaybackStateDao {
    /**
     * Get the previously persisted [PlaybackState].
     *
     * @return The previously persisted [PlaybackState], or null if one was not present.
     */
    @Query("SELECT * FROM PlaybackState WHERE id = 0") suspend fun getState(): PlaybackState?

    /** Delete any previously persisted [PlaybackState]s. */
    @Query("DELETE FROM PlaybackState") suspend fun nukeState()

    /**
     * Insert a new [PlaybackState] into the database.
     *
     * @param state The [PlaybackState] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertState(state: PlaybackState)
}

/**
 * Provides control of the persisted queue state tables.
 *
 * @author Alexander Capehart (Muthupandi)
 */
@Dao
interface QueueDao {
    /**
     * Get the previously persisted queue heap.
     *
     * @return A list of persisted [QueueHeapItem]s wrapping each heap item.
     */
    @Query("SELECT * FROM QueueHeapItem") suspend fun getHeap(): List<QueueHeapItem>

    /**
     * Get the previously persisted queue mapping.
     *
     * @return A list of persisted [QueueShuffledMappingItem]s wrapping each heap item.
     */
    @Query("SELECT * FROM QueueShuffledMappingItem")
    suspend fun getShuffledMapping(): List<QueueShuffledMappingItem>

    /** Delete any previously persisted queue heap entries. */
    @Query("DELETE FROM QueueHeapItem") suspend fun nukeHeap()

    /** Delete any previously persisted queue mapping entries. */
    @Query("DELETE FROM QueueShuffledMappingItem") suspend fun nukeShuffledMapping()

    /**
     * Insert new heap entries into the database.
     *
     * @param heap The list of wrapped [QueueHeapItem]s to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertHeap(heap: List<QueueHeapItem>)

    /**
     * Insert new mapping entries into the database.
     *
     * @param mapping The list of wrapped [QueueShuffledMappingItem] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertShuffledMapping(mapping: List<QueueShuffledMappingItem>)
}

// TODO: Figure out how to get RepeatMode to map to an int instead of a string
@Entity
data class PlaybackState(
    @PrimaryKey val id: Int,
    val index: Int,
    val positionMs: Long,
    val repeatMode: RepeatMode,
    val songUid: Music.UID,
    val parentUid: Music.UID?,
)

@Entity data class QueueHeapItem(@PrimaryKey val id: Int, val uid: Music.UID)

@Entity data class QueueShuffledMappingItem(@PrimaryKey val id: Int, val index: Int)

@Entity
data class PlayHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songUid: Music.UID,
    val title: String,
    val artist: String,
    val album: String,
    val durationListenedMs: Long,
    val timestamp: Long,
)

@Dao
interface PlayHistoryDao {
    @Insert suspend fun insert(playHistory: PlayHistory)

    @Query(
        "SELECT songUid, title, artist, album, SUM(durationListenedMs) as totalDurationMs, COUNT(*) as playCount FROM PlayHistory GROUP BY songUid, title, artist, album ORDER BY playCount DESC, totalDurationMs DESC LIMIT 50"
    )
    suspend fun getTopSongs(): List<TopSongStat>

    @Query(
        "SELECT artist, SUM(durationListenedMs) as totalDurationMs, COUNT(*) as playCount FROM PlayHistory GROUP BY artist ORDER BY playCount DESC, totalDurationMs DESC LIMIT 50"
    )
    suspend fun getTopArtists(): List<TopArtistStat>

    @Query(
        "SELECT album, SUM(durationListenedMs) as totalDurationMs, COUNT(*) as playCount FROM PlayHistory GROUP BY album ORDER BY playCount DESC, totalDurationMs DESC LIMIT 50"
    )
    suspend fun getTopAlbums(): List<TopAlbumStat>

    @Query(
        "SELECT timestamp, durationListenedMs FROM PlayHistory WHERE timestamp >= :sinceTimestamp"
    )
    suspend fun getRecentHistory(sinceTimestamp: Long): List<RecentHistoryStat>
}

data class TopSongStat(
    val songUid: Music.UID,
    val title: String,
    val artist: String,
    val album: String,
    val totalDurationMs: Long,
    val playCount: Int,
)

data class TopArtistStat(val artist: String, val totalDurationMs: Long, val playCount: Int)

data class TopAlbumStat(val album: String, val totalDurationMs: Long, val playCount: Int)

data class RecentHistoryStat(val timestamp: Long, val durationListenedMs: Long)
