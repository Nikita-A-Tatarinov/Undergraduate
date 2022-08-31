package hse.fcs.se.useful;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class UsefulMethods {

    /**
     * Splits the given toSplit string by the given separator.
     *
     * @param toSplit   String to split by the given separator
     * @param separator Separator for splitting the given string
     * @return {@link ArrayList} with strings, which spell the given string by placing
     * separator between them
     * @throws NullPointerException If either toSplit or separator is null
     */
    public static ArrayList<String> split(@NotNull String toSplit, @NotNull String separator) {
        ArrayList<String> splitString = new ArrayList<>();
        int index;
        while ((index = toSplit.indexOf(separator)) != -1) {
            splitString.add(toSplit.substring(0, index));
            toSplit = toSplit.substring(index + separator.length());
        }
        splitString.add(toSplit);
        return splitString;
    }

    /**
     * Joins the given splitString with the given separator.
     *
     * @param splitString String to join by the given separator
     * @param separator   Separator for joining the given splitString
     * @return {@link String} of the given splitString, joined with the given separator
     * @throws NullPointerException If either splitString or separator is null
     */
    public static String join(@NotNull ArrayList<@NotNull String> splitString, @NotNull String separator) {
        if (splitString.size() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(splitString.get(0));
        for (int i = 1; i < splitString.size(); i++) {
            result.append(separator).append(splitString.get(i));
        }
        return result.toString();
    }

    /**
     * Checks if the given path is an existing directory.
     *
     * @param path Path to check
     * @throws NullPointerException     If the given path is null
     * @throws IllegalArgumentException If the given path does not exist or
     *                                  the given path is not a directory
     */
    public static void checkDirectoryExistence(@NotNull String path) {
        File f = new File(path);
        if (!f.exists()) {
            throw new IllegalArgumentException("The given path does not exist. ");
        }
        if (!f.isDirectory()) {
            throw new IllegalArgumentException("The given path is not a directory. ");
        }
    }

    /**
     * Checks if the given path is an existing file.
     *
     * @param path Path to check
     * @throws NullPointerException     If the given path is null
     * @throws IllegalArgumentException If the given path does not exist or
     *                                  the given path is not a file
     */
    public static void checkFileExistence(@NotNull String path) {
        File f = new File(path);
        if (!f.exists()) {
            throw new IllegalArgumentException("The given path does not exist. ");
        }
        if (!f.isFile()) {
            throw new IllegalArgumentException("The given path is not a file. ");
        }
    }

    /**
     * Shows an {@link Alert} of the given type (alertType) with the given title (title),
     * header text (headerText) and context text (contextText). If the given alert type is
     * {@link javafx.scene.control.Alert.AlertType#CONFIRMATION}, pushing any button leads
     * to closing the alert with no additional actions.
     *
     * @param alertType   Type of the alert to show
     * @param title       Title of the alert to show
     * @param headerText  Header text of the alert to show
     * @param contextText Context text of the alert to show
     */
    public static void showAlert(Alert.AlertType alertType, String title, String headerText, String contextText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        alert.showAndWait();
    }

    /**
     * Shows alert of type {@link javafx.scene.control.Alert.AlertType#CONFIRMATION} with the given title (title),
     * header text (headerText) and context text (contextText). If button of type {@link ButtonType#OK} was pressed,
     * returns true. Otherwise, returns false.
     *
     * @param title       Title of the alert to show
     * @param headerText  Header text of the alert to show
     * @param contextText Context text of the alert to show
     * @return true: if button of {@link ButtonType#OK} was pressed; false: otherwise
     */
    public static boolean showConfirmationAlert(String title, String headerText, String contextText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        Optional<ButtonType> optional = alert.showAndWait();
        return optional.isPresent() && ButtonType.OK.equals(optional.get());
    }

    public static String showTextInputDialog(String title, String headerText, String contextText, String initialText) {
        TextInputDialog textInputDialog = new TextInputDialog(initialText);
        textInputDialog.setTitle(title);
        textInputDialog.setHeaderText(headerText);
        textInputDialog.setContentText(contextText);
        Optional<String> optional = textInputDialog.showAndWait();
        return optional.orElse(null);
    }
}
