package io.github.artshp.jwhisper.client.gui.state;

import io.github.artshp.jwhisper.client.gui.config.ClientConfig;
import io.github.artshp.jwhisper.client.gui.config.ConfigManager;
import io.github.artshp.jwhisper.client.gui.event.MessageListener;
import io.github.artshp.jwhisper.client.gui.users.UserKeys;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class responsible for application state.
 */
public class AppStateManager {

    /**
     * Application config manager
     */
    private final ConfigManager configManager = new ConfigManager();

    /**
     * Incoming messages listeners
     */
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();

    /**
     * Current username
     */
    @Getter
    @Setter
    private String currentUsername;

    /**
     * Server hostname
     */
    @Getter
    private String serverHostname;

    /**
     * Server port
     */
    @Getter
    private int serverPort;

    @Getter
    @Setter
    private UserKeys userKeys;

    /**
     * Create a new application state manager.
     */
    public AppStateManager() {
        ClientConfig config = configManager.loadOrCreateDefaultConfig();

        serverHostname = config.hostname();
        serverPort = config.port();
    }

    /**
     * Set server hostname and persist changes.
     * @param hostname hostname
     */
    public void setServerHostname(String hostname) {
        ClientConfig config = new ClientConfig(hostname, serverPort);
        configManager.saveConfig(config);

        this.serverHostname = hostname;
    }

    /**
     * Set server port and persist changes.
     * @param port port
     */
    public void setServerPort(int port) {
        ClientConfig config = new ClientConfig(serverHostname, port);
        configManager.saveConfig(config);

        this.serverPort = port;
    }

    /**
     * Add messages listener to the list to be notified about incoming messages.
     * @param listener listener to be registered
     * @see #dispatchMessage(String, String)
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * Handle incoming message event, notify listeners.
     * @param sender sender username
     * @param message message text
     * @see #addMessageListener(MessageListener)
     */
    public void dispatchMessage(String sender, String message) {
        Platform.runLater(() -> {
            for (MessageListener listener : messageListeners) {
                listener.onMessageReceived(sender, message);
            }
        });
    }
}
