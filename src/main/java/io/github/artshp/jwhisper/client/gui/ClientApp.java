package io.github.artshp.jwhisper.client.gui;

import atlantafx.base.theme.PrimerDark;
import io.github.artshp.jwhisper.client.gui.controller.HomeController;
import io.github.artshp.jwhisper.client.gui.controller.LocalSetupController;
import io.github.artshp.jwhisper.client.gui.controller.LoginController;
import io.github.artshp.jwhisper.client.gui.controller.SettingsController;
import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.network.NetworkClient;
import io.github.artshp.jwhisper.client.gui.security.IdentityManager;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Client app class.
 */
@Slf4j
public class ClientApp extends Application {

    /**
     * State manager
     */
    private final AppStateManager stateManager = new AppStateManager();

    /**
     * Network client
     */
    private final NetworkClient networkClient = new NetworkClient();

    /**
     * Constructs a new client application.
     */
    public ClientApp() {
    }

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting Client App");
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/app-icon.png"))
        ));

        SceneSwitcher sceneSwitcher = new SceneSwitcher(primaryStage);
        sceneSwitcher.registerController(LocalSetupController.class, () ->
                new LocalSetupController(sceneSwitcher, stateManager));
        sceneSwitcher.registerController(LoginController.class, () ->
                new LoginController(sceneSwitcher, stateManager, networkClient));
        sceneSwitcher.registerController(SettingsController.class, () ->
                new SettingsController(sceneSwitcher, stateManager));
        sceneSwitcher.registerController(HomeController.class, () ->
                new HomeController(sceneSwitcher, stateManager, networkClient));

        sceneSwitcher.switchTo("/fxml/LocalSetup.fxml");

        primaryStage.setTitle("JWhisper Secure Messenger");
        primaryStage.show();

        if (IdentityManager.isKeyStoreAvailable()) {
            sceneSwitcher.switchTo("/fxml/Login.fxml");
        } else {
            sceneSwitcher.switchTo("/fxml/LocalSetup.fxml");
        }
    }
}
