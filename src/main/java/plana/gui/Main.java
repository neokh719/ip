package plana.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import plana.Plana;

/**
 * Starts Plana's FXML-based JavaFX interface.
 */
public class Main extends Application {
    /**
     * Loads and displays Plana's main window.
     *
     * @param stage the primary JavaFX stage.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane mainWindow = fxmlLoader.load();
            MainWindow controller = fxmlLoader.getController();
            controller.setPlana(new Plana());

            Scene scene = new Scene(mainWindow);
            stage.setTitle("Plana");
            stage.setScene(scene);
            stage.setMinHeight(380);
            stage.setMinWidth(520);
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Plana's GUI.", exception);
        }
    }
}
