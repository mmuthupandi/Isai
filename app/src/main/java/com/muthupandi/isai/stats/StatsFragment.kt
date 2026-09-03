/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * StatsFragment.kt is part of Isai.
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.muthupandi.isai.R
import com.muthupandi.isai.databinding.FragmentStatsBinding
import com.muthupandi.isai.databinding.ItemStatsGroupBinding
import com.muthupandi.isai.databinding.ItemStatsTrackBinding
import com.muthupandi.isai.playback.PlaybackViewModel
import com.muthupandi.isai.util.systemBarInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private val viewModel: StatsViewModel by viewModels()
    private val playbackModel: PlaybackViewModel by activityViewModels()
    private var _binding: FragmentStatsBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.statsScroll.setOnApplyWindowInsetsListener { v, insets ->
            v.updatePadding(
                bottom =
                    insets.systemBarInsetsCompat.bottom +
                        resources.getDimensionPixelSize(R.dimen.spacing_medium)
            )
            insets
        }

        // We can create a simple adapter or just use a custom view, but for simplicity
        // let's just populate the LinearLayout manually if RecyclerView is too verbose,
        // or just use a basic RecyclerView Adapter.
        // Actually, let's just populate a linear layout in code for Top Tracks to keep it simple
        // without writing a full Adapter class if it's just top 5.
        // But since we used RecyclerView in XML, we should use a proper adapter.

        viewModel.loadStats()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalListeningTimeMs.collectLatest { totalMs ->
                val hours = totalMs / (1000 * 60 * 60)
                val minutes = (totalMs / (1000 * 60)) % 60
                binding.statsTotalTime.text = "${hours}h ${minutes}m"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalPlays.collectLatest { plays ->
                binding.statsTotalPlays.text = plays.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weeklyData.collectLatest { data ->
                if (data.isNotEmpty()) {
                    binding.statsBarChart.setData(data, viewModel.weeklyLabels.value)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topSongs.collectLatest { items ->
                binding.statsTopTracksList.removeAllViews()
                items.take(5).forEachIndexed { index, item ->
                    val itemBinding =
                        ItemStatsTrackBinding.inflate(
                            layoutInflater,
                            binding.statsTopTracksList,
                            true,
                        )
                    itemBinding.statsItemRank.text = "#${index + 1}"
                    itemBinding.statsItemTitle.text = item.stat.title
                    itemBinding.statsItemArtist.text = item.stat.artist
                    itemBinding.statsItemPlays.text = "${item.stat.playCount} plays"

                    if (item.song != null) {
                        itemBinding.statsItemCover.bind(item.song)
                    }

                    if (index == 0)
                        itemBinding.statsItemRank.setTextColor(0xFFFFD700.toInt()) // Gold
                    if (index == 1)
                        itemBinding.statsItemRank.setTextColor(0xFFC0C0C0.toInt()) // Silver
                    if (index == 2)
                        itemBinding.statsItemRank.setTextColor(0xFFCD7F32.toInt()) // Bronze
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topArtists.collectLatest { artists ->
                binding.statsTopArtistsList.removeAllViews()
                artists.take(5).forEachIndexed { index, stat ->
                    val itemBinding =
                        ItemStatsGroupBinding.inflate(
                            layoutInflater,
                            binding.statsTopArtistsList,
                            true,
                        )
                    itemBinding.statsGroupRank.text = "#${index + 1}"
                    itemBinding.statsGroupName.text = stat.artist
                    val hours = stat.totalDurationMs / (1000 * 60 * 60)
                    val minutes = (stat.totalDurationMs / (1000 * 60)) % 60
                    val timeStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                    itemBinding.statsGroupDuration.text = "$timeStr • ${stat.playCount} plays"

                    if (index == 0) itemBinding.statsGroupRank.setTextColor(0xFFFFD700.toInt())
                    if (index == 1) itemBinding.statsGroupRank.setTextColor(0xFFC0C0C0.toInt())
                    if (index == 2) itemBinding.statsGroupRank.setTextColor(0xFFCD7F32.toInt())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topAlbums.collectLatest { albums ->
                binding.statsTopAlbumsList.removeAllViews()
                albums.take(5).forEachIndexed { index, stat ->
                    val itemBinding =
                        ItemStatsGroupBinding.inflate(
                            layoutInflater,
                            binding.statsTopAlbumsList,
                            true,
                        )
                    itemBinding.statsGroupRank.text = "#${index + 1}"
                    itemBinding.statsGroupName.text = stat.album
                    val hours = stat.totalDurationMs / (1000 * 60 * 60)
                    val minutes = (stat.totalDurationMs / (1000 * 60)) % 60
                    val timeStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                    itemBinding.statsGroupDuration.text = "$timeStr • ${stat.playCount} plays"

                    if (index == 0) itemBinding.statsGroupRank.setTextColor(0xFFFFD700.toInt())
                    if (index == 1) itemBinding.statsGroupRank.setTextColor(0xFFC0C0C0.toInt())
                    if (index == 2) itemBinding.statsGroupRank.setTextColor(0xFFCD7F32.toInt())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
