package io.github.artshp.jwhisper.client.gui.state;

import io.github.artshp.jwhisper.client.gui.config.ClientConfig;
import io.github.artshp.jwhisper.client.gui.config.ConfigManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Class responsible for application state.
 */
public class AppStateManager {

    /**
     * Application config manager
     */
    private final ConfigManager configManager = new ConfigManager();

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
}
