package plana.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import plana.Plana;

/**
 * Controls Plana's main chat window and forwards user input to Plana's core logic.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private Plana plana;

    /**
     * Initializes automatic scrolling after the FXML fields have been injected.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Injects the Plana instance used by this window and displays the greeting.
     *
     * @param plana Plana instance that processes chat commands.
     */
    public void setPlana(Plana plana) {
        this.plana = plana;
        dialogContainer.getChildren().addAll(
                DialogBox.getPlanaBannerDialog(Plana.getWelcomeBanner()),
                DialogBox.getPlanaDialog(Plana.getWelcomeGreeting(), null)
        );
    }

    /**
     * Processes the text field contents and appends both sides of the conversation.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.isBlank() || plana == null) {
            return;
        }

        Plana.Response response = plana.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input),
                DialogBox.getPlanaDialog(response.text(), response.commandType(), response.error())
        );
        userInput.clear();

        if (response.exit()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
            PauseTransition exitDelay = new PauseTransition(Duration.seconds(0.8));
            exitDelay.setOnFinished(event -> Platform.exit());
            exitDelay.play();
        }
    }
}
