/*
 * Copyright (c) 2026 Muthupandi (Isai Project)

 * Copyright (c) 2023 OxygenCobalt (Auxio Project)
 * ArtistPlaybackChoiceAdapter.kt is part of Isai.
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
 
package com.muthupandi.isai.playback.decision

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muthupandi.isai.databinding.ItemPickerChoiceBinding
import com.muthupandi.isai.list.ClickableListListener
import com.muthupandi.isai.list.adapter.FlexibleListAdapter
import com.muthupandi.isai.list.adapter.SimpleDiffCallback
import com.muthupandi.isai.music.resolve
import com.muthupandi.isai.util.context
import com.muthupandi.isai.util.inflater
import com.muthupandi.musikr.Artist

/**
 * A [FlexibleListAdapter] that displays a list of [Artist] playback choices, for use with
 * [PlayFromArtistDialog].
 *
 * @param listener A [ClickableListListener] to bind interactions to.
 */
class ArtistPlaybackChoiceAdapter(private val listener: ClickableListListener<Artist>) :
    FlexibleListAdapter<Artist, ArtistPlaybackChoiceViewHolder>(
        ArtistPlaybackChoiceViewHolder.DIFF_CALLBACK
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ArtistPlaybackChoiceViewHolder.from(parent)

    override fun onBindViewHolder(holder: ArtistPlaybackChoiceViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

/**
 * A [RecyclerView.ViewHolder] that displays a smaller variant of a typical [Artist] item, for use
 * [ArtistPlaybackChoiceAdapter]. Use [from] to create an instance.
 *
 * @author Alexander Capehart (Muthupandi)
 */
class ArtistPlaybackChoiceViewHolder
private constructor(private val binding: ItemPickerChoiceBinding) :
    RecyclerView.ViewHolder(binding.root) {
    /**
     * Bind new data to this instance.
     *
     * @param artist The new [Artist] to bind.
     * @param listener A [ClickableListListener] to bind interactions to.
     */
    fun bind(artist: Artist, listener: ClickableListListener<Artist>) {
        listener.bind(artist, this)
        binding.pickerImage.bind(artist)
        binding.pickerName.text = artist.name.resolve(binding.context)
    }

    companion object {

        /**
         * Create a new instance.
         *
         * @param parent The parent to inflate this instance from.
         * @return A new instance.
         */
        fun from(parent: ViewGroup) =
            ArtistPlaybackChoiceViewHolder(
                ItemPickerChoiceBinding.inflate(parent.context.inflater, parent, false)
            )

        /** A comparator that can be used with DiffUtil. */
        val DIFF_CALLBACK =
            object : SimpleDiffCallback<Artist>() {
                override fun areContentsTheSame(oldItem: Artist, newItem: Artist) =
                    oldItem.name == newItem.name
            }
    }
}
