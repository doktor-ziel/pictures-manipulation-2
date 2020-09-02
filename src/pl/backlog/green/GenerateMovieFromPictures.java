package pl.backlog.green;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;

public class GenerateMovieFromPictures {
    public static Size getExpectedSize(String path) throws IOException {
        Path inputDirPath = Paths.get(path);
        String imagePath = Files.list(inputDirPath).findFirst().orElseThrow().toString();
        Mat image = Imgcodecs.imread(imagePath, IMREAD_COLOR);
        return image.size();
    }

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String dirPath = "resources/picts";
        String outputPath = "resources/video.avi";

        Size expectedSize = getExpectedSize(dirPath);

        VideoWriter videoWriter = new VideoWriter(
                outputPath,
                VideoWriter.fourcc('F', 'M', 'P', '4'),
                4,
                expectedSize,
                true);

        if(!videoWriter.isOpened()){
            videoWriter.release();
            throw new IllegalArgumentException(
                    "Video Writer Exception: VideoWriter not opened, check parameters.");
        }

        Files.list(Paths.get(dirPath))
                .map(Path::toString)
                .peek(System.out::println)
                .map(p -> Imgcodecs.imread(p, IMREAD_COLOR))
                .forEach(videoWriter::write);
        videoWriter.release();
    }
}
