package io.github.artshp.jwhisper.client.gui.controller;

import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.security.ServerTrustManager;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import io.github.artshp.jwhisper.client.gui.util.DialogFactory;
import io.github.artshp.jwhisper.common.crypto.CertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
     * Path or name of selected certificate file
     */
    @FXML
    private TextField filePathField;

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
     * Handle "browse certificate" operation. Read certificate from file and parse it.
     */
    @FXML
    private void handleBrowseCertificateFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select server certificate");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Certificate Files", "*.crt", "*.pem", "*.cer"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage currentStage = (Stage) filePathField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile != null) {
            try {
                filePathField.setText(selectedFile.getName());

                String pemContent = Files.readString(selectedFile.toPath());
                pemCertificateField.setText(pemContent);
            } catch (IOException e) {
                DialogFactory.createAlert(
                        Alert.AlertType.ERROR,
                        "File read error",
                        "Could not process certificate asset file",
                        e.getMessage()
                ).showAndWait();
            }
        }
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
        ServerTrustManager serverTrustManager;
        try {
            serverTrustManager = new ServerTrustManager(charPassword);
        } catch (Exception e) {
            DialogFactory.createAlert(
                    Alert.AlertType.ERROR,
                    "Import failed",
                    "Failed to open truststore",
                    e.getMessage()
            ).showAndWait();
            return;
        }

        Optional<X509Certificate> certificateOptional = CertUtils.parsePemCertificate(pemString);
        if (certificateOptional.isEmpty()) {
            LOGGER.error("Failed to load certificate.");

            DialogFactory.createAlert(
                    Alert.AlertType.ERROR,
                    "Import failed",
                    null,
                    "Provided certificate is not a valid certificate."
            ).showAndWait();
            return;
        }

        X509Certificate certificate = certificateOptional.get();
        serverTrustManager.addTrustedCertificate(certificate);

        pemCertificateField.clear();
        filePathField.clear();
        passwordField.clear();

        DialogFactory.createAlert(
                Alert.AlertType.INFORMATION,
                "Import successful",
                null,
                "The certificate was successfully added to truststore."
        ).showAndWait();
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
