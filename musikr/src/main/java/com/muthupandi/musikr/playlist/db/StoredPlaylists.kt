/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * StoredPlaylists.kt is part of Isai.
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
 
package com.muthupandi.musikr.playlist.db

import android.content.Context
import com.muthupandi.musikr.Music
import com.muthupandi.musikr.Song
import com.muthupandi.musikr.playlist.PlaylistFile
import com.muthupandi.musikr.playlist.PlaylistHandle
import com.muthupandi.musikr.playlist.SongPointer

abstract class StoredPlaylists {
    internal abstract suspend fun new(name: String, songs: List<Song>): PlaylistHandle

    internal abstract suspend fun read(): List<PlaylistFile>

    companion object {
        fun from(context: Context): StoredPlaylists =
            StoredPlaylistsImpl(PlaylistDatabase.from(context).playlistDao())
    }
}

private class StoredPlaylistsImpl(private val playlistDao: PlaylistDao) : StoredPlaylists() {
    override suspend fun new(name: String, songs: List<Song>): PlaylistHandle {
        val info = PlaylistInfo(Music.UID.isai(Music.UID.Item.PLAYLIST), name)
        playlistDao.insertPlaylist(RawPlaylist(info, songs.map { PlaylistSong(it.uid) }))
        return StoredPlaylistHandle(info, playlistDao)
    }

    override suspend fun read() =
        playlistDao.readRawPlaylists().map {
            PlaylistFile(
                it.playlistInfo.name,
                it.songs.map { song -> SongPointer.UID(song.songUid) },
                StoredPlaylistHandle(it.playlistInfo, playlistDao),
            )
        }
}
