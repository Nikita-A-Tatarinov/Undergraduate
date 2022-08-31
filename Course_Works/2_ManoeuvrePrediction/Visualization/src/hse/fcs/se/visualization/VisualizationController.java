package hse.fcs.se.visualization;

import hse.fcs.se.useful.UsefulMethods;
import hse.fcs.se.visualization.editparameters.EditParametersController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class VisualizationController implements Initializable {

    public static final Color CAR_COLOR = Color.RED;
    public static final Color TRUCK_COLOR = Color.YELLOW;
    public static final Color VAN_COLOR = Color.GREEN;
    public static final Color BUS_COLOR = Color.BLUE;
    public static final Color TRAILER_COLOR = Color.MAROON;
    public static final Color PEDESTRIAN_COLOR = Color.GREEN;
    public static final Color BICYCLE_COLOR = Color.BLUE;
    public static final Color MOTORCYCLE_COLOR = Color.YELLOW;
    public static final Color HIGHLIGHT_COLOR = Color.CYAN;
    public static final Color COLOR_OF_SPEED_VECTOR = Color.CYAN;
    public static final Color COLOR_OF_VECTOR_TEXT = Color.CYAN;
    public static final Color COLOR_OF_PREDICTION_TEXT = Color.GOLDENROD;
    public static final Color COLOR_OF_SHIFT_PREDICTION = Color.GOLDENROD;
    public static final String COLOR_OF_SELECTED_BUTTON = "#ADFF2F";
    public static final double RADIUS_OF_VRU_CIRCLES = 7;

    @FXML
    AnchorPane visualizationAnchorPane;
    @FXML
    MenuBar mapChoiceMenuBar;
    @FXML
    Menu datasetChoiceMenu;
    @FXML
    MenuItem inDChoiceMenuItem;
    @FXML
    MenuItem rounDChoiceMenuItem;
    @FXML
    Menu fileNumberChoiceMenu;
    @FXML
    MenuBar modeChoiceMenuBar;
    @FXML
    Menu modeChoiceMenu;
    @FXML
    MenuItem playbackModeMenuItem;
    @FXML
    MenuItem customModeMenuItem;
    @FXML
    MenuItem extendedModeMenuItem;
    @FXML
    Button downloadButton;
    @FXML
    Button uploadButton;
    @FXML
    AnchorPane playbackModeControlsAnchorPane;
    @FXML
    Button backwardButton;
    @FXML
    Button playPauseButton;
    @FXML
    Button forwardButton;
    @FXML
    Button fastBackwardButton;
    @FXML
    Button fastForwardButton;
    @FXML
    Slider currentFrameSlider;
    @FXML
    Label currentFrameLabel;
    @FXML
    Label speedLabel;
    @FXML
    MenuBar speedMenuBar;
    @FXML
    Menu speedMenu;
    @FXML
    MenuItem x1SpeedMenuItem;
    @FXML
    MenuItem x2SpeedMenuItem;
    @FXML
    MenuItem x5SpeedMenuItem;
    @FXML
    MenuItem x10SpeedMenuItem;
    @FXML
    MenuItem x50SpeedMenuItem;
    @FXML
    AnchorPane customModeControlsAnchorPane;
    @FXML
    Button addCarButton;
    @FXML
    Button addTruckButton;
    @FXML
    Button addPedestrianButton;
    @FXML
    Button addBicycleButton;
    @FXML
    Button addMotorcycleButton;
    @FXML
    Button addVanButton;
    @FXML
    Button addBusButton;
    @FXML
    Button addTrailerButton;
    @FXML
    Button handToolButton;
    @FXML
    Button selectionButton;
    @FXML
    Button modificationButton;
    @FXML
    Button deletionButton;
    @FXML
    ScrollPane mapScrollPane;
    @FXML
    AnchorPane mapAnchorPane;
    @FXML
    Label mapResizingLabel;
    @FXML
    MenuBar mapResizingMenuBar;
    @FXML
    Menu mapResizingMenu;
    @FXML
    MenuItem x1MapSizeMenuItem;
    @FXML
    MenuItem x1and5MapSizeMenuItem;
    @FXML
    MenuItem x2MapSizeMenuItem;
    @FXML
    ImageView roadUsersKeywordImageView;
    @FXML
    ImageView manoeuvreKeywordImageView;

    VisualizationModel model;
    int currentFrame;
    double xTransformationCoefficient;
    double yTransformationCoefficient;

    int indexOfSelectedVehicle = -1;
    String selectedTypeOfVehicleToAdd = null;
    ArrayList<ArrayList<Double>> parametersOfVehiclesAtFrame;
    ArrayList<String> predictionsOfVehiclesAtFrame;

    Task<Void> visualizationTask;
    int visualizationSpeed = 50;

    double mapHeight;
    double mapWidth;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new VisualizationModel();
        currentFrameSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentFrame = newValue.intValue();
            currentFrameLabel.setText(currentFrame + " / [" + model.minFrame + "; " + model.maxFrame + "]");
            updatePlaybackButtons();
            if (visualizationTask == null || !visualizationTask.isRunning() ||
                    newValue.intValue() % (50 / visualizationSpeed) == 0) {
                visualizeCurrentFrame();
            }
        });
        datasetChoice("inD", 0, 0);
    }

    public void inDChoiceMenuItemOnAction() {
        datasetChoice(inDChoiceMenuItem.getText(), 0, 0);
    }

    public void rounDChoiceMenuItemOnAction() {
        datasetChoice(rounDChoiceMenuItem.getText(), 0, 0);
    }

    public void playbackModeMenuItemOnAction() {
        if (visualizationTask != null && visualizationTask.isRunning()) {
            visualizationTask.cancel(false);
            visualizationTask = null;
        }
        removeVehicleSelection();
        modeChoiceMenu.setText(playbackModeMenuItem.getText());
        playbackModeControlsAnchorPane.setDisable(false);
        playbackModeControlsAnchorPane.setVisible(true);
        customModeControlsAnchorPane.setDisable(true);
        customModeControlsAnchorPane.setVisible(false);

        mapScrollPane.setPannable(true);
        mapAnchorPane.removeEventHandler(MouseEvent.MOUSE_PRESSED, addOrEditMouseEventHandler);
        mapAnchorPane.getScene().getWindow().removeEventHandler(KeyEvent.KEY_PRESSED, deleteKeyEventHandler);
        currentFrameSlider.setValue(currentFrame + 1);
        currentFrameSlider.setValue(currentFrame - 1);


        AnchorPane.setTopAnchor(roadUsersKeywordImageView, 195.0);
        AnchorPane.setTopAnchor(manoeuvreKeywordImageView, 520.0);
    }

    public void customModeMenuItemOnAction() {
        if (visualizationTask != null && visualizationTask.isRunning()) {
            visualizationTask.cancel(false);
            visualizationTask = null;
        }
        modeChoiceMenu.setText(customModeMenuItem.getText());
        customModeControlsAnchorPane.setDisable(false);
        customModeControlsAnchorPane.setVisible(true);
        playbackModeControlsAnchorPane.setDisable(true);
        playbackModeControlsAnchorPane.setVisible(false);

        indexOfSelectedVehicle = -1;
        selectedTypeOfVehicleToAdd = "selection";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);

        mapScrollPane.setPannable(false);
        mapAnchorPane.addEventHandler(MouseEvent.MOUSE_PRESSED, addOrEditMouseEventHandler);
        mapAnchorPane.getScene().getWindow().addEventHandler(KeyEvent.KEY_PRESSED, deleteKeyEventHandler);

        AnchorPane.setTopAnchor(roadUsersKeywordImageView, 180.0);
        AnchorPane.setTopAnchor(manoeuvreKeywordImageView, 505.0);
    }

    public void extendedModeMenuItemOnAction() {
        if (visualizationTask != null && visualizationTask.isRunning()) {
            visualizationTask.cancel(false);
            visualizationTask = null;
        }
        removeVehicleSelection();
        modeChoiceMenu.setText(extendedModeMenuItem.getText());
        playbackModeControlsAnchorPane.setDisable(false);
        playbackModeControlsAnchorPane.setVisible(true);
        customModeControlsAnchorPane.setDisable(true);
        customModeControlsAnchorPane.setVisible(false);

        mapScrollPane.setPannable(true);
        mapAnchorPane.removeEventHandler(MouseEvent.MOUSE_PRESSED, addOrEditMouseEventHandler);
        mapAnchorPane.getScene().getWindow().removeEventHandler(KeyEvent.KEY_PRESSED, deleteKeyEventHandler);
        currentFrameSlider.setValue(currentFrame + 1);
        currentFrameSlider.setValue(currentFrame - 1);


        AnchorPane.setTopAnchor(roadUsersKeywordImageView, 195.0);
        AnchorPane.setTopAnchor(manoeuvreKeywordImageView, 520.0);
    }

    public void downloadButtonOnAction() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(model.datasetName + "." + model.fileNumber + "." + currentFrame), null);
        UsefulMethods.showAlert(Alert.AlertType.INFORMATION, "Ref copied", "Ref is successfully copied to clipboard ",
                model.datasetName + "." + model.fileNumber + "." + currentFrame);
    }

    public void uploadButtonOnAction() {
        String refS = UsefulMethods.showTextInputDialog("Input ref", "Input a reference to dataset",
                "Reference: ", "datasetName.fileNumber.frameNumber");
        if (refS == null) {
            return;
        }
        ArrayList<String> ref = UsefulMethods.split(refS, ".");
        if (ref.size() != 3) {
            UsefulMethods.showAlert(Alert.AlertType.ERROR, "Incorrect ref format",
                    "Ref has incorrect format", "There should be 3 fields");
            return;
        }
        try {
            datasetChoice(ref.get(0), Integer.parseInt(ref.get(1)), Integer.parseInt(ref.get(2)));
        } catch (NumberFormatException nfe) {
            UsefulMethods.showAlert(Alert.AlertType.ERROR, "Incorrect ref format", null,
                    "Second and third fields must be covertible to integer numbers");
        }
    }

    public void backwardButtonOnAction() {
        currentFrameSlider.setValue(currentFrame - 1);
    }

    public void playPauseButtonOnAction() {
        if (visualizationTask != null && visualizationTask.isRunning()) {
            visualizationTask.cancel(false);
            visualizationTask = null;
        } else {
            initializeAndStartTask();
        }
    }

    public void forwardButtonOnAction() {
        currentFrameSlider.setValue(currentFrame + 1);
    }

    public void fastBackwardButtonOnAction() {
        currentFrameSlider.setValue(currentFrame - 5);
    }

    public void fastForwardButtonOnAction() {
        currentFrameSlider.setValue(currentFrame + 5);
    }

    public void currentFrameSliderOnDragEntered() {
        if (visualizationTask != null && visualizationTask.isRunning()) {
            visualizationTask.cancel(false);
        }
    }

    public void currentFrameSliderOnDragDone() {
        if (visualizationTask != null && visualizationTask.isCancelled()) {
            initializeAndStartTask();
        }
    }

    public void x1SpeedMenuItemOnAction() {
        visualizationSpeed = 50;
        speedMenu.setText(x1SpeedMenuItem.getText());
    }

    public void x2SpeedMenuItemOnAction() {
        visualizationSpeed = 25;
        speedMenu.setText(x2SpeedMenuItem.getText());
    }

    public void x5SpeedMenuItemOnAction() {
        visualizationSpeed = 10;
        speedMenu.setText(x5SpeedMenuItem.getText());
    }

    public void x10SpeedMenuItemOnAction() {
        visualizationSpeed = 5;
        speedMenu.setText(x10SpeedMenuItem.getText());
    }

    public void x50SpeedMenuItemOnAction() {
        visualizationSpeed = 1;
        speedMenu.setText(x50SpeedMenuItem.getText());
    }

    public void addCarButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "car";
        addCarButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addTruckButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "truck";
        addCarButton.setStyle("");
        addTruckButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addPedestrianButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "pedestrian";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addBicycleButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "bicycle";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addMotorcycleButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "motorcycle";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addVanButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "van";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addBusButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "bus";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void addTrailerButtonOnAction() {
        removeVehicleSelection();
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "trailer";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        handToolButton.setStyle("");
        selectionButton.setStyle("");
    }

    public void handToolButtonOnAction() {
        removeVehicleSelection();
        mapScrollPane.setPannable(true);
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        selectedTypeOfVehicleToAdd = "handtool";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
        selectionButton.setStyle("");
    }

    public void selectionButtonOnAction() {
        if (indexOfSelectedVehicle != -1) {
            modificationButton.setDisable(false);
            deletionButton.setDisable(false);
        } else {
            modificationButton.setDisable(true);
            deletionButton.setDisable(true);
        }
        mapScrollPane.setPannable(false);
        selectedTypeOfVehicleToAdd = "selection";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("-fx-background-color: " + COLOR_OF_SELECTED_BUTTON);
    }

    public void modificationButtonOnAction() {
        EditParametersController.getStaticFields(parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(0),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(1),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(3),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(2),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(4),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(5),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(6),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(7),
                parametersOfVehiclesAtFrame.get(indexOfSelectedVehicle).get(8));
        Pane pane;
        try {
            pane = FXMLLoader.load(Objects.requireNonNull(EditParametersController.class.getResource("edit_parameters.fxml")));
        } catch (IOException e) {
            UsefulMethods.showAlert(Alert.AlertType.ERROR, "Problems with loading window for editing parameters",
                    e.getClass().toString(), e.getMessage());
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("Edit parameters");
        stage.setWidth(800.0);
        stage.setHeight(400.0);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(pane));
        stage.showAndWait();
        if ("inD".equals(model.datasetName)) {
            editInDVehicle(indexOfSelectedVehicle, EditParametersController.x, EditParametersController.y,
                    EditParametersController.length, EditParametersController.width,
                    EditParametersController.xVelocity, EditParametersController.yVelocity,
                    EditParametersController.xAcceleration, EditParametersController.yAcceleration,
                    EditParametersController.heading);
        } else {
            editRounDVehicle(indexOfSelectedVehicle, EditParametersController.x, EditParametersController.y,
                    EditParametersController.length, EditParametersController.width,
                    EditParametersController.xVelocity, EditParametersController.yVelocity,
                    EditParametersController.xAcceleration, EditParametersController.yAcceleration,
                    EditParametersController.heading);
        }
    }

    public void deletionButtonOnAction() {
        if (indexOfSelectedVehicle == -1) {
            return;
        }
        mapAnchorPane.getChildren().remove(3 * parametersOfVehiclesAtFrame.size() + indexOfSelectedVehicle + 1);
        mapAnchorPane.getChildren().remove(2 * parametersOfVehiclesAtFrame.size() + indexOfSelectedVehicle + 1);
        mapAnchorPane.getChildren().remove(parametersOfVehiclesAtFrame.size() + indexOfSelectedVehicle + 1);
        mapAnchorPane.getChildren().remove(indexOfSelectedVehicle + 1);
        removeParameters(indexOfSelectedVehicle);
        removePrediction(indexOfSelectedVehicle);
        indexOfSelectedVehicle = -1;
        modificationButton.setDisable(true);
        deletionButton.setDisable(true);
        updatePredictions();
    }

    public void x1MapSizeMenuItemOnAction() {
        if (x1and5MapSizeMenuItem.getText().equals(mapResizingMenu.getText())) {
            mapHeight /= 1.5;
            mapWidth /= 1.5;
            xTransformationCoefficient /= 1.5;
            yTransformationCoefficient /= 1.5;
        } else if (x2MapSizeMenuItem.getText().equals(mapResizingMenu.getText())) {
            mapHeight /= 2;
            mapWidth /= 2;
            xTransformationCoefficient /= 2;
            yTransformationCoefficient /= 2;
        }
        mapResizingMenu.setText(x1SpeedMenuItem.getText());
        ((ImageView) mapAnchorPane.getChildren().get(0)).setFitHeight(mapHeight);
        ((ImageView) mapAnchorPane.getChildren().get(0)).setFitWidth(mapWidth);
        moveObjectsByScale();
    }

    public void x1and5MapSizeMenuItemOnAction() {
        if (x1and5MapSizeMenuItem.getText().equals(mapResizingMenu.getText())) {
            mapHeight /= 1.5;
            mapWidth /= 1.5;
            xTransformationCoefficient /= 1.5;
            yTransformationCoefficient /= 1.5;
        } else if (x2MapSizeMenuItem.getText().equals(mapResizingMenu.getText())) {
            mapHeight /= 2;
            mapWidth /= 2;
            xTransformationCoefficient /= 2;
            yTransformationCoefficient /= 2;
        }
        mapHeight *= 1.5;
        mapWidth *= 1.5;
        xTransformationCoefficient *= 1.5;
        yTransformationCoefficient *= 1.5;
        mapResizingMenu.setText(x1and5MapSizeMenuItem.getText());
        ((ImageView) mapAnchorPane.getChildren().get(0)).setFitHeight(mapHeight);
        ((ImageView) mapAnchorPane.getChildren().get(0)).setFitWidth(mapWidth);
        moveObjectsByScale();
    }

    public void x2MapSizeMenuItemOnAction() {
        if (x1and5MapSizeMenuItem.getText().equals(mapResizingMenu.getText())) {
            mapHeight /= 1.5;
            mapWidth /= 1.5;
            xTransformationCoefficient /= 1.5;
            yTransformationCoefficient /= 1.5;
        } else if (x2MapSizeMenuItem.getText().equals(mapResizingMenu.getText())) {
            mapHeight /= 2;
            mapWidth /= 2;
            xTransformationCoefficient /= 2;
            yTransformationCoefficient /= 2;
        }
        mapHeight *= 2;
        mapWidth *= 2;
        xTransformationCoefficient *= 2;
        yTransformationCoefficient *= 2;
        mapResizingMenu.setText(x2MapSizeMenuItem.getText());
        ((ImageView) mapAnchorPane.getChildren().get(0)).setFitHeight(mapHeight);
        ((ImageView) mapAnchorPane.getChildren().get(0)).setFitWidth(mapWidth);
        moveObjectsByScale();
    }

    void moveObjectsByScale() {
        for (int i = 0; i < parametersOfVehiclesAtFrame.size(); i++) {
            if ("inD".equals(model.datasetName)) {
                editInDVehicle(i, parametersOfVehiclesAtFrame.get(i).get(0), parametersOfVehiclesAtFrame.get(i).get(1),
                        parametersOfVehiclesAtFrame.get(i).get(2), parametersOfVehiclesAtFrame.get(i).get(3),
                        parametersOfVehiclesAtFrame.get(i).get(4), parametersOfVehiclesAtFrame.get(i).get(5),
                        parametersOfVehiclesAtFrame.get(i).get(6), parametersOfVehiclesAtFrame.get(i).get(7),
                        parametersOfVehiclesAtFrame.get(i).get(8));
            } else {
                editRounDVehicle(i, parametersOfVehiclesAtFrame.get(i).get(0), parametersOfVehiclesAtFrame.get(i).get(1),
                        parametersOfVehiclesAtFrame.get(i).get(2), parametersOfVehiclesAtFrame.get(i).get(3),
                        parametersOfVehiclesAtFrame.get(i).get(4), parametersOfVehiclesAtFrame.get(i).get(5),
                        parametersOfVehiclesAtFrame.get(i).get(6), parametersOfVehiclesAtFrame.get(i).get(7),
                        parametersOfVehiclesAtFrame.get(i).get(8));
            }
        }
    }

    void datasetChoice(String newDatasetName, int newFileNumber, int newFrame) {
        removeVehicleSelection();
        try {
            model.refresh(newDatasetName, newFileNumber);
        } catch (IllegalArgumentException iae) {
            UsefulMethods.showAlert(Alert.AlertType.ERROR, "Incorrect ref format", null,
                    iae.getMessage());
            return;
        } catch (IOException e) {
            e.printStackTrace();
            UsefulMethods.showAlert(Alert.AlertType.ERROR, "Problems with reading dataset files ",
                    e.getClass().toString(), e.getMessage());
            return;
        }
        datasetChoiceMenu.setText(newDatasetName);
        fileNumberChoiceMenu.getItems().clear();
        for (int i = model.minFileNumber; i < model.maxFileNumber + 1; i++) {
            fileNumberChoiceMenu.getItems().add(new MenuItem(String.valueOf(i)));
            final MenuItem menuItem = fileNumberChoiceMenu.getItems().get(i - model.minFileNumber);
            menuItem.setOnAction(event -> {
                try {
                    model.refresh(Integer.parseInt(menuItem.getText()));
                } catch (IOException e) {
                    e.printStackTrace();
                    UsefulMethods.showAlert(Alert.AlertType.ERROR, "Problems with reading dataset files",
                            e.getClass().toString(), e.getMessage());
                }
                fileNumberChoiceMenu.setText(menuItem.getText());
                uploadMap(model.minFrame);
            });
        }
        fileNumberChoiceMenu.setText(String.valueOf(newFileNumber));
        uploadMap(newFrame);
    }

    void uploadMap(int newFrame) {
        Image image;
        try {
            image = new Image(new FileInputStream("datasets" + File.separator +
                    model.datasetFullName + File.separator + "data" + File.separator + model.fileNumber / 10 + "" +
                    model.fileNumber % 10 + model.backgroundFileName));
            mapHeight = image.getHeight();
            mapWidth = image.getWidth();
            mapResizingMenu.setText("x1");
            mapAnchorPane.getChildren().clear();
            parametersOfVehiclesAtFrame = new ArrayList<>();
            predictionsOfVehiclesAtFrame = new ArrayList<>();
            mapAnchorPane.getChildren().add(new ImageView(image));
        } catch (IOException e) {
            UsefulMethods.showAlert(Alert.AlertType.ERROR, "Problems with opening file",
                    e.getClass().toString(), e.getMessage());
            e.printStackTrace();
            return;
        }

        if ("inD".equals(model.datasetName)) {
            xTransformationCoefficient = 0.0835 / model.visualizationCoefficient;
        } else {
            xTransformationCoefficient = 0.1 / model.visualizationCoefficient;
        }
        yTransformationCoefficient = -xTransformationCoefficient;

        currentFrameSlider.setMin(model.minFrame);
        currentFrameSlider.setMax(model.maxFrame);
        if (newFrame < model.minFrame) {
            newFrame = model.minFrame;
        }
        if (newFrame > model.maxFrame) {
            newFrame = model.maxFrame;
        }
        currentFrameSlider.setValue(newFrame - 1);
        currentFrameSlider.setValue(newFrame + 1);
        currentFrameSlider.setValue(newFrame);

        if ("inD".equals(model.datasetName)) {
            addPedestrianButton.setDisable(false);
            addBicycleButton.setDisable(false);
            addMotorcycleButton.setDisable(true);
            addVanButton.setDisable(true);
            addBusButton.setDisable(true);
            addTrailerButton.setDisable(true);
        } else {
            addPedestrianButton.setDisable(false);
            addBicycleButton.setDisable(false);
            addMotorcycleButton.setDisable(false);
            addVanButton.setDisable(false);
            addBusButton.setDisable(false);
            addTrailerButton.setDisable(false);
        }
        selectedTypeOfVehicleToAdd = "selection";
        addCarButton.setStyle("");
        addTruckButton.setStyle("");
        addPedestrianButton.setStyle("");
        addBicycleButton.setStyle("");
        addMotorcycleButton.setStyle("");
        addVanButton.setStyle("");
        addBusButton.setStyle("");
        addTrailerButton.setStyle("");
        handToolButton.setStyle("");
        selectionButton.setStyle("-fx-background-color:" + COLOR_OF_SELECTED_BUTTON);

        mapScrollPane.setPannable("playback".equals(modeChoiceMenu.getText()));
    }

    void updatePlaybackButtons() {
        if (currentFrame == model.minFrame) {
            fastBackwardButton.setDisable(true);
            backwardButton.setDisable(true);
        } else {
            fastBackwardButton.setDisable(false);
            backwardButton.setDisable(false);
        }
        if (currentFrame == model.maxFrame) {
            playPauseButton.setDisable(true);
            forwardButton.setDisable(true);
            fastForwardButton.setDisable(true);
        } else {
            playPauseButton.setDisable(false);
            forwardButton.setDisable(false);
            fastForwardButton.setDisable(false);
        }
    }

    void visualizeCurrentFrame() {
        mapAnchorPane.getChildren().remove(1, mapAnchorPane.getChildren().size());
        parametersOfVehiclesAtFrame.clear();
        predictionsOfVehiclesAtFrame.clear();
        try {
            if ("playback".equals(modeChoiceMenu.getText())) {
                for (int i = 0; i < model.tracksByFrames.get(currentFrame - model.minFrame).size(); i++) {
                    if ("inD".equals(model.datasetName)) {
                        addInDVehicle(
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfX)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfY)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfLength)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfWidth)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfHeading)),
                                model.classes.get(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfTrackId))
                        );
                    } else {
                        addRounDVehicle(
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfX)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfY)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfLength)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfWidth)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfHeading)),
                                model.classes.get(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfTrackId))
                        );
                    }
                }
                updatePredictions();
            } else {
                for (int i = 0; i < model.tracksByFrames.get(currentFrame - model.minFrame).size(); i++) {
                    if ("inD".equals(model.datasetName)) {
                        addInDVehicle(
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfX)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfY)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfLength)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfWidth)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfHeading)),
                                model.classes.get(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfTrackId)),
                                model.predictions.get(currentFrame - model.minFrame).get(i).getValue()
                        );
                    } else {
                        addRounDVehicle(
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfX)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfY)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfLength)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfWidth)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYVelocity)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfXAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfYAcceleration)),
                                Double.parseDouble(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfHeading)),
                                model.classes.get(model.tracksByFrames.get(currentFrame - model.minFrame).get(i).get(model.indexOfTrackId)),
                                model.predictions.get(currentFrame - model.minFrame).get(i).getValue()
                        );
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {

        }
    }

    void addInDVehicle(double x, double y, double length, double width, double xVelocity, double yVelocity,
                       double xAcceleration, double yAcceleration, double heading, String classOfVehicle) {
        addNewParameters(x, y, length, width, xVelocity, yVelocity, xAcceleration, yAcceleration, heading);
        if ("car".equals(classOfVehicle) || "truck_bus".equals(classOfVehicle)) {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Rectangle(
                    (x - length / 2) * xTransformationCoefficient, (y + width / 2) * yTransformationCoefficient,
                    length * xTransformationCoefficient, width * xTransformationCoefficient));
            if ("car".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(CAR_COLOR);
            } else {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(TRUCK_COLOR);
            }
            mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        } else {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Circle(
                    x * xTransformationCoefficient, y * yTransformationCoefficient, RADIUS_OF_VRU_CIRCLES));
            if ("pedestrian".equals(classOfVehicle)) {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(PEDESTRIAN_COLOR);
            } else {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(BICYCLE_COLOR);
            }
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        }
        mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
        mapAnchorPane.getChildren().add(2 * parametersOfVehiclesAtFrame.size(), getSpeedVector(x, y, xVelocity, yVelocity, heading));
        mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        if ("playback".equals(modeChoiceMenu.getText())) {
            addPrediction("faster");
            mapAnchorPane.getChildren().add(3 * parametersOfVehiclesAtFrame.size(),
                    getVectorTextFlow(x, y, xVelocity, yVelocity, "faster"));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
            mapAnchorPane.getChildren().add(4 * parametersOfVehiclesAtFrame.size(),
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, "faster"));
            mapAnchorPane.getChildren().get(4 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        } else {
            String prediction = model.getPrediction(
                    UsefulMethods.split(model.getArrayOfNearestNeighbours(getTracksOfFrame()).get(parametersOfVehiclesAtFrame.size() - 1), ","),
                    classOfVehicle);
            addPrediction(prediction);
            mapAnchorPane.getChildren().add(3 * parametersOfVehiclesAtFrame.size(),
                    getVectorTextFlow(x, y, xVelocity, yVelocity, prediction));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
            mapAnchorPane.getChildren().add(4 * parametersOfVehiclesAtFrame.size(),
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, prediction));
            mapAnchorPane.getChildren().get(4 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
            updatePredictions();
        }
    }

    void addInDVehicle(double x, double y, double length, double width, double xVelocity, double yVelocity,
                       double xAcceleration, double yAcceleration, double heading, String classOfVehicle, String prediction) {
        addNewParameters(x, y, length, width, xVelocity, yVelocity, xAcceleration, yAcceleration, heading);
        if ("car".equals(classOfVehicle) || "truck_bus".equals(classOfVehicle)) {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Rectangle(
                    (x - length / 2) * xTransformationCoefficient, (y + width / 2) * yTransformationCoefficient,
                    length * xTransformationCoefficient, width * xTransformationCoefficient));
            if ("car".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(CAR_COLOR);
            } else {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(TRUCK_COLOR);
            }
            mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        } else {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Circle(
                    x * xTransformationCoefficient, y * yTransformationCoefficient, RADIUS_OF_VRU_CIRCLES));
            if ("pedestrian".equals(classOfVehicle)) {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(PEDESTRIAN_COLOR);
            } else {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(BICYCLE_COLOR);
            }
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        }
        mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
        mapAnchorPane.getChildren().add(2 * parametersOfVehiclesAtFrame.size(), getSpeedVector(x, y, xVelocity, yVelocity, heading));
        mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        addPrediction(prediction);
        mapAnchorPane.getChildren().add(3 * parametersOfVehiclesAtFrame.size(),
                getVectorTextFlow(x, y, xVelocity, yVelocity, prediction));
        mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        mapAnchorPane.getChildren().add(4 * parametersOfVehiclesAtFrame.size(),
                getPredictionPolyline(x, y, xVelocity, yVelocity, heading, prediction));
        mapAnchorPane.getChildren().get(4 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
    }

    void editInDVehicle(int index, double x, double y, double length, double width, double xVelocity, double yVelocity,
                        double xAcceleration, double yAcceleration, double heading) {
        editParameters(index, x, y, length, width, xVelocity, yVelocity, xAcceleration, yAcceleration, heading);
        String prediction;
        if (mapAnchorPane.getChildren().get(index + 1) instanceof Rectangle) {
            mapAnchorPane.getChildren().get(index + 1).setRotate(0);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setX((x - length / 2) * xTransformationCoefficient);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setY((y + width / 2) * yTransformationCoefficient);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setHeight(width * xTransformationCoefficient);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setWidth(length * xTransformationCoefficient);
        } else {
            ((Circle) mapAnchorPane.getChildren().get(index + 1)).setCenterX(x * xTransformationCoefficient);
            ((Circle) mapAnchorPane.getChildren().get(index + 1)).setCenterY(y * yTransformationCoefficient);
        }
        mapAnchorPane.getChildren().get(index + 1).setRotate(360 - heading);
        mapAnchorPane.getChildren().set(parametersOfVehiclesAtFrame.size() + index + 1, getSpeedVector(x, y, xVelocity, yVelocity, heading));
        mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
        if ("playback".equals(modeChoiceMenu.getText())) {
            editPrediction(index, "faster");
            mapAnchorPane.getChildren().set(2 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getVectorTextFlow(x, y, xVelocity, yVelocity, "faster"));
            mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
            mapAnchorPane.getChildren().set(3 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, "faaster"));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
        } else {
            prediction = model.getPrediction(
                    UsefulMethods.split(model.getArrayOfNearestNeighbours(getTracksOfFrame()).get(index), ","),
                    getClassOfVehicle(index));
            editPrediction(index, prediction);
            mapAnchorPane.getChildren().set(2 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getVectorTextFlow(x, y, xVelocity, yVelocity, prediction));
            mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
            mapAnchorPane.getChildren().set(3 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, prediction));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
            updatePredictions();
        }
    }

    void addRounDVehicle(double x, double y, double length, double width, double xVelocity, double yVelocity,
                         double xAcceleration, double yAcceleration, double heading, String classOfVehicle) {
        addNewParameters(x, y, length, width, xVelocity, yVelocity, xAcceleration, yAcceleration, heading);
        if ("car".equals(classOfVehicle) || "truck".equals(classOfVehicle) || "van".equals(classOfVehicle) ||
                "bus".equals(classOfVehicle) || "trailer".equals(classOfVehicle)) {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Rectangle(
                    (x - length / 2) * xTransformationCoefficient, (y + width / 2) * yTransformationCoefficient,
                    length * xTransformationCoefficient, width * xTransformationCoefficient));
            if ("car".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(CAR_COLOR);
            } else if ("truck".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(TRUCK_COLOR);
            } else if ("van".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(VAN_COLOR);
            } else if ("bus".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(BUS_COLOR);
            } else {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(TRAILER_COLOR);
            }
            mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        } else {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Circle(
                    x * xTransformationCoefficient, y * yTransformationCoefficient, RADIUS_OF_VRU_CIRCLES));
            if ("pedestrian".equals(classOfVehicle)) {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(PEDESTRIAN_COLOR);
            } else if ("bicycle".equals(classOfVehicle)) {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(BICYCLE_COLOR);
            } else {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(MOTORCYCLE_COLOR);
            }
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        }
        mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
        mapAnchorPane.getChildren().add(2 * parametersOfVehiclesAtFrame.size(), getSpeedVector(x, y, xVelocity, yVelocity, heading));
        mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        if ("playback".equals(modeChoiceMenu.getText())) {
            addPrediction("faster");
            mapAnchorPane.getChildren().add(3 * parametersOfVehiclesAtFrame.size(),
                    getVectorTextFlow(x, y, xVelocity, yVelocity, "faster"));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
            mapAnchorPane.getChildren().add(4 * parametersOfVehiclesAtFrame.size(),
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, "faster"));
            mapAnchorPane.getChildren().get(4 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        } else {
            String prediction = model.getPrediction(
                    UsefulMethods.split(model.getArrayOfNearestNeighbours(getTracksOfFrame()).get(parametersOfVehiclesAtFrame.size() - 1), ","),
                    classOfVehicle);
            addPrediction(prediction);
            mapAnchorPane.getChildren().add(3 * parametersOfVehiclesAtFrame.size(),
                    getVectorTextFlow(x, y, xVelocity, yVelocity, prediction));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
            mapAnchorPane.getChildren().add(4 * parametersOfVehiclesAtFrame.size(),
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, prediction));
            mapAnchorPane.getChildren().get(4 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
            updatePredictions();
        }
    }

    void addRounDVehicle(double x, double y, double length, double width, double xVelocity, double yVelocity,
                         double xAcceleration, double yAcceleration, double heading, String classOfVehicle, String prediction) {
        addNewParameters(x, y, length, width, xVelocity, yVelocity, xAcceleration, yAcceleration, heading);
        if ("car".equals(classOfVehicle) || "truck".equals(classOfVehicle) || "van".equals(classOfVehicle) ||
                "bus".equals(classOfVehicle) || "trailer".equals(classOfVehicle)) {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Rectangle(
                    (x - length / 2) * xTransformationCoefficient, (y + width / 2) * yTransformationCoefficient,
                    length * xTransformationCoefficient, width * xTransformationCoefficient));
            if ("car".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(CAR_COLOR);
            } else if ("truck".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(TRUCK_COLOR);
            } else if ("van".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(VAN_COLOR);
            } else if ("bus".equals(classOfVehicle)) {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(BUS_COLOR);
            } else {
                ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(TRAILER_COLOR);
            }
            mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Rectangle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        } else {
            mapAnchorPane.getChildren().add(parametersOfVehiclesAtFrame.size(), new Circle(
                    x * xTransformationCoefficient, y * yTransformationCoefficient, RADIUS_OF_VRU_CIRCLES));
            if ("pedestrian".equals(classOfVehicle)) {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(PEDESTRIAN_COLOR);
            } else if ("bicycle".equals(classOfVehicle)) {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(BICYCLE_COLOR);
            } else {
                ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setFill(MOTORCYCLE_COLOR);
            }
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeType(StrokeType.OUTSIDE);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStroke(Color.TRANSPARENT);
            ((Circle) mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size())).setStrokeWidth(12);
        }
        mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size()).setRotate(360 - heading);
        mapAnchorPane.getChildren().add(2 * parametersOfVehiclesAtFrame.size(), getSpeedVector(x, y, xVelocity, yVelocity, heading));
        mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        addPrediction(prediction);
        mapAnchorPane.getChildren().add(3 * parametersOfVehiclesAtFrame.size(),
                getVectorTextFlow(x, y, xVelocity, yVelocity, prediction));
        mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
        mapAnchorPane.getChildren().add(4 * parametersOfVehiclesAtFrame.size(),
                getPredictionPolyline(x, y, xVelocity, yVelocity, heading, prediction));
        mapAnchorPane.getChildren().get(4 * parametersOfVehiclesAtFrame.size()).setMouseTransparent(true);
    }

    void editRounDVehicle(int index, double x, double y, double length, double width, double xVelocity, double yVelocity,
                          double xAcceleration, double yAcceleration, double heading) {
        editParameters(index, x, y, length, width, xVelocity, yVelocity, xAcceleration, yAcceleration, heading);
        if (mapAnchorPane.getChildren().get(index + 1) instanceof Rectangle) {
            mapAnchorPane.getChildren().get(index + 1).setRotate(0);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setX((x - length / 2) * xTransformationCoefficient);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setY((y + width / 2) * yTransformationCoefficient);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setHeight(width * xTransformationCoefficient);
            ((Rectangle) mapAnchorPane.getChildren().get(index + 1)).setWidth(length * xTransformationCoefficient);
        } else {
            ((Circle) mapAnchorPane.getChildren().get(index + 1)).setCenterX(x * xTransformationCoefficient);
            ((Circle) mapAnchorPane.getChildren().get(index + 1)).setCenterY(y * yTransformationCoefficient);
        }
        mapAnchorPane.getChildren().get(index + 1).setRotate(360 - heading);
        mapAnchorPane.getChildren().set(parametersOfVehiclesAtFrame.size() + index + 1, getSpeedVector(x, y, xVelocity, yVelocity, heading));
        mapAnchorPane.getChildren().get(parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
        if ("playback".equals(modeChoiceMenu.getText())) {
            editPrediction(index, "faster");
            mapAnchorPane.getChildren().set(2 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getVectorTextFlow(x, y, xVelocity, yVelocity, "faster"));
            mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
            mapAnchorPane.getChildren().set(3 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, "faster"));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
        } else {
            String prediction = model.getPrediction(
                    UsefulMethods.split(model.getArrayOfNearestNeighbours(getTracksOfFrame()).get(index), ","),
                    getClassOfVehicle(index));
            editPrediction(index, prediction);
            mapAnchorPane.getChildren().set(2 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getVectorTextFlow(x, y, xVelocity, yVelocity, prediction));
            mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
            mapAnchorPane.getChildren().set(3 * parametersOfVehiclesAtFrame.size() + index + 1,
                    getPredictionPolyline(x, y, xVelocity, yVelocity, heading, prediction));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size() + index + 1).setMouseTransparent(true);
            updatePredictions();
        }
    }

    Polygon getSpeedVector(double x, double y, double xVelocity, double yVelocity, double heading) {
        heading *= (-Math.PI) / 180;
        Polygon polygon = new Polygon(x * xTransformationCoefficient - 3 * Math.sin(heading),
                y * yTransformationCoefficient + 3 * Math.cos(heading),
                x * xTransformationCoefficient + 3 * Math.sin(heading),
                y * yTransformationCoefficient - 3 * Math.cos(heading),
                x * xTransformationCoefficient + 3 * xVelocity,
                y * yTransformationCoefficient - 3 * yVelocity);
        polygon.setFill(COLOR_OF_SPEED_VECTOR);
        return polygon;
    }

    Polyline getPredictionPolyline(double x, double y, double xVelocity, double yVelocity, double heading, String prediction) {
        heading *= Math.PI / 180;
        heading += Math.PI;
        Polyline polyline = new Polyline();
        polyline.setFill(Color.TRANSPARENT);
        polyline.setStrokeWidth(3);
        polyline.setStroke(COLOR_OF_SHIFT_PREDICTION);
        if ("faster".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient + 1.5 * 3 * xVelocity,
                    y * yTransformationCoefficient - 1.5 * 3 * yVelocity);
        } else if ("slower".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient + 0.5 * 3 * xVelocity,
                    y * yTransformationCoefficient - 0.5 * 3 * yVelocity);
        } else if ("easy-turn-left".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient - 40 * Math.cos(heading + Math.PI / 12),
                    y * yTransformationCoefficient + 40 * Math.sin(heading + Math.PI / 12),
                    x * xTransformationCoefficient - 50 * Math.cos(heading + Math.PI / 6),
                    y * yTransformationCoefficient + 50 * Math.sin(heading + Math.PI / 6)
            );
        } else if ("easy-turn-right".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient - 40 * Math.cos(heading - Math.PI / 12),
                    y * yTransformationCoefficient + 40 * Math.sin(heading - Math.PI / 12),
                    x * xTransformationCoefficient - 50 * Math.cos(heading - Math.PI / 6),
                    y * yTransformationCoefficient + 50 * Math.sin(heading - Math.PI / 6)
            );
        } else if ("turn-left".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient - 20 * Math.cos(heading + Math.PI / 4),
                    y * yTransformationCoefficient + 20 * Math.sin(heading + Math.PI / 4),
                    x * xTransformationCoefficient - 30 * Math.cos(heading + Math.PI / 3),
                    y * yTransformationCoefficient + 30 * Math.sin(heading + Math.PI / 3)
            );
        } else if ("turn-right".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient - 20 * Math.cos(heading - Math.PI / 4),
                    y * yTransformationCoefficient + 20 * Math.sin(heading - Math.PI / 4),
                    x * xTransformationCoefficient - 30 * Math.cos(heading - Math.PI / 3),
                    y * yTransformationCoefficient + 30 * Math.sin(heading - Math.PI / 3)
            );
        } else if ("constant-speed".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient + 3 * xVelocity,
                    y * yTransformationCoefficient - 3 * yVelocity);
        } else if ("still".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient - RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    y * yTransformationCoefficient - RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    x * xTransformationCoefficient + RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    y * yTransformationCoefficient - RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    x * xTransformationCoefficient + RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    y * yTransformationCoefficient + RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    x * xTransformationCoefficient - RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3,
                    y * yTransformationCoefficient + RADIUS_OF_VRU_CIRCLES * Math.sqrt(2) / 3
            );
            polyline.setStroke(Color.TRANSPARENT);
            polyline.setFill(COLOR_OF_SHIFT_PREDICTION);
        } else if ("lane-change-left".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient - 20 * Math.cos(heading),
                    y * yTransformationCoefficient + 20 * Math.sin(heading),
                    x * xTransformationCoefficient - 30 * Math.cos(heading + Math.PI / 10),
                    y * yTransformationCoefficient + 30 * Math.sin(heading + Math.PI / 10),
                    x * xTransformationCoefficient - 35 * Math.cos(heading + Math.PI / 12),
                    y * yTransformationCoefficient + 35 * Math.sin(heading + Math.PI / 12),
                    x * xTransformationCoefficient - 50 * Math.cos(heading + Math.PI / 20),
                    y * yTransformationCoefficient + 50 * Math.sin(heading + Math.PI / 20)
            );
        } else if ("lane-change-right".equals(prediction)) {
            polyline.getPoints().setAll(x * xTransformationCoefficient,
                    y * yTransformationCoefficient,
                    x * xTransformationCoefficient - 20 * Math.cos(heading),
                    y * yTransformationCoefficient + 20 * Math.sin(heading),
                    x * xTransformationCoefficient - 30 * Math.cos(heading - Math.PI / 10),
                    y * yTransformationCoefficient + 30 * Math.sin(heading - Math.PI / 10),
                    x * xTransformationCoefficient - 35 * Math.cos(heading - Math.PI / 12),
                    y * yTransformationCoefficient + 35 * Math.sin(heading - Math.PI / 12),
                    x * xTransformationCoefficient - 50 * Math.cos(heading - Math.PI / 20),
                    y * yTransformationCoefficient + 50 * Math.sin(heading - Math.PI / 20)
            );
        } else {
            polyline.getPoints().setAll(x * xTransformationCoefficient - 5,
                    y * yTransformationCoefficient - 5,
                    x * xTransformationCoefficient - 5,
                    y * yTransformationCoefficient + 5,
                    x * xTransformationCoefficient + 5,
                    y * yTransformationCoefficient - 5,
                    x * xTransformationCoefficient + 5,
                    y * yTransformationCoefficient + 5
            );
        }
        return polyline;
    }

    TextFlow getVectorTextFlow(double x, double y, double xVelocity, double yVelocity, String predictedManoeuvre) {
        Text coordinatesAndVelocity = new Text("x: (" + String.format("%.1f", x) + "; " + String.format("%.1f", y) + ")" +
                System.lineSeparator() + "v: (" + String.format("%.1f", xVelocity) + "; " +
                String.format("%.1f", yVelocity) + ")" + System.lineSeparator());
        coordinatesAndVelocity.setFill(COLOR_OF_VECTOR_TEXT);
        Text prediction = new Text(predictedManoeuvre);
        prediction.setFill(COLOR_OF_PREDICTION_TEXT);
        TextFlow textFlow = new TextFlow(coordinatesAndVelocity, prediction);
        textFlow.setLayoutX(x * xTransformationCoefficient - 20);
        textFlow.setLayoutY(y * yTransformationCoefficient - 50);
        return textFlow;
    }

    String getClassOfVehicle(int index) {
        if (mapAnchorPane.getChildren().get(index + 1) instanceof Rectangle) {
            if (CAR_COLOR.equals(((Rectangle) mapAnchorPane.getChildren().get(index + 1)).getFill())) {
                return "car";
            }
            if (TRUCK_COLOR.equals(((Rectangle) mapAnchorPane.getChildren().get(index + 1)).getFill())) {
                return "inD".equals(model.datasetName) ? "truck_bus" : "truck";
            }
            if (VAN_COLOR.equals(((Rectangle) mapAnchorPane.getChildren().get(index + 1)).getFill())) {
                return "van";
            }
            if (BUS_COLOR.equals(((Rectangle) mapAnchorPane.getChildren().get(index + 1)).getFill())) {
                return "bus";
            }
            return "trailer";
        }
        return PEDESTRIAN_COLOR.equals(((Circle) mapAnchorPane.getChildren().get(index + 1)).getFill()) ? "pedestrian" :
                (BICYCLE_COLOR.equals(((Circle) mapAnchorPane.getChildren().get(index + 1)).getFill()) ? "bicycle" : "motorcycle");
    }

    void updatePredictions() {
        String prediction;
        for (int i = 0; i < parametersOfVehiclesAtFrame.size(); i++) {
            if ("playback".equals(modeChoiceMenu.getText())) {
                prediction = model.getPrediction(
                        UsefulMethods.split(model.getArrayOfNearestNeighbours(model.tracksByFrames.get(currentFrame)).get(i), ","),
                        getClassOfVehicle(i));
            } else {
                prediction = model.getPrediction(
                        UsefulMethods.split(model.getArrayOfNearestNeighbours(getTracksOfFrame()).get(i), ","),
                        getClassOfVehicle(i));
            }
            predictionsOfVehiclesAtFrame.set(i, prediction);
            mapAnchorPane.getChildren().set(2 * parametersOfVehiclesAtFrame.size() + i + 1, getVectorTextFlow(
                    parametersOfVehiclesAtFrame.get(i).get(0), parametersOfVehiclesAtFrame.get(i).get(1),
                    parametersOfVehiclesAtFrame.get(i).get(4), parametersOfVehiclesAtFrame.get(i).get(5),
                    prediction
            ));
            mapAnchorPane.getChildren().get(2 * parametersOfVehiclesAtFrame.size() + i + 1).setMouseTransparent(true);
            mapAnchorPane.getChildren().set(3 * parametersOfVehiclesAtFrame.size() + i + 1,
                    getPredictionPolyline(parametersOfVehiclesAtFrame.get(i).get(0), parametersOfVehiclesAtFrame.get(i).get(1),
                            parametersOfVehiclesAtFrame.get(i).get(4), parametersOfVehiclesAtFrame.get(i).get(5),
                            parametersOfVehiclesAtFrame.get(i).get(8), prediction));
            mapAnchorPane.getChildren().get(3 * parametersOfVehiclesAtFrame.size() + i + 1).setMouseTransparent(true);
        }
    }

    final EventHandler<MouseEvent> addOrEditMouseEventHandler = event -> {
        if ("handtool".equals(selectedTypeOfVehicleToAdd)) {
            return;
        }
        PickResult pickResult = event.getPickResult();
        if (pickResult == null) {
            return;
        }
        Node node = pickResult.getIntersectedNode();
        if (node == null || node instanceof Polygon || node instanceof Text || node instanceof Polyline) {
            return;
        }
        if (node instanceof Rectangle || node instanceof Circle) {
            if (!"selection".equals(selectedTypeOfVehicleToAdd)) {
                return;
            }
            if (node instanceof Rectangle) {
                if (indexOfSelectedVehicle != -1) {
                    if (mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1) instanceof Rectangle) {
                        removeStrokeFromRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                    } else {
                        removeStrokeFromCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                    }
                }
                indexOfSelectedVehicle = mapAnchorPane.getChildren().indexOf(node) - 1;
                addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
            } else {
                if (indexOfSelectedVehicle != -1) {
                    if (mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1) instanceof Rectangle) {
                        removeStrokeFromRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                    } else {
                        removeStrokeFromCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                    }
                }
                indexOfSelectedVehicle = mapAnchorPane.getChildren().indexOf(node) - 1;
                addStrokeToCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
            }
            modificationButton.setDisable(false);
            deletionButton.setDisable(false);
        } else {
            modificationButton.setDisable(true);
            deletionButton.setDisable(true);
            if (indexOfSelectedVehicle != -1) {
                if (mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1) instanceof Rectangle) {
                    removeStrokeFromRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else {
                    removeStrokeFromCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                }
                indexOfSelectedVehicle = -1;
                return;
            }
            if ("selection".equals(selectedTypeOfVehicleToAdd)) {
                return;
            }
            if ("inD".equals(model.datasetName)) {
                if ("car".equals(selectedTypeOfVehicleToAdd)) {
                    addInDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            3.5, 1.7, 0, 0, 0, 0, 0, "car");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("truck".equals(selectedTypeOfVehicleToAdd)) {
                    addInDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            7, 2.5, 0, 0, 0, 0, 0, "truck_bus");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("pedestrian".equals(selectedTypeOfVehicleToAdd)) {
                    addInDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            0, 0, 0, 0, 0, 0, 0, "pedestrian");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("bicycle".equals(selectedTypeOfVehicleToAdd)) {
                    addInDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            0, 0, 0, 0, 0, 0, 0, "bicycle");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                }
            } else {
                if ("car".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            3.5, 1.7, 0, 0, 0, 0, 0, "car");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("truck".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            7.5, 2.5, 0, 0, 0, 0, 0, "truck");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("van".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            4.5, 1.9, 0, 0, 0, 0, 0, "van");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("bus".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            6.5, 2.3, 0, 0, 0, 0, 0, "bus");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("trailer".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            5.5, 2.1, 0, 0, 0, 0, 0, "trailer");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("pedestrian".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            0, 0, 0, 0, 0, 0, 0, "pedestrian");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("bicycle".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            0, 0, 0, 0, 0, 0, 0, "bicycle");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                } else if ("motorcycle".equals(selectedTypeOfVehicleToAdd)) {
                    addRounDVehicle(event.getX() / xTransformationCoefficient,
                            event.getY() / yTransformationCoefficient,
                            0, 0, 0, 0, 0, 0, 0, "motorcycle");
                    indexOfSelectedVehicle = parametersOfVehiclesAtFrame.size() - 1;
                    addStrokeToCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
                }
            }
            selectionButtonOnAction();
        }
    };

    final EventHandler<KeyEvent> deleteKeyEventHandler = event -> {
        if (!KeyCode.DELETE.equals(event.getCode())) {
            return;
        }
        deletionButtonOnAction();
    };

    void removeVehicleSelection() {
        if (indexOfSelectedVehicle != -1) {
            if (mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1) instanceof Rectangle) {
                removeStrokeFromRectangle((Rectangle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
            } else {
                removeStrokeFromCircle((Circle) mapAnchorPane.getChildren().get(indexOfSelectedVehicle + 1));
            }
            indexOfSelectedVehicle = -1;
            modificationButton.setDisable(true);
            deletionButton.setDisable(true);
        }
    }

    void addNewParameters(double x, double y, double length, double width, double xVelocity, double yVelocity,
                          double xAcceleration, double yAcceleration, double heading) {
        parametersOfVehiclesAtFrame.add(new ArrayList<>());
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(x);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(y);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(length);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(width);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(xVelocity);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(yVelocity);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(xAcceleration);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(yAcceleration);
        parametersOfVehiclesAtFrame.get(parametersOfVehiclesAtFrame.size() - 1).add(heading);
    }

    void addPrediction(String prediction) {
        predictionsOfVehiclesAtFrame.add(prediction);
    }

    void removeParameters(int index) {
        parametersOfVehiclesAtFrame.remove(index);
    }

    void removePrediction(int index) {
        predictionsOfVehiclesAtFrame.remove(index);
    }

    void editParameters(int index, double x, double y, double length, double width, double xVelocity, double yVelocity,
                        double xAcceleration, double yAcceleration, double heading) {
        parametersOfVehiclesAtFrame.get(index).set(0, x);
        parametersOfVehiclesAtFrame.get(index).set(1, y);
        parametersOfVehiclesAtFrame.get(index).set(2, length);
        parametersOfVehiclesAtFrame.get(index).set(3, width);
        parametersOfVehiclesAtFrame.get(index).set(4, xVelocity);
        parametersOfVehiclesAtFrame.get(index).set(5, yVelocity);
        parametersOfVehiclesAtFrame.get(index).set(6, xAcceleration);
        parametersOfVehiclesAtFrame.get(index).set(7, yAcceleration);
        parametersOfVehiclesAtFrame.get(index).set(8, heading);
    }

    void editPrediction(int index, String prediction) {
        predictionsOfVehiclesAtFrame.set(index, prediction);
    }

    void addStrokeToRectangle(Rectangle rectangle) {
        rectangle.setStroke(HIGHLIGHT_COLOR);
    }

    void removeStrokeFromRectangle(Rectangle rectangle) {
        rectangle.setStroke(Color.TRANSPARENT);
    }

    void addStrokeToCircle(Circle circle) {
        circle.setStroke(HIGHLIGHT_COLOR);
    }

    void removeStrokeFromCircle(Circle circle) {
        circle.setStroke(Color.TRANSPARENT);
    }

    void initializeAndStartTask() {
        visualizationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (!visualizationTask.isCancelled() && currentFrame < model.maxFrame) {
                    Platform.runLater(() -> currentFrameSlider.setValue(currentFrame + 1));
                    Thread.sleep(visualizationSpeed);
                }
                return null;
            }
        };
        Thread thread = new Thread(visualizationTask);
        thread.setDaemon(false);
        thread.start();
    }

    ArrayList<ArrayList<String>> getTracksOfFrame() {
        ArrayList<ArrayList<String>> tracksInFrame = new ArrayList<>();
        double heading, speed, acceleration;
        double[] angle = new double[2];
        for (int i = 0; i < parametersOfVehiclesAtFrame.size(); i++) {
            tracksInFrame.add(new ArrayList<>());

            tracksInFrame.get(i).add(String.valueOf(model.fileNumber));
            tracksInFrame.get(i).add(String.valueOf(i));
            tracksInFrame.get(i).add(String.valueOf(currentFrame));
            tracksInFrame.get(i).add("0");
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(0).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(1).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(8).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(3).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(2).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(4).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(5).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(6).toString());
            tracksInFrame.get(i).add(parametersOfVehiclesAtFrame.get(i).get(7).toString());

            speed = Math.sqrt(Math.pow(parametersOfVehiclesAtFrame.get(i).get(4), 2) +
                    Math.pow(parametersOfVehiclesAtFrame.get(i).get(5), 2));
            acceleration = Math.sqrt(Math.pow(parametersOfVehiclesAtFrame.get(i).get(6), 2) +
                    Math.pow(parametersOfVehiclesAtFrame.get(i).get(7), 2));
            heading = parametersOfVehiclesAtFrame.get(i).get(8) * Math.PI / 180;
            for (int j = 0; j < 2; j++) {
                if (parametersOfVehiclesAtFrame.get(i).get(4 + 2 * j) == 0) {
                    if (parametersOfVehiclesAtFrame.get(i).get(5 + 2 * j) > 0) {
                        angle[j] = Math.PI / 2;
                    } else if (parametersOfVehiclesAtFrame.get(i).get(5 + 2 * j) < 0) {
                        angle[j] = 3 * Math.PI / 2;
                    } else {
                        angle[j] = heading;
                    }
                } else {
                    angle[j] = Math.atan(parametersOfVehiclesAtFrame.get(i).get(5 + 2 * j) /
                            parametersOfVehiclesAtFrame.get(i).get(4 + 2 * j));
                    if (parametersOfVehiclesAtFrame.get(i).get(4 + 2 * j) < 0) {
                        angle[j] += Math.PI;
                    }
                    if (angle[j] < 0) {
                        angle[j] += 2 * Math.PI;
                    }
                }
            }
            tracksInFrame.get(i).add(String.valueOf(speed * Math.cos(angle[0] - heading)));
            tracksInFrame.get(i).add(String.valueOf(speed * Math.sin(angle[0] - heading)));
            tracksInFrame.get(i).add(String.valueOf(acceleration * Math.cos(angle[1] - heading)));
            tracksInFrame.get(i).add(String.valueOf(acceleration * Math.sin(angle[1] - heading)));
        }
        return tracksInFrame;
    }
}
