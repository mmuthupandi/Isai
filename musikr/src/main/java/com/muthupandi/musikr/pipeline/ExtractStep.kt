/*
 * Copyright (c) 2026 Muthupandi (Isai Project)
 * Copyright (c) 2026 OxygenCobalt (Auxio Project)
 * ExtractStep.kt is part of Isai.
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
import com.muthupandi.musikr.Config
import com.muthupandi.musikr.cache.Audio
import com.muthupandi.musikr.cache.CachedFile
import com.muthupandi.musikr.cache.MutableCache
import com.muthupandi.musikr.covers.Cover
import com.muthupandi.musikr.covers.CoverResult
import com.muthupandi.musikr.covers.MutableCovers
import com.muthupandi.musikr.metadata.Metadata
import com.muthupandi.musikr.metadata.MetadataExtractor
import com.muthupandi.musikr.metadata.MetadataResult
import com.muthupandi.musikr.tag.parse.TagParser
import com.muthupandi.musikr.util.mapParallel
import com.muthupandi.musikr.util.merge
import com.muthupandi.musikr.util.tryAsyncWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel

internal interface ExtractStep {
    suspend fun extract(
        scope: CoroutineScope,
        explored: Channel<Explored>,
        extracted: Channel<Extracted>,
    ): Deferred<Result<Unit>>

    companion object {
        fun from(context: Context, config: Config): ExtractStep =
            ExtractStepImpl(
                MetadataExtractor.from(context),
                TagParser.new(),
                config.storage.cache,
                config.storage.covers,
            )
    }
}

private class ExtractStepImpl(
    private val metadataExtractor: MetadataExtractor,
    private val tagParser: TagParser,
    private val cache: MutableCache,
    private val covers: MutableCovers<out Cover>,
) : ExtractStep {
    override suspend fun extract(
        scope: CoroutineScope,
        explored: Channel<Explored>,
        extracted: Channel<Extracted>,
    ): Deferred<Result<Unit>> {
        val addingMs = System.currentTimeMillis()
        val extract = Channel<ParsedExtractItem>(PARALLELISM)
        val extractTask =
            scope.mapParallel(PARALLELISM, explored, extract, Dispatchers.IO) { item ->
                when (item) {
                    is RawSong -> Finalized(item)
                    is RawPlaylist -> Finalized(item)
                    is NewSong -> {
                        when (val result = metadataExtractor.extract(item.file)) {
                            is MetadataResult.Success ->
                                result.metadata?.let { metadata -> NeedsParsing(item, metadata) }
                                    ?: Finalized(InvalidSong)
                            MetadataResult.NoMetadata -> Finalized(InvalidSong)
                            MetadataResult.NotAudio -> Finalized(NotAudio)
                            MetadataResult.ProviderFailed -> Finalized(InvalidSong)
                        }
                    }
                    is NotAudio -> Finalized(NotAudio)
                }
            }
        val parsed = Channel<ParsedCachingItem>(Channel.UNLIMITED)
        val parsedTask =
            scope.mapParallel(PARALLELISM, extract, parsed, Dispatchers.IO) { item ->
                when (item) {
                    is Finalized -> item
                    is NeedsParsing -> {
                        val tags = tagParser.parse(item.metadata)
                        val cover =
                            when (val result = covers.create(item.newSong.file, item.metadata)) {
                                is CoverResult.Hit -> result.cover
                                else -> null
                            }
                        NeedsCaching(
                            RawSong(
                                item.newSong.file,
                                item.metadata.properties,
                                tags,
                                cover,
                                // The thing about date added is that it's resolution can
                                // actually be expensive in some modes (ex. saf backend), so
                                // we resolve this by moving date added extraction as an
                                // extraction operation rather than doing the redundant work
                                // during exploration (well, kind of, MediaStore's date
                                // added query is basically free, it's only saf that has
                                // it's slow hacky workaround that we must accommodate
                                // here.)
                                item.newSong.file.addedMs.resolve() ?: addingMs,
                            )
                        )
                    }
                }
            }
        val finalizedTask =
            scope.tryAsyncWith(extracted, Dispatchers.IO) {
                val exclude = mutableListOf<CachedFile>()
                val pending = mutableListOf<CachedFile>()
                for (item in parsed) {
                    val result =
                        when (item) {
                            is Finalized -> item
                            is NeedsCaching -> {
                                pending.add(item.rawSong.toCachedFile())
                                if (pending.size >= CACHE_BATCH_SIZE) {
                                    cache.writeAll(pending.toList())
                                    pending.clear()
                                }
                                Finalized(item.rawSong)
                            }
                        }
                    if (result.extracted is RawSong) {
                        exclude.add(result.extracted.toCachedFile())
                    }
                    it.send(result.extracted)
                }
                if (pending.isNotEmpty()) {
                    cache.writeAll(pending)
                }
                cache.cleanup(exclude)
            }

        return scope.merge(extractTask, parsedTask, finalizedTask)
    }

    private sealed interface ParsedExtractItem

    private data class NeedsParsing(val newSong: NewSong, val metadata: Metadata) :
        ParsedExtractItem

    private sealed interface ParsedCachingItem

    private data class NeedsCaching(val rawSong: RawSong) : ParsedCachingItem

    private data class Finalized(val extracted: Extracted) : ParsedExtractItem, ParsedCachingItem

    private fun RawSong.toCachedFile() =
        CachedFile(file, audio = Audio(properties, tags, cover?.id), addedMs)

    private companion object {
        const val PARALLELISM = 8
        const val CACHE_BATCH_SIZE = 500
    }
}
