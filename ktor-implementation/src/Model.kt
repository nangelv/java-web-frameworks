package ktor.model

import kotlinx.serialization.Serializable

enum class VideoState { READY, PROCESSING }

@Serializable
class VideoStatus(val state: VideoState)

@Serializable
class Video(
    var id: Long = 0,
    var title: String? = null,
    var duration: Long = 0,
    var location: String? = null,
    var subject: String? = null,
    var contentType: String? = null,
    var dataUrl: String? = null)
