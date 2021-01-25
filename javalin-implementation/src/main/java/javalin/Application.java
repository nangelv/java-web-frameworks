package javalin;

import io.javalin.Javalin;
import javalin.model.Video;

import java.io.IOException;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Application {

    // Good parts:
    // no annotations
    // routing shows clearly the mapping of methods to URLs
    // no static magic to get context like in Spring
    // simple and relatively easy to understand
    // clean documentation
    //
    // Bad parts:
    // needs additional work to extract parameters (vs Annotation declarations in Spring)
    // no dependency injection (can use third party)
    // not even close to the number of features and integrations supported by spring

    public static void main(String[] args) throws IOException {
        VideoServiceController videoServiceController = new VideoServiceController();
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
        }).routes(() -> {
            get("/video", videoServiceController::getAll);
            post("/video", videoServiceController::create);
            get("/video/:id/data", videoServiceController::getOne);
            post("/video/:id/data", videoServiceController::uploadData);

//            Alternative way to define routing
//
//            path("/video", () -> {
//                get(videoServiceController::getAll);
//                post(videoServiceController::create);
//                path(":id/data", () -> {
//                    get(videoServiceController::getOne);
//                    post(videoServiceController::uploadData);
//                });
//            });
        }).start(8080);
    }
}