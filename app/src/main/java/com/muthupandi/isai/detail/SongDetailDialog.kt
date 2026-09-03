/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * SongDetailDialog.kt is part of Isai.
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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.muthupandi.isai.R
import com.muthupandi.isai.databinding.DialogSongDetailBinding
import com.muthupandi.isai.detail.list.SongProperty
import com.muthupandi.isai.detail.list.SongPropertyAdapter
import com.muthupandi.isai.list.adapter.UpdateInstructions
import com.muthupandi.isai.ui.ViewBindingMaterialDialogFragment
import com.muthupandi.isai.util.collectImmediately
import com.muthupandi.musikr.Song
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber as L

/**
 * A [ViewBindingMaterialDialogFragment] that shows information about a Song.
 *
 * @author Alexander Capehart (Muthupandi)
 */
@AndroidEntryPoint
class SongDetailDialog : ViewBindingMaterialDialogFragment<DialogSongDetailBinding>() {
    private val detailModel: DetailViewModel by activityViewModels()
    // Information about what song to display is initially within the navigation arguments
    // as a UID, as that is the only safe way to parcel an song.
    private val args: SongDetailDialogArgs by navArgs()
    private val detailAdapter = SongPropertyAdapter()

    override fun onCreateBinding(inflater: LayoutInflater) =
        DialogSongDetailBinding.inflate(inflater)

    override fun onConfigDialog(builder: AlertDialog.Builder) {
        super.onConfigDialog(builder)
        builder.setTitle(R.string.lbl_props).setPositiveButton(R.string.lbl_ok, null)
    }

    override fun onBindingCreated(binding: DialogSongDetailBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.detailProperties.adapter = detailAdapter
        // DetailViewModel handles most initialization from the navigation argument.
        detailModel.setSong(args.songUid)
        detailModel.toShow.consume()
        collectImmediately(detailModel.currentSong, ::updateSong)
        collectImmediately(detailModel.currentSongProperties, ::updateSongProperties)
    }

    private fun updateSong(song: Song?) {
        L.d("No song to show, navigating away")
        if (song == null) {
            findNavController().navigateUp()
            return
        }
    }

    private fun updateSongProperties(songProperties: List<SongProperty>) {
        detailAdapter.update(songProperties, UpdateInstructions.Replace(0))
    }
}
