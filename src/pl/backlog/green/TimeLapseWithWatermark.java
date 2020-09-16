package pl.backlog.green;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgcodecs.Imgcodecs.*;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.videoio.VideoWriter.fourcc;

public class TimeLapseWithWatermark {
    public static Size getVideoSize(String path, int width) throws IOException {
        Path inputDirPath = Paths.get(path);
        String imagePath = Files.list(inputDirPath).findFirst().orElseThrow().toString();
        Mat image = imread(imagePath, IMREAD_COLOR);
        Size oldSize = image.size();
        if (oldSize.width > width) {
            double scale = ((double)width)/((double)oldSize.width);
            Size newSize = new Size(oldSize.width*scale, oldSize.height*scale);
            return newSize;
        } else {
            return oldSize;
        }
    }

    public static Mat addWatermark(Mat image, Mat watermark) {
        cvtColor(image, image, COLOR_BGR2BGRA);
        Mat transparentLayer = new Mat(image.rows(), image.cols(), CV_8UC4);
        Rect roi = new Rect(
                image.cols() - watermark.cols()-10,
                image.rows() - watermark.rows()-10,
                watermark.cols(),
                watermark.rows());
        watermark.copyTo(transparentLayer.submat(roi));
        addWeighted(image, 1, transparentLayer, 0.4, 0, image);
        cvtColor(image, image, COLOR_BGRA2BGR);
        return image;
    }

    public static Stream<Mat> addWatermark(Stream<Mat> images, Mat watermark) {
        return images.map(image -> addWatermark(image, watermark));
    }

    public static Mat readWatermark(String path, int width) {
        Mat watermark = imread(path, IMREAD_UNCHANGED);
        Size oldSize = watermark.size();
        double scale = ((double)width)/((double)oldSize.width);
        Size newSize = new Size(oldSize.width*scale, oldSize.height*scale);
        Imgproc.resize(watermark, watermark, newSize);
        return watermark;
    }

    public static void writeMovie(Stream<Mat> images, String outputPath, int fps, Size expectedSize) {
        VideoWriter videoWriter = new VideoWriter(
                outputPath,
                fourcc('F', 'M', 'P', '4'),
                fps,
                expectedSize,
                true);

        if(!videoWriter.isOpened()){
            videoWriter.release();
            throw new IllegalArgumentException(
                    "Video Writer Exception: VideoWriter not opened, check parameters.");
        }

        images.forEach(videoWriter::write);
        videoWriter.release();
    }

    public static Mat resize(Mat image, Size size) {
        Imgproc.resize(image, image, size);
        return image;
    }

    public static void createTimeLapse(String imagesDirPath, String watermarkPath, String outPath, int expectedWidth, int fps) throws IOException {
        Size size = getVideoSize(imagesDirPath, expectedWidth);
        Mat watermark = readWatermark(watermarkPath, (int) (expectedWidth*0.13));

        Stream<Mat> images = Files.list(Paths.get(imagesDirPath))
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .map(p -> imread(p))
                .map(i -> resize(i, size));
        writeMovie(addWatermark(images, watermark), outPath, fps, size);

    }

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String imagesDirPath = args[0];
        String watermarkPath = args[1];
        String outputPath = args[2];
        int expectedSize = Integer.parseInt(args[3]);
        int fps = Integer.parseInt(args[4]);

        createTimeLapse(imagesDirPath, watermarkPath, outputPath, expectedSize, fps);
    }
}
