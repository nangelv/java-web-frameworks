package javalin.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
@Value
public class VideoStatus {

	public enum VideoState { READY, PROCESSING}

	VideoState state;
}
