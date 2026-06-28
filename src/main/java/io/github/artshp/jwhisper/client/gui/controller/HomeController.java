package io.github.artshp.jwhisper.client.gui.controller;

import io.github.artshp.jwhisper.client.gui.model.UiMessage;
import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.network.NetworkClient;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import io.github.artshp.jwhisper.client.gui.users.UserKeys;
import io.github.artshp.jwhisper.common.exception.NetworkServiceException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * Home screen controller. Responsible for chats with users.
 */
@Slf4j
public class HomeController {

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
     * Track lists of messages linked to usernames
     */
    private final Map<String, List<UiMessage>> conversationLedger = new HashMap<>();

    /**
     * List of active usernames in UI
     */
    private final ObservableList<String> activeContacts = FXCollections.observableArrayList();

    /**
     * List view of contacts
     */
    @FXML
    private ListView<String> contactsListView;

    /**
     * Label with current user username
     */
    @FXML
    private Label currentUserLabel;

    /**
     * Label with username of the user with whom chat is open at the moment
     */
    @FXML
    private Label activeChatPartnerLabel;

    /**
     * Container with messages from current chat
     */
    @FXML
    private VBox messageContainer;

    /**
     * Scroll pane with messages
     */
    @FXML
    private ScrollPane timelineScrollPane;

    /**
     * Input field with message
     */
    @FXML
    private TextField messageInputField;

    /**
     * Chat area
     */
    @FXML
    private VBox chatCanvasArea;

    /**
     * Create a new controller.
     * @param sceneSwitcher scene switcher
     * @param stateManager state manager
     * @param networkClient network client
     */
    public HomeController(SceneSwitcher sceneSwitcher, AppStateManager stateManager, NetworkClient networkClient) {
        this.sceneSwitcher = sceneSwitcher;
        this.stateManager = stateManager;
        this.networkClient = networkClient;
    }

    /**
     * Initialize controller.
     */
    @FXML
    private void initialize() {
        currentUserLabel.setText("@" + stateManager.getCurrentUsername());
        contactsListView.setItems(activeContacts);
        chatCanvasArea.setDisable(true); // disable input line until a user is explicitly clicked

        // Switch conversations when a name in the list is selected
        contactsListView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                renderActiveChatTimeline(newValue);
            }
        });

        // Register a listener for incoming messages
        stateManager.addMessageListener((sender, message) -> {
            UiMessage incoming = new UiMessage(sender, message, false);
            appendMessageToLedger(sender, incoming);
        });
    }

    /**
     * Add message to ledger (registry) of messages, i.e. to the chat with corresponding user.
     * @param username user username
     * @param message message
     */
    private void appendMessageToLedger(String username, UiMessage message) {
        conversationLedger.computeIfAbsent(username, _ -> {
            Platform.runLater(() -> {
                if (!activeContacts.contains(username)) activeContacts.add(username);
            });
            return new ArrayList<>();
        }).add(message);

        // If the message is from the currently active chat partner, render it immediately
        String currentSelection = contactsListView.getSelectionModel().getSelectedItem();
        if (username.equals(currentSelection)) {
            Platform.runLater(() -> renderSingleBubble(message));
        }
    }

    /**
     * Render from scratch active char. Happens after switching between chats.
     * @param username user username
     */
    private void renderActiveChatTimeline(String username) {
        chatCanvasArea.setDisable(false);
        activeChatPartnerLabel.setText("Chatting with @" + username);
        messageContainer.getChildren().clear();

        List<UiMessage> history = conversationLedger.getOrDefault(username, Collections.emptyList());
        for (UiMessage uiMessage : history) {
            renderSingleBubble(uiMessage);
        }

        // Auto-scroll timeline downward to show the newest messages
        timelineScrollPane.setVvalue(1.0);
    }

    /**
     * Render single bubble with a message.
     * @param uiMessage message to render
     */
    private void renderSingleBubble(UiMessage uiMessage) {
        Label textLabel = new Label(uiMessage.message());
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(300);

        HBox bubbleWrapper = new HBox(textLabel);
        bubbleWrapper.setPadding(new Insets(8, 14, 8, 14));
        bubbleWrapper.getStyleClass().add("card");

        HBox alignmentWrapper = new HBox(bubbleWrapper);
        alignmentWrapper.setMaxWidth(Double.MAX_VALUE);

        if (uiMessage.isFromMe()) {
            alignmentWrapper.setAlignment(Pos.CENTER_RIGHT);

            bubbleWrapper.setStyle(
                    "-fx-background-color: #2f80ed; " +
                    "-fx-background-radius: 14 14 2 14;"
            );
            textLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        } else {
            alignmentWrapper.setAlignment(Pos.CENTER_LEFT);

            bubbleWrapper.setStyle(
                    "-fx-background-color: #2d3139; " +
                    "-fx-border-color: #414754; " +
                    "-fx-border-radius: 14 14 14 2; " +
                    "-fx-background-radius: 14 14 14 2;"
            );
            textLabel.setStyle("-fx-text-fill: #f0f2f5;");
        }

        messageContainer.getChildren().add(alignmentWrapper);
    }

    /**
     * Handle "send message" operation. Send message to recipient and show it in UI.
     */
    @FXML
    private void handleSendMessage() {
        String input = messageInputField.getText().trim();
        String recipient = contactsListView.getSelectionModel().getSelectedItem();
        if (input.isEmpty() || recipient == null) return;

        String username = stateManager.getCurrentUsername();
        UserKeys keys = stateManager.getUserKeys();

        boolean success;
        try {
            success = networkClient.sendDirectMessage(username, keys.signing().getPrivate(), recipient, input);
        } catch (IOException | NetworkServiceException e) {
            LOGGER.error("Failed to send message", e);
            return;
        }

        if (!success) {
            LOGGER.error("Failed to send message");
            return;
        }

        UiMessage outgoing = new UiMessage(username, input, true);
        appendMessageToLedger(recipient, outgoing);
        messageInputField.clear();

        timelineScrollPane.setVvalue(1.0);
    }

    /**
     * Handle "new chat" operation. Add created dialog to dialog list.
     */
    @FXML
    private void handleNewChatDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("Initiate encrypted session");
        dialog.setContentText("Enter recipient username:");

        dialog.showAndWait().ifPresent(username -> {
            String cleanName = username.trim();
            // TODO: check if user is in registry
            if (!cleanName.isBlank() && !activeContacts.contains(cleanName)) {
                activeContacts.add(cleanName);
                contactsListView.getSelectionModel().select(cleanName);
            }
        });
    }

    /**
     * Handle "logout" operation. Disconnect from server, go to the login screen.
     * @throws IOException if an I/O error occurs when closing the web socket
     */
    @FXML
    private void handleLogout() throws IOException {
        networkClient.close();
        stateManager.setCurrentUsername(null);
        conversationLedger.clear();
        activeContacts.clear();
        sceneSwitcher.switchTo("/fxml/Login.fxml");
    }
}
