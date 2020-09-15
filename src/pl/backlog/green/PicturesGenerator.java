package pl.backlog.green;

import org.opencv.core.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.circle;

public class PicturesGenerator {
    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Path res = Paths.get("resources/picts");
        Files.createDirectories(res);

        for (int i = 0; i < 200; i++) {
            Mat image = new Mat(200, 200, CvType.CV_8UC3, new Scalar(255.0, 255.0, 255.0));
            circle(image, new Point(i,i), 50, new Scalar(90.0, 60.0, 90.0), 10);
            String path = String.format("%s/pict%03d.png", res.toAbsolutePath().toString(), i);
            imwrite(path, image);
        }
    }
}
