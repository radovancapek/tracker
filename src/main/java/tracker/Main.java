package tracker;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        OpenCV.loadShared();


        ImageView imageView = new ImageView();
        imageView.setFitHeight(600);
        imageView.setFitWidth(800);
        imageView.setPreserveRatio(true);

        Line lineV = new Line(0, 50, 100, 100);
        lineV.setStroke(Color.BLUE);
        lineV.setStrokeWidth(3);

        Line lineH = new Line(0, 50, 100, 100);
        lineH.setStroke(Color.BLUE);
        lineH.setStrokeWidth(3);

        Group root = new Group(imageView);
        root.getChildren().add(lineV);
        root.getChildren().add(lineH);
        primaryStage.setTitle("Tracker");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        Controller controller = new Controller(imageView, lineV, lineH);

        controller.startCamera();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
