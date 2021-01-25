/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoServiceController {

    private static final Logger logger = LoggerFactory.getLogger(VideoServiceController.class);
    public static final AtomicLong currentId = new AtomicLong(0L);
    private final VideoFileManager videoFileManager;
    private final Map<Long, Video> videos;

    public VideoServiceController() throws IOException {
        videoFileManager = VideoFileManager.get();
        videos = new ConcurrentHashMap<>();
    }

    @GetMapping(value = "/video")
    @ResponseBody
    public Collection<Video> getVideos() {
        return videos.values();
    }

    @PostMapping("/video")
    @ResponseBody
    public Video addNewVideo(@RequestBody Video video) {
        setVideoIdAndUrl(video);
        videos.put(video.getId(), video);
        return video;
    }

    @PostMapping("/video/{id}/data")
    @ResponseBody
    public VideoStatus uploadVideoContent(
            @PathVariable("id") Long id,
            @RequestParam("data") MultipartFile videoData,
            HttpServletResponse response) {
        Video video = videos.get(id);

        if (video == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        try {
            videoFileManager.saveVideoData(video, videoData.getInputStream());
            return new VideoStatus(VideoStatus.VideoState.READY);
        } catch (IOException e) {
            logger.error("Failed to upload video content for video with id " + video.getId(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return null;
        }
    }

    @GetMapping("/video/{id}/data")
    @ResponseBody
    public void getVideoData(
            @PathVariable("id") Long id,
            HttpServletResponse response) {
        Video video = videos.get(id);
        if (video == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        try {
            videoFileManager.copyVideoData(video, response.getOutputStream());
        } catch (IOException e) {
            logger.error("Unable to fetch video content for video with id " + video.getId(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // -- From the hints section. --

    private void setVideoIdAndUrl(Video entity) {
        entity.setId(currentId.incrementAndGet());
        entity.setDataUrl(getDataUrl(entity.getId()));
    }

    private String getDataUrl(long videoId){
        return getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
    }

    private String getUrlBaseForLocalServer() {
        // Ugly as hell, is there a better way to get the request?
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://"+request.getServerName()
                        + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
    }
}
