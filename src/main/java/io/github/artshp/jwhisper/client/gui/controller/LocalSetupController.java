package io.github.artshp.jwhisper.client.gui.controller;

import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.security.IdentityManager;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import io.github.artshp.jwhisper.client.gui.users.UserKeys;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Local setup controller. Corresponds to creating user's identity.
 * @see IdentityManager
 */
@Slf4j
public class LocalSetupController {

    /**
     * Scene switcher
     */
    private final SceneSwitcher sceneSwitcher;

    /**
     * State manager
     */
    private final AppStateManager stateManager;

    /**
     * Password field
     */
    @FXML
    private PasswordField masterPasswordField;

    /**
     * Confirmation password field
     */
    @FXML
    private PasswordField confirmPasswordField;

    /**
     * Label for error messages
     */
    @FXML
    private Label errorLabel;

    /**
     * Create a new local setup controller
     * @param sceneSwitcher scene switcher
     * @param appStateManager state manager
     */
    public LocalSetupController(SceneSwitcher sceneSwitcher, AppStateManager appStateManager) {
        this.sceneSwitcher = sceneSwitcher;
        this.stateManager = appStateManager;
    }

    /**
     * Handle "generate identity" operation.
     */
    @FXML
    private void handleGenerateIdentity() {
        String password = masterPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (password.isEmpty()) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        char[] charPassword = password.toCharArray();
        try {
            LOGGER.info("Key Store is not available. Creating it...");

            UserKeys keys = IdentityManager.createKeys(charPassword, UUID.randomUUID().toString());
            stateManager.setUserKeys(keys);

            sceneSwitcher.switchTo("/fxml/Login.fxml");
        } catch (Exception e) {
            errorLabel.setText("Key generation failed: " + e.getMessage());
        }
    }
}
