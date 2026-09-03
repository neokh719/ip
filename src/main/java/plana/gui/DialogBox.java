package plana.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import plana.command.CommandType;

/**
 * Represents a chat bubble containing a speaker marker and message text.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;

    @FXML
    private Label displayPicture;

    private DialogBox(String text, String avatarText, String avatarStyleClass) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Plana's dialog bubble.", exception);
        }

        dialog.setText(text);
        displayPicture.setText(avatarText);
        displayPicture.getStyleClass().add(avatarStyleClass);
    }

    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    private void changeDialogStyle(CommandType commandType) {
        if (commandType == null) {
            return;
        }

        switch (commandType) {
            case TODO:
            case DEADLINE:
            case EVENT:
                dialog.getStyleClass().add("add-label");
                break;
            case MARK:
            case UNMARK:
                dialog.getStyleClass().add("marked-label");
                break;
            case DELETE:
                dialog.getStyleClass().add("delete-label");
                break;
            default:
                break;
        }
    }

    /**
     * Creates a right-aligned chat bubble for the user's message.
     *
     * @param text user message to display.
     * @return right-aligned user dialog bubble.
     */
    public static DialogBox getUserDialog(String text) {
        return new DialogBox(text, "YOU", "user-avatar");
    }

    /**
     * Creates a left-aligned chat bubble for a Plana response.
     *
     * @param text Plana response to display.
     * @param commandType command type used to style the response bubble.
     * @return left-aligned Plana dialog bubble.
     */
    public static DialogBox getPlanaDialog(String text, CommandType commandType) {
        DialogBox dialogBox = new DialogBox(text, "P", "plana-avatar");
        dialogBox.flip();
        dialogBox.changeDialogStyle(commandType);
        return dialogBox;
    }
}
