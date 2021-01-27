import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import ktor.model.Video
import ktor.model.VideoState
import ktor.model.VideoStatus
import java.util.concurrent.atomic.AtomicLong

class VideoServiceController(val videoFileManager: VideoFileManager) {
    val currentId = AtomicLong()
    val videos: MutableMap<Long, Video> = mutableMapOf()

    fun getAll(): List<Video> = videos.values.toList()

    suspend fun create(call: ApplicationCall) {
        val video = call.receive<Video>()
        setVideoIdAndUrl(call.request, video)
        videos[video.id] = video
        call.respond(video)
    }

    suspend fun getOne(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLong()
        val video = videos[id] ?: return call.respondText("Not Found", status = HttpStatusCode.NotFound)

        call.respondOutputStream {
            videoFileManager.copyVideoData(video, this)
        }
    }

    suspend fun upload(call: ApplicationCall) {
        val id = call.parameters["id"]?.toLong()
        val video = videos[id] ?: return call.respondText("Not Found", status = HttpStatusCode.NotFound)
        try {
            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem) {
                    videoFileManager.saveVideoData(video, part.streamProvider())
                }
                part.dispose()
            }
            call.respond(VideoStatus(VideoState.READY))
        } catch (ex: Exception) {
            call.response.status(HttpStatusCode.InternalServerError)
        }
    }

    private fun setVideoIdAndUrl(request: ApplicationRequest, entity: Video) {
        entity.id = currentId.incrementAndGet()
        entity.dataUrl = "http://${request.host()}:${request.port()}/video/${entity.id}/data"
    }
}