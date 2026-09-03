/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * PlaylistChoiceAdapter.kt is part of Isai.
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
 
package com.muthupandi.isai.music.decision

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muthupandi.isai.databinding.ItemPickerChoiceBinding
import com.muthupandi.isai.list.ClickableListListener
import com.muthupandi.isai.list.adapter.FlexibleListAdapter
import com.muthupandi.isai.list.adapter.SimpleDiffCallback
import com.muthupandi.isai.music.resolve
import com.muthupandi.isai.util.context
import com.muthupandi.isai.util.inflater

/**
 * A [FlexibleListAdapter] that displays a list of [PlaylistChoice] options to select from in
 * [AddToPlaylistDialog].
 *
 * @param listener [ClickableListListener] to bind interactions to.
 */
class PlaylistChoiceAdapter(val listener: ClickableListListener<PlaylistChoice>) :
    FlexibleListAdapter<PlaylistChoice, PlaylistChoiceViewHolder>(
        PlaylistChoiceViewHolder.DIFF_CALLBACK
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PlaylistChoiceViewHolder.from(parent)

    override fun onBindViewHolder(holder: PlaylistChoiceViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

/**
 * A [RecyclerView.ViewHolder] that displays an individual playlist choice. Use [from] to create an
 * instance.
 *
 * @author Alexander Capehart (Muthupandi)
 */
class PlaylistChoiceViewHolder private constructor(private val binding: ItemPickerChoiceBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(choice: PlaylistChoice, listener: ClickableListListener<PlaylistChoice>) {
        listener.bind(choice, this)
        binding.pickerImage.apply {
            bind(choice.playlist)
            isActivated = choice.alreadyAdded
        }
        binding.pickerName.text = choice.playlist.name.resolve(binding.context)
    }

    companion object {
        /**
         * Create a new instance.
         *
         * @param parent The parent to inflate this instance from.
         * @return A new instance.
         */
        fun from(parent: ViewGroup) =
            PlaylistChoiceViewHolder(
                ItemPickerChoiceBinding.inflate(parent.context.inflater, parent, false)
            )

        /** A comparator that can be used with DiffUtil. */
        val DIFF_CALLBACK =
            object : SimpleDiffCallback<PlaylistChoice>() {
                override fun areContentsTheSame(oldItem: PlaylistChoice, newItem: PlaylistChoice) =
                    oldItem.playlist.name == newItem.playlist.name &&
                        oldItem.alreadyAdded == newItem.alreadyAdded
            }
    }
}
