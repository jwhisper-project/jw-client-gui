package io.github.artshp.jwhisper.client.gui.navigation;

import io.github.artshp.jwhisper.client.gui.controller.LocalSetupController;
import io.github.artshp.jwhisper.client.gui.controller.LoginController;
import io.github.artshp.jwhisper.client.gui.controller.SettingsController;
import io.github.artshp.jwhisper.client.gui.network.NetworkClient;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Class responsible for switching between scenes.
 */
public class SceneSwitcher {

    /**
     * Application stage
     */
    private final Stage stage;

    /**
     * Application state manager
     */
    private final AppStateManager stateManager;

    /**
     * Application network client
     */
    private final NetworkClient networkClient;

    /**
     * Create a new scene switcher.
     * @param stage stage
     * @param stateManager state manager
     * @param networkClient network client
     */
    public SceneSwitcher(Stage stage, AppStateManager stateManager, NetworkClient networkClient) {
        this.stage = stage;
        this.stateManager = stateManager;
        this.networkClient = networkClient;
    }

    /**
     * Perform seamless scene swap.
     * @param fxmlPath path to {@code FXML} scene configuration
     */
    public void switchTo(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(getControllerFactory());

        try {
            Parent root = loader.load();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root); // seamless scene swap
            }

            stage.sizeToScene();
        } catch (IOException e) {
            throw new IllegalArgumentException("Navigation failure: Failed to render layout", e);
        }
    }

    /**
     * Get controller factory for FXML loader.
     * @return controller
     */
    private Callback<Class<?>, Object> getControllerFactory() {
        return controllerClass -> {
            if (controllerClass == LocalSetupController.class) {
                return new LocalSetupController(this);
            } else if (controllerClass == LoginController.class) {
                return new LoginController(this, stateManager, networkClient);
            } else if (controllerClass == SettingsController.class) {
                return new SettingsController(this, stateManager);
            }

            throw new IllegalArgumentException("Unknown controller class type: " + controllerClass);
        };
    }
}
