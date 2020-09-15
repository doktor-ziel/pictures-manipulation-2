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
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.videoio.VideoWriter.fourcc;

public class GenerateMovieFromPictures {
    public static Size getExpectedSize(String path) throws IOException {
        Path inputDirPath = Paths.get(path);
        String imagePath = Files.list(inputDirPath).findFirst().orElseThrow().toString();
        Mat image = imread(imagePath, IMREAD_COLOR);
        return image.size();
    }

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String dirPath = "resources/picts";
        String outputPath = "resources/video.avi";

        Size expectedSize = getExpectedSize(dirPath);
        int fps = 4;
        boolean isColor = true;

        VideoWriter videoWriter = new VideoWriter(
                outputPath,
                fourcc('F', 'M', 'P', '4'),
                fps,
                expectedSize,
                isColor);

        if(!videoWriter.isOpened()){
            videoWriter.release();
            throw new IllegalArgumentException(
                    "Video Writer Exception: VideoWriter not opened, check parameters.");
        }

        Files.list(Paths.get(dirPath))
                .map(Path::toString)
                .peek(System.out::println)
                .map(p -> imread(p, IMREAD_COLOR))
                .forEach(videoWriter::write);

        // Or without Stream API:
//        List<Path> filesList = Files.list(Paths.get(dirPath)).collect(Collectors.toList());
//        for (Path filePath : filesList) {
//            String filePathString = filePath.toString();
//            System.out.println(filePathString);
//            Mat image = Imgcodecs.imread(filePathString, IMREAD_COLOR);
//            videoWriter.write(image);
//        }

        videoWriter.release();
    }
}
