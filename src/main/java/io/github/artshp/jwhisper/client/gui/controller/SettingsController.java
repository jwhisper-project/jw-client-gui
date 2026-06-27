package io.github.artshp.jwhisper.client.gui.controller;

import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.security.ServerTrustManager;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import io.github.artshp.jwhisper.common.crypto.CertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Settings controller.
 */
@Slf4j
public class SettingsController {

    /**
     * Scene switcher
     */
    private final SceneSwitcher sceneSwitcher;

    /**
     * State manager
     */
    private final AppStateManager stateManager;

    /**
     * Hostname field
     */
    @FXML
    private TextField hostnameField;

    /**
     * Port field
     */
    @FXML
    private TextField portField;

    /**
     * Certificate field
     */
    @FXML
    private TextArea pemCertificateField;

    /**
     * Password field
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Create a new settings controller.
     * @param sceneSwitcher scene switcher
     * @param stateManager state manager
     */
    public SettingsController(SceneSwitcher sceneSwitcher, AppStateManager stateManager) {
        this.sceneSwitcher = sceneSwitcher;
        this.stateManager = stateManager;
    }

    /**
     * Initialize controller.
     */
    @FXML
    private void initialize() {
        hostnameField.setText(stateManager.getServerHostname());
        portField.setText(String.valueOf(stateManager.getServerPort()));
    }

    /**
     * Handle "import certificate" operation.
     * Tries to import provided certificate to truststore.
     * @see ServerTrustManager
     */
    @FXML
    private void handleImportCertificate() {
        String pemString = pemCertificateField.getText();
        String password = passwordField.getText();
        if (pemString == null || pemString.isBlank() || password.isBlank()) return;

        char[] charPassword = password.toCharArray();
        ServerTrustManager serverTrustManager = new ServerTrustManager(charPassword);

        Optional<X509Certificate> certificateOptional = CertUtils.parsePemCertificate(pemString);
        if (certificateOptional.isEmpty()) {
            LOGGER.error("Failed to load certificate.");
            return;
        }

        X509Certificate certificate = certificateOptional.get();
        serverTrustManager.addTrustedCertificate(certificate);

        pemCertificateField.clear();
        passwordField.clear();
    }

    /**
     * Handle "save settings" operation. Persist updated values.
     */
    @FXML
    private void handleSave() {
        stateManager.setServerHostname(hostnameField.getText().trim());
        try {
            stateManager.setServerPort(Integer.parseInt(portField.getText().trim()));
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Handle "return" operation. Return back to the login screen.
     */
    @FXML
    private void handleReturn() {
        sceneSwitcher.switchTo("/fxml/Login.fxml");
    }
}
