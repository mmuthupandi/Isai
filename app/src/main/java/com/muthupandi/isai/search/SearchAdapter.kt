/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * SearchAdapter.kt is part of Isai.
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
 
package com.muthupandi.isai.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muthupandi.isai.list.BasicHeader
import com.muthupandi.isai.list.Item
import com.muthupandi.isai.list.PlainDivider
import com.muthupandi.isai.list.SelectableListListener
import com.muthupandi.isai.list.adapter.SelectionIndicatorAdapter
import com.muthupandi.isai.list.adapter.SimpleDiffCallback
import com.muthupandi.isai.list.recycler.AlbumViewHolder
import com.muthupandi.isai.list.recycler.ArtistViewHolder
import com.muthupandi.isai.list.recycler.BasicHeaderViewHolder
import com.muthupandi.isai.list.recycler.DividerViewHolder
import com.muthupandi.isai.list.recycler.GenreViewHolder
import com.muthupandi.isai.list.recycler.PlaylistViewHolder
import com.muthupandi.isai.list.recycler.SongViewHolder
import com.muthupandi.musikr.Album
import com.muthupandi.musikr.Artist
import com.muthupandi.musikr.Genre
import com.muthupandi.musikr.Music
import com.muthupandi.musikr.Playlist
import com.muthupandi.musikr.Song

/**
 * An adapter that displays search results.
 *
 * @param listener An [SelectableListListener] to bind interactions to.
 * @author Alexander Capehart (Muthupandi)
 */
class SearchAdapter(private val listener: SelectableListListener<Music>) :
    SelectionIndicatorAdapter<Item, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int) =
        when (getItem(position)) {
            is Song -> SongViewHolder.VIEW_TYPE
            is Album -> AlbumViewHolder.VIEW_TYPE
            is Artist -> ArtistViewHolder.VIEW_TYPE
            is Genre -> GenreViewHolder.VIEW_TYPE
            is Playlist -> PlaylistViewHolder.VIEW_TYPE
            is PlainDivider -> DividerViewHolder.VIEW_TYPE
            is BasicHeader -> BasicHeaderViewHolder.VIEW_TYPE
            else -> super.getItemViewType(position)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            SongViewHolder.VIEW_TYPE -> SongViewHolder.from(parent)
            AlbumViewHolder.VIEW_TYPE -> AlbumViewHolder.from(parent)
            ArtistViewHolder.VIEW_TYPE -> ArtistViewHolder.from(parent)
            GenreViewHolder.VIEW_TYPE -> GenreViewHolder.from(parent)
            PlaylistViewHolder.VIEW_TYPE -> PlaylistViewHolder.from(parent)
            DividerViewHolder.VIEW_TYPE -> DividerViewHolder.from(parent)
            BasicHeaderViewHolder.VIEW_TYPE -> BasicHeaderViewHolder.from(parent)
            else -> error("Invalid item type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Song -> (holder as SongViewHolder).bind(item, listener)
            is Album -> (holder as AlbumViewHolder).bind(item, listener)
            is Artist -> (holder as ArtistViewHolder).bind(item, listener)
            is Genre -> (holder as GenreViewHolder).bind(item, listener)
            is Playlist -> (holder as PlaylistViewHolder).bind(item, listener)
            is BasicHeader -> (holder as BasicHeaderViewHolder).bind(item)
        }
    }

    private companion object {
        /** A comparator that can be used with DiffUtil. */
        val DIFF_CALLBACK =
            object : SimpleDiffCallback<Item>() {
                override fun areContentsTheSame(oldItem: Item, newItem: Item) =
                    when (oldItem) {
                        is Song if newItem is Song ->
                            SongViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        is Album if newItem is Album ->
                            AlbumViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        is Artist if newItem is Artist ->
                            ArtistViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        is Genre if newItem is Genre ->
                            GenreViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        is Playlist if newItem is Playlist ->
                            PlaylistViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        is PlainDivider if newItem is PlainDivider ->
                            DividerViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        is BasicHeader if newItem is BasicHeader ->
                            BasicHeaderViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)

                        else -> false
                    }
            }
    }
}
