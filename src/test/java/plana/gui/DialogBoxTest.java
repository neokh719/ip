package plana.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import plana.command.CommandType;

/**
 * Tests the message presentation created for Plana's JavaFX conversation view.
 */
class DialogBoxTest {
    private static final long TOOLKIT_START_TIMEOUT_SECONDS = 10;

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch startupComplete = new CountDownLatch(1);
        try {
            Platform.startup(startupComplete::countDown);
        } catch (IllegalStateException exception) {
            startupComplete.countDown();
        }
        assertTrue(startupComplete.await(TOOLKIT_START_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @AfterAll
    static void stopToolkit() {
        Platform.exit();
    }

    @Test
    void getUserDialog_compactRightAlignedMessageCreated() throws Exception {
        onFxThread(() -> {
            DialogBox userDialog = DialogBox.getUserDialog("todo buy milk");
            Label message = (Label) userDialog.getChildren().get(0);

            assertEquals(Pos.TOP_RIGHT, userDialog.getAlignment());
            assertEquals(1, userDialog.getChildren().size());
            assertTrue(message.getStyleClass().contains("user-label"));

            userDialog.resize(900, 100);
            assertEquals(420, message.getMaxWidth());
            return null;
        });
    }

    @Test
    void getPlanaDialog_responseAndErrorUseDistinctPresentation() throws Exception {
        onFxThread(() -> {
            DialogBox responseDialog = DialogBox.getPlanaDialog("Task added", CommandType.TODO);
            Label responseAvatar = (Label) responseDialog.getChildren().get(0);
            Label responseMessage = (Label) responseDialog.getChildren().get(1);
            DialogBox errorDialog = DialogBox.getPlanaDialog("Invalid command", CommandType.UNKNOWN, true);
            Label errorAvatar = (Label) errorDialog.getChildren().get(0);
            Label errorMessage = (Label) errorDialog.getChildren().get(1);

            assertEquals(Pos.TOP_LEFT, responseDialog.getAlignment());
            assertEquals("P", responseAvatar.getText());
            assertTrue(responseMessage.getStyleClass().contains("plana-label"));
            assertTrue(responseMessage.getStyleClass().contains("add-label"));

            responseDialog.resize(900, 100);
            assertEquals(620, responseMessage.getMaxWidth());
            assertEquals("!", errorAvatar.getText());
            assertTrue(errorAvatar.getStyleClass().contains("error-avatar"));
            assertTrue(errorMessage.getStyleClass().contains("error-label"));
            return null;
        });
    }

    private <T> T onFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get();
    }
}
