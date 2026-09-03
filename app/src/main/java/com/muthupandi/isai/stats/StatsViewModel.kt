/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * StatsViewModel.kt is part of Isai.
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
 
package com.muthupandi.isai.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muthupandi.isai.music.MusicRepository
import com.muthupandi.isai.playback.persist.PlayHistoryDao
import com.muthupandi.isai.playback.persist.TopAlbumStat
import com.muthupandi.isai.playback.persist.TopArtistStat
import com.muthupandi.isai.playback.persist.TopSongStat
import com.muthupandi.musikr.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TopSongItem(val stat: TopSongStat, val song: Song?)

@HiltViewModel
class StatsViewModel
@Inject
constructor(
    private val playHistoryDao: PlayHistoryDao,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    private val _topSongs = MutableStateFlow<List<TopSongItem>>(emptyList())
    val topSongs: StateFlow<List<TopSongItem>> = _topSongs

    private val _topArtists = MutableStateFlow<List<TopArtistStat>>(emptyList())
    val topArtists: StateFlow<List<TopArtistStat>> = _topArtists

    private val _topAlbums = MutableStateFlow<List<TopAlbumStat>>(emptyList())
    val topAlbums: StateFlow<List<TopAlbumStat>> = _topAlbums

    private val _totalListeningTimeMs = MutableStateFlow(0L)
    val totalListeningTimeMs: StateFlow<Long> = _totalListeningTimeMs

    private val _totalPlays = MutableStateFlow(0)
    val totalPlays: StateFlow<Int> = _totalPlays

    private val _weeklyData = MutableStateFlow<List<Float>>(emptyList())
    val weeklyData: StateFlow<List<Float>> = _weeklyData

    private val _weeklyLabels = MutableStateFlow<List<String>>(emptyList())
    val weeklyLabels: StateFlow<List<String>> = _weeklyLabels

    fun loadStats() {
        viewModelScope.launch {
            val top = playHistoryDao.getTopSongs()

            // Map to TopSongItem, resolving the song via MusicRepository
            val mappedTopSongs = top.map { stat ->
                val song = musicRepository.find(stat.songUid) as? Song
                TopSongItem(stat, song)
            }
            _topSongs.value = mappedTopSongs

            _topArtists.value = playHistoryDao.getTopArtists()
            _topAlbums.value = playHistoryDao.getTopAlbums()

            _totalListeningTimeMs.value = top.sumOf { it.totalDurationMs }
            _totalPlays.value = top.sumOf { it.playCount }

            // Generate some dummy weekly data for the chart or compute it
            // Assuming recent 7 days
            val dayMs = 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            val startOfLastWeek = now - (7 * dayMs)
            val recent = playHistoryDao.getRecentHistory(startOfLastWeek)

            val dayBuckets = FloatArray(7)
            recent.forEach { stat ->
                val daysAgo = ((now - stat.timestamp) / dayMs).toInt()
                if (daysAgo in 0..6) {
                    dayBuckets[6 - daysAgo] += stat.durationListenedMs.toFloat()
                }
            }

            _weeklyData.value = dayBuckets.toList()
            _weeklyLabels.value = listOf("6d", "5d", "4d", "3d", "2d", "1d", "Now")
        }
    }
}
