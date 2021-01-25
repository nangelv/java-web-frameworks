package javalin;

import io.javalin.http.Context;
import javalin.model.Video;
import javalin.model.VideoStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class VideoServiceController {

    public static final AtomicLong currentId = new AtomicLong();
    private final VideoFileManager videoFileManager;
    private final Map<Long, Video> videos;

    public VideoServiceController() throws IOException {
        videoFileManager = VideoFileManager.get();
        videos = new ConcurrentHashMap<>();
    }


    public void getAll(Context context) {
        context.json(videos.values());
    }

    @SneakyThrows
    public void create(Context context) {
        var video = context.bodyAsClass(Video.class);
        setVideoIdAndUrl(context, video);
        videos.put(video.getId(), video);
        context.json(video);
    }

    public void getOne(Context context) {
        var id = getId(context);
        Video video = videos.get(id);
        if (video == null) {
            context.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        try {
            videoFileManager.copyVideoData(video, context.res.getOutputStream());
        } catch (IOException e) {
            log.error("Unable to fetch video content for video with id " + video.getId(), e);
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    public void uploadData(Context context) {
        var id = getId(context);
        Video video = videos.get(id);
        if (video == null) {
            context.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        try {
            videoFileManager.saveVideoData(video, context.uploadedFile("data").getContent());
            var response = new VideoStatus(VideoStatus.VideoState.READY);
            context.json(response);
        } catch (IOException e) {
            log.error("Failed to upload video content for video with id " + video.getId(), e);
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    private Long getId(Context context) {
        return context.pathParam("id", Long.class).get();
    }

    private void setVideoIdAndUrl(Context context, Video entity) {
        entity.setId(currentId.incrementAndGet());
        entity.setDataUrl(getDataUrl(context, entity.getId()));
    }

    private String getDataUrl(Context context, long videoId) {
        var serverName = context.req.getServerName();
        var port = context.port() != 80 ? ":" + context.port() : "";
        return "http://%s:%s/video/%s/data".formatted(serverName, port, videoId);
    }
}
