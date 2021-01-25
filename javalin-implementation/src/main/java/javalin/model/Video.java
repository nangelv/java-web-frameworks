package javalin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@JsonDeserialize(builder = Video.VideoBuilder.class)
public class Video {
	private long id;
	private String title;
	private long duration;
	private String location;
	private String subject;
	private String contentType;
	private String dataUrl;

	@JsonPOJOBuilder(withPrefix = "")
	public static class VideoBuilder {}
}
