package tracker;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    private static final int FPS = 60;

    private VideoCapture capture = new VideoCapture("track.mp4");
    // the id of the camera to be used
    private static int cameraId = 0;

    private ImageView currentFrame;

    private Point crossPoint;

    private Line vertical;

    private Line horizontal;

    private ScheduledExecutorService timer;

    private Mat processedFrame;

    public Controller(ImageView currentFrame, Line v, Line h) {
        this.currentFrame = currentFrame;
        this.vertical = v;
        this.horizontal = h;
        processedFrame = new Mat();
    }

    @FXML
    protected void startCamera() {
        // start the video capture
        this.capture.open("track.mp4");

        // is the video stream available?
        if (this.capture.isOpened()) {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = new Runnable() {

                @Override
                public void run() {
                    Mat frame = grabFrame();
                    Image imageToShow = Utils.mat2Image(frame);

                    updateImageView(currentFrame, imageToShow);
                }
            };

            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, 1000 / FPS, TimeUnit.MILLISECONDS);

        } else {
            // log the error
            System.err.println("Impossible to open the camera connection...");
        }
    }

    private void processFrame(Mat frame) {
        Mat frameHSV = new Mat();
        Mat thresh = new Mat();
        Mat whitePixels = new Mat();

        double h_low = 128;
        double s_low = 128;
        double v_low = 128;
        double h_hi = 255;
        double s_hi = 255;
        double v_hi = 255.0;

        Point result = new Point(0, 0);
        int sumX = 0;
        int sumY = 0;
        int count = 0;

        Scalar lowerThreshold = new Scalar(h_low, s_low, v_low);
        Scalar upperThreshold = new Scalar(h_hi, s_hi, v_hi);

        Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);
        Core.inRange(frameHSV, lowerThreshold, upperThreshold, thresh);
        Core.findNonZero(thresh, whitePixels);
        if (!whitePixels.empty()) {
            MatOfPoint mop = new MatOfPoint(whitePixels);
            Point[] points = mop.toArray();
            count = points.length;
            for (int i = 0; i < count; i++) {
                Point point = new Point();
                point.x = points[i].x;
                point.y = points[i].y;

                sumX += point.x;
                sumY += points[i].y;
            }
        }


        if (count > 0) {
            result.x = sumX / count;
            result.y = sumY / count;
            updateLines(vertical, horizontal, result);
//            System.out.println(result);
        }
    }

    private Mat grabFrame() {
        Runnable ProcessFrame = new Runnable() {
            @Override
            public void run() {
                processFrame(processedFrame);
            }
        };

        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    processedFrame = frame;
                    Thread t = new Thread(ProcessFrame);
                    t.start();
                } else {
                    Platform.exit();
                    System.exit(0);
                }
            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    private void updateLines(Line v, Line h, Point point) {
        Utils.setLineProp(v.startXProperty(), point.x);
        Utils.setLineProp(v.endXProperty(), point.x);
        Utils.setLineProp(v.startYProperty(), point.y + 10);
        Utils.setLineProp(v.endYProperty(), point.y - 10);

        Utils.setLineProp(h.startXProperty(), point.x - 10);
        Utils.setLineProp(h.endXProperty(), point.x + 10);
        Utils.setLineProp(h.startYProperty(), point.y);
        Utils.setLineProp(h.endYProperty(), point.y);
    }

}
