package hse.fcs.se.visualization;

import hse.fcs.se.useful.UsefulMethods;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(VisualizationController.class.getResource("visualization.fxml")));
        primaryStage.setTitle("Visualization");
        primaryStage.setMinHeight(735);
        primaryStage.setMinWidth(735);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setOnCloseRequest(event -> {
            if (!UsefulMethods.showConfirmationAlert("Close program request ",
                    "Are you sure you want to close the program? ",
                    null)) {
                event.consume();
            }
        });
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
