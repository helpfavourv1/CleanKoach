package com.zdmgold.cleankoach.core.domain.model

enum class MediaKind { IMAGE, VIDEO, AUDIO }

data class MediaItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val kind: MediaKind,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val dateAdded: Long,
    val mimeType: String,
    val relativePath: String?,
    val isTrashed: Boolean
)
