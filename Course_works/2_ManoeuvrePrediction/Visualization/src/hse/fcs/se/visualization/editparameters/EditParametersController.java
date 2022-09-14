package hse.fcs.se.visualization.editparameters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EditParametersController implements Initializable {

    @FXML
    private Text xText;
    @FXML
    private Text yText;
    @FXML
    private Text widthText;
    @FXML
    private Text lengthText;
    @FXML
    private Text xVelocityText;
    @FXML
    private Text yVelocityText;
    @FXML
    private Text xAccelerationText;
    @FXML
    private Text yAccelerationText;
    @FXML
    private Text headingText;
    @FXML
    private TextField xTextField;
    @FXML
    private TextField yTextField;
    @FXML
    private TextField widthTextField;
    @FXML
    private TextField lengthTextField;
    @FXML
    private TextField xVelocityTextField;
    @FXML
    private TextField yVelocityTextField;
    @FXML
    private TextField xAccelerationTextField;
    @FXML
    private TextField yAccelerationTextField;
    @FXML
    private TextField headingTextField;
    @FXML
    private Text xHintText;
    @FXML
    private Text yHintText;
    @FXML
    private Text widthHintText;
    @FXML
    private Text lengthHintText;
    @FXML
    private Text xVelocityHintText;
    @FXML
    private Text yVelocityHintText;
    @FXML
    private Text xAccelerationHintText;
    @FXML
    private Text yAccelerationHintText;
    @FXML
    private Text headingHintText;
    @FXML
    private Button submitButton;

    public static Double x;
    public static Double y;
    public static Double width;
    public static Double length;
    public static Double xVelocity;
    public static Double yVelocity;
    public static Double xAcceleration;
    public static Double yAcceleration;
    public static Double heading;

    public static void getStaticFields(double newX, double newY, double newWidth, double newLength,
                                       double newXVelocity, double newYVelocity,
                                       double newXAcceleration, double newYAcceleration, double newHeading) {
        x = newX;
        y = newY;
        width = newWidth;
        length = newLength;
        xVelocity = newXVelocity;
        yVelocity = newYVelocity;
        xAcceleration = newXAcceleration;
        yAcceleration = newYAcceleration;
        heading = newHeading;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        yTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        widthTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        lengthTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        xVelocityTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        yVelocityTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        xAccelerationTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        yAccelerationHintText.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        headingTextField.textProperty().addListener((observable, oldValue, newValue) -> checkAllTextFields());
        xTextField.setText(String.valueOf(x));
        yTextField.setText(String.valueOf(y));
        widthTextField.setText(String.valueOf(width));
        lengthTextField.setText(String.valueOf(length));
        xVelocityTextField.setText(String.valueOf(xVelocity));
        yVelocityTextField.setText(String.valueOf(yVelocity));
        xAccelerationTextField.setText(String.valueOf(xAcceleration));
        yAccelerationTextField.setText(String.valueOf(yAcceleration));
        headingTextField.setText(String.valueOf(heading));
    }

    public void submitButtonOnAction() {
        x = Double.parseDouble(xTextField.getText());
        y = Double.parseDouble(yTextField.getText());
        width = Double.parseDouble(widthTextField.getText());
        length = Double.parseDouble(lengthTextField.getText());
        xVelocity = Double.parseDouble(xVelocityTextField.getText());
        yVelocity = Double.parseDouble(yVelocityTextField.getText());
        xAcceleration = Double.parseDouble(xAccelerationTextField.getText());
        yAcceleration = Double.parseDouble(yAccelerationTextField.getText());
        heading = Double.parseDouble(headingTextField.getText());
        ((Stage) submitButton.getScene().getWindow()).close();
    }

    private void checkAllTextFields() {
        submitButton.setDisable(!checkTextField(xTextField, xHintText) || !checkTextField(yTextField, yHintText) ||
                !checkTextField(widthTextField, widthHintText) || !checkTextField(lengthTextField, lengthHintText) ||
                !checkTextField(xVelocityTextField, xVelocityHintText) ||
                !checkTextField(yVelocityTextField, yVelocityHintText) ||
                !checkTextField(xAccelerationTextField, xAccelerationHintText) ||
                !checkTextField(yAccelerationTextField, yAccelerationHintText) ||
                !checkTextField(headingTextField, headingHintText));
    }

    private boolean checkTextField(TextField textField, Text hintText) {
        try {
            Double.parseDouble(textField.getText());
        } catch (NumberFormatException e) {
            hintText.setText("Cannot be converted to double. ");
            return false;
        }
        hintText.setText("");
        return true;
    }
}
