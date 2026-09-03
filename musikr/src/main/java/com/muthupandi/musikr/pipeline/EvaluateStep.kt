/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * EvaluateStep.kt is part of Isai.
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
 
package com.muthupandi.musikr.pipeline

import android.content.Context
import com.muthupandi.musikr.BuildConfig
import com.muthupandi.musikr.Config
import com.muthupandi.musikr.Interpretation
import com.muthupandi.musikr.MutableLibrary
import com.muthupandi.musikr.graph.MusicGraph
import com.muthupandi.musikr.model.LibraryFactory
import com.muthupandi.musikr.playlist.db.StoredPlaylists
import com.muthupandi.musikr.playlist.interpret.PlaylistInterpreter
import com.muthupandi.musikr.tag.interpret.TagInterpreter
import kotlinx.coroutines.channels.Channel

internal interface EvaluateStep {
    suspend fun evaluate(extractedMusic: Channel<Extracted>): MutableLibrary

    companion object {
        fun new(context: Context, config: Config, interpretation: Interpretation): EvaluateStep =
            EvaluateStepImpl(
                context,
                TagInterpreter.new(interpretation),
                PlaylistInterpreter.new(interpretation),
                config.storage.storedPlaylists,
                LibraryFactory.new(),
            )
    }
}

private class EvaluateStepImpl(
    private val context: Context,
    private val tagInterpreter: TagInterpreter,
    private val playlistInterpreter: PlaylistInterpreter,
    private val storedPlaylists: StoredPlaylists,
    private val libraryFactory: LibraryFactory,
) : EvaluateStep {
    override suspend fun evaluate(extractedMusic: Channel<Extracted>): MutableLibrary {
        val builder = MusicGraph.builder()
        for (extracted in extractedMusic) {
            when (extracted) {
                is RawSong -> builder.add(tagInterpreter.interpret(extracted))
                is RawPlaylist -> builder.add(playlistInterpreter.interpret(extracted.file))
                is NotAudio -> {}
                is InvalidSong -> {}
            }
        }
        val graph = builder.build()

        // Render graph to Graphviz in debug mode
        if (BuildConfig.DEBUG) {
            val fileName = "music_graph_debug.dot"
            graph.renderToGraphviz(context, fileName)
        }

        return libraryFactory.create(graph, storedPlaylists, playlistInterpreter)
    }
}
