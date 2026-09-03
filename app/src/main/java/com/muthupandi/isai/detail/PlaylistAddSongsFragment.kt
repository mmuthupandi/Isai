/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * PlaylistAddSongsFragment.kt is part of Isai.
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
 
package com.muthupandi.isai.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.muthupandi.isai.R
import com.muthupandi.isai.databinding.FragmentAddSongsBinding
import com.muthupandi.isai.home.HomeViewModel
import com.muthupandi.isai.list.SelectableListListener
import com.muthupandi.isai.list.adapter.SelectionIndicatorAdapter
import com.muthupandi.isai.list.recycler.SongViewHolder
import com.muthupandi.isai.music.MusicRepository
import com.muthupandi.isai.ui.ViewBindingFragment
import com.muthupandi.isai.util.collectImmediately
import com.muthupandi.isai.util.showToast
import com.muthupandi.musikr.Music
import com.muthupandi.musikr.Song
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistAddSongsFragment : ViewBindingFragment<FragmentAddSongsBinding>() {
    private val homeModel: HomeViewModel by activityViewModels()
    private val args: PlaylistAddSongsFragmentArgs by navArgs()

    @Inject lateinit var musicRepository: MusicRepository

    private val selectedSongs = mutableSetOf<Song>()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateBinding(inflater: LayoutInflater) =
        FragmentAddSongsBinding.inflate(inflater)

    override fun onBindingCreated(binding: FragmentAddSongsBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)

        songAdapter =
            SongAdapter(
                object : SelectableListListener<Song> {
                    override fun onClick(item: Song, viewHolder: RecyclerView.ViewHolder) {
                        toggleSelection(item, binding)
                    }

                    override fun onOpenMenu(item: Song) {}

                    override fun onSelect(item: Song) {
                        toggleSelection(item, binding)
                    }
                }
            )

        binding.addSongsRecycler.apply {
            adapter = songAdapter
        }

        binding.addSongsToolbar.apply {
            setNavigationOnClickListener { findNavController().navigateUp() }
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_save) {
                    saveSongs()
                    true
                } else {
                    false
                }
            }
        }

        collectImmediately(homeModel.songList) { songs ->
            songAdapter.update(songs, null)
        }

        updateToolbar(binding)
    }

    override fun onDestroyBinding(binding: FragmentAddSongsBinding) {
        super.onDestroyBinding(binding)
        binding.addSongsRecycler.adapter = null
        binding.addSongsToolbar.setOnMenuItemClickListener(null)
    }

    private fun toggleSelection(item: Song, binding: FragmentAddSongsBinding) {
        if (selectedSongs.contains(item)) {
            selectedSongs.remove(item)
        } else {
            selectedSongs.add(item)
        }
        songAdapter.setSelected(selectedSongs)
        updateToolbar(binding)
    }

    private fun updateToolbar(binding: FragmentAddSongsBinding) {
        binding.addSongsToolbar.title =
            if (selectedSongs.isEmpty()) {
                getString(R.string.lbl_add_songs)
            } else {
                getString(R.string.fmt_selected, selectedSongs.size)
            }
        binding.addSongsToolbar.menu.findItem(R.id.action_save)?.isVisible =
            selectedSongs.isNotEmpty()
    }

    private fun saveSongs() {
        if (selectedSongs.isEmpty()) return
        val playlistUid = Music.UID.fromString(args.playlistUid) ?: return
        val playlist = musicRepository.library?.findPlaylist(playlistUid) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            musicRepository.addToPlaylist(selectedSongs.toList(), playlist)
            requireContext().showToast(R.string.fmt_added_to_playlist)
            findNavController().navigateUp()
        }
    }

    private class SongAdapter(private val listener: SelectableListListener<Song>) :
        SelectionIndicatorAdapter<Song, SongViewHolder>(SongViewHolder.DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SongViewHolder.from(parent)

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            holder.bind(getItem(position), listener)
        }
    }
}
