package io.github.artshp.jwhisper.client.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        System.out.println("[GUI] LoginController node elements bounded successfully!");
    }

    @FXML
    private void handleConnect() {
        String input = usernameField.getText();
        statusLabel.setText("Hello, " + (input.isBlank() ? "Whisperer" : input) + "!");
    }
}
