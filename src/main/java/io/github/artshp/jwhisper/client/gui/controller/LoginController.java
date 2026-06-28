package io.github.artshp.jwhisper.client.gui.controller;

import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.network.NetworkClient;
import io.github.artshp.jwhisper.client.gui.security.IdentityManager;
import io.github.artshp.jwhisper.client.gui.security.ServerTrustManager;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import io.github.artshp.jwhisper.client.gui.users.UserKeys;
import io.github.artshp.jwhisper.common.exception.WrongPasswordException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Login controller. Responsible for login, register and settings.
 * @see IdentityManager
 */
@Slf4j
public class LoginController {

    /**
     * Scene switcher
     */
    private final SceneSwitcher sceneSwitcher;

    /**
     * State manager
     */
    private final AppStateManager stateManager;

    /**
     * Network client
     */
    private final NetworkClient networkClient;

    /**
     * Username field
     */
    @FXML
    private TextField usernameField;

    /**
     * Password field
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Status label
     */
    @FXML
    private Label statusLabel;

    /**
     * Create a new login controller.
     * @param sceneSwitcher scene switcher
     * @param stateManager state manager
     * @param networkClient network client
     */
    public LoginController(SceneSwitcher sceneSwitcher, AppStateManager stateManager, NetworkClient networkClient) {
        this.sceneSwitcher = sceneSwitcher;
        this.stateManager = stateManager;
        this.networkClient = networkClient;
    }

    /**
     * Handle "login" operation. If successful, redirect to Home screen.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        char[] charPassword = password.toCharArray();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        UserKeys keys;
        LOGGER.info("Key Store is available. Trying to load it...");

        try {
            keys = IdentityManager.loadKeys(charPassword);
        } catch (WrongPasswordException e) {
            LOGGER.error("Wrong password provided.");
            return;
        }
        stateManager.setUserKeys(keys);

        ServerTrustManager serverTrustManager = new ServerTrustManager(charPassword);

        String host = stateManager.getServerHostname();
        int port = stateManager.getServerPort();

        statusLabel.setText("Connecting to server...");

        networkClient.connect(host, port, keys, serverTrustManager).thenRun(() -> {
            boolean isLoggedIn;
            try {
                isLoggedIn = networkClient.login(username, keys.signing().getPrivate());
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Login failed: " + e.getCause().getMessage()));
                return;
            }

            if (!isLoggedIn) {
                Platform.runLater(() -> statusLabel.setText("Login failed"));
                return;
            }

            stateManager.setCurrentUsername(username);

            Platform.runLater(() -> sceneSwitcher.switchTo("/fxml/Home.fxml"));
        }).exceptionally(e -> {
            Platform.runLater(() -> statusLabel.setText("Connection failed: " + e.getCause().getMessage()));
            return null;
        });
    }

    /**
     * Handle "register" operation. If successful, remain on the same screen
     */
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        char[] charPassword = password.toCharArray();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        UserKeys keys;
        LOGGER.info("Key Store is available. Trying to load it...");

        try {
            keys = IdentityManager.loadKeys(charPassword);
        } catch (WrongPasswordException e) {
            LOGGER.error("Wrong password provided.");
            return;
        }
        stateManager.setUserKeys(keys);

        ServerTrustManager serverTrustManager = new ServerTrustManager(charPassword);

        String host = stateManager.getServerHostname();
        int port = stateManager.getServerPort();

        statusLabel.setText("Connecting to server...");
        networkClient.connect(host, port, keys, serverTrustManager).thenRun(() -> {
            boolean isRegistered;
            try {
                isRegistered = networkClient.register(username, keys);
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Register failed: " + e.getCause().getMessage()));
                return;
            }

            if (!isRegistered) {
                Platform.runLater(() -> statusLabel.setText("Register failed"));
                return;
            }

            Platform.runLater(() -> statusLabel.setText("Registered successfully"));
        }).exceptionally(e -> {
            Platform.runLater(() -> statusLabel.setText("Connection failed: " + e.getCause().getMessage()));
            return null;
        });
    }

    /**
     * Handle "open settings" operation. Redirect to settings screen.
     */
    @FXML
    private void handleOpenSettings() {
        sceneSwitcher.switchTo("/fxml/Settings.fxml");
    }
}
