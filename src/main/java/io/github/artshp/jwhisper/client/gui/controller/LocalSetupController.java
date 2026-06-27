package io.github.artshp.jwhisper.client.gui.controller;

import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.security.IdentityManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

/**
 * Local setup controller. Corresponds to creating user's identity.
 * @see IdentityManager
 */
public class LocalSetupController {

    /**
     * Scene switcher
     */
    private final SceneSwitcher sceneSwitcher;

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
     */
    public LocalSetupController(SceneSwitcher sceneSwitcher) {
        this.sceneSwitcher = sceneSwitcher;
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

        try {
            // TODO: Call identity manager
            sceneSwitcher.switchTo("/fxml/Login.fxml");
        } catch (Exception e) {
            errorLabel.setText("Key generation failed: " + e.getMessage());
        }
    }
}
