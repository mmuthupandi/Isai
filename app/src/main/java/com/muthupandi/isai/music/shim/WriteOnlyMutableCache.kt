/*
 * Copyright (c) 2026 Muthupandi (Isai Project)

 * Copyright (c) 2025 OxygenCobalt (Auxio Project)
 * WriteOnlyMutableCache.kt is part of Isai.
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
 
package com.muthupandi.isai.music.shim

import com.muthupandi.musikr.cache.CacheResult
import com.muthupandi.musikr.cache.CachedFile
import com.muthupandi.musikr.cache.MutableCache
import com.muthupandi.musikr.fs.File

class WriteOnlyMutableCache(private val inner: MutableCache) : MutableCache {
    override suspend fun read(file: File): CacheResult {
        return when (val result = inner.read(file)) {
            is CacheResult.Hit -> CacheResult.Stale(file, result.file.addedMs)
            else -> result
        }
    }

    override suspend fun write(cachedFile: CachedFile) {
        inner.write(cachedFile)
    }

    override suspend fun writeAll(cachedFiles: List<CachedFile>) {
        inner.writeAll(cachedFiles)
    }

    override suspend fun cleanup(excluding: List<CachedFile>) {
        inner.cleanup(excluding)
    }
}
