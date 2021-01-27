import ktor.model.Video
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * This class provides a simple implementation to store video binary
 * data on the file system in a "videos" folder. The class provides
 * methods for saving videos and retrieving their binary data.
 *
 * @author jules
 */
class VideoFileManager private constructor() {
    private val targetDir_ = Paths.get("videos")

    // Private helper method for resolving video file paths
    private fun getVideoPath(v: Video?): Path {
        assert(v != null)
        return targetDir_.resolve("video" + v!!.id + ".mpg")
    }

    /**
     * This method returns true if the specified Video has binary
     * data stored on the file system.
     *
     * @param v
     * @return
     */
    fun hasVideoData(v: Video?): Boolean {
        val source = getVideoPath(v)
        return Files.exists(source)
    }

    /**
     * This method copies the binary data for the given video to
     * the provided output stream. The caller is responsible for
     * ensuring that the specified Video has binary data associated
     * with it. If not, this method will throw a FileNotFoundException.
     *
     * @param v
     * @param out
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyVideoData(v: Video, out: OutputStream?) {
        val source = getVideoPath(v)
        if (!Files.exists(source)) {
            throw FileNotFoundException("Unable to find the referenced video file for videoId:" + v.id)
        }
        Files.copy(source, out)
    }

    /**
     * This method reads all of the data in the provided InputStream and stores
     * it on the file system. The data is associated with the Video object that
     * is provided by the caller.
     *
     * @param v
     * @param videoData
     * @throws IOException
     */
    @Throws(IOException::class)
    fun saveVideoData(v: Video?, videoData: InputStream?) {
        assert(videoData != null)
        val target = getVideoPath(v)
        Files.copy(videoData, target, StandardCopyOption.REPLACE_EXISTING)
    }

    companion object {
        /**
         * This static factory method creates and returns a
         * VideoFileManager object to the caller. Feel free to customize
         * this method to take parameters, etc. if you want.
         *
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun get(): VideoFileManager {
            return VideoFileManager()
        }
    }

    // The VideoFileManager.get() method should be used
    // to obtain an instance
    init {
        if (!Files.exists(targetDir_)) {
            Files.createDirectories(targetDir_)
        }
    }
}