package io.github.artshp.jwhisper.client.gui.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Class responsible for switching between scenes.
 */
public class SceneSwitcher {

    /**
     * Controllers registry
     */
    private final HashMap<Class<?>, Supplier<?>> controllers = new HashMap<>();

    /**
     * Application stage
     */
    private final Stage stage;

    /**
     * Create a new scene switcher.
     * @param stage stage
     */
    public SceneSwitcher(Stage stage) {
        this.stage = stage;
    }

    /**
     * Registers a controller class with its constructor factory.
     * @param controllerClass controller class
     * @param controllerSupplier supplier providing new instance of controller
     * @param <T> controller type
     */
    public <T> void registerController(Class<T> controllerClass, Supplier<T> controllerSupplier) {
        controllers.put(controllerClass, controllerSupplier);
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
            Supplier<?> supplier = controllers.get(controllerClass);
            if (supplier != null) {
                return supplier.get();
            }
            throw new IllegalArgumentException(
                    "No controller factory registered for type: " + controllerClass.getName()
            );
        };
    }
}
