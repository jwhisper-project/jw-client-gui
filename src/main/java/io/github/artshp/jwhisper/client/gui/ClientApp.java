package io.github.artshp.jwhisper.client.gui;

import atlantafx.base.theme.PrimerDark;
import io.github.artshp.jwhisper.client.gui.config.ClientConfig;
import io.github.artshp.jwhisper.client.gui.config.ConfigManager;
import io.github.artshp.jwhisper.client.gui.navigation.SceneSwitcher;
import io.github.artshp.jwhisper.client.gui.network.NetworkClient;
import io.github.artshp.jwhisper.client.gui.security.IdentityManager;
import io.github.artshp.jwhisper.client.gui.security.ServerTrustManager;
import io.github.artshp.jwhisper.client.gui.state.AppStateManager;
import io.github.artshp.jwhisper.client.gui.users.UserKeys;
import io.github.artshp.jwhisper.common.crypto.CertUtils;
import io.github.artshp.jwhisper.common.crypto.PasswordUtils;
import io.github.artshp.jwhisper.common.exception.InputRetryException;
import io.github.artshp.jwhisper.common.exception.NetworkServiceException;
import io.github.artshp.jwhisper.common.exception.WrongPasswordException;
import io.github.artshp.jwhisper.common.io.ConsoleUtils;
import io.github.artshp.jwhisper.common.io.UserInputUtils;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Client app class.
 */
@Slf4j
public class ClientApp extends Application {

    /**
     * Config file manager.
     */
    private final ConfigManager configManager = new ConfigManager();

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

        SceneSwitcher sceneSwitcher = new SceneSwitcher(primaryStage, stateManager, networkClient);
        sceneSwitcher.switchTo("/fxml/LocalSetup.fxml");

        primaryStage.setTitle("JWhisper Secure Messenger");
        primaryStage.show();

        if (IdentityManager.isKeyStoreAvailable()) {
            sceneSwitcher.switchTo("/fxml/Login.fxml");
        } else {
            sceneSwitcher.switchTo("/fxml/LocalSetup.fxml");
        }
    }

    /**
     * Start client application.
     * @throws InputRetryException if user failed to provide required value
     * @throws NetworkServiceException if failed to register user
     */
    @Deprecated
    public void start() throws InputRetryException, NetworkServiceException {
        LOGGER.info("Starting Client App");
        System.out.println("----- JWhisper Client -----");

        String username = UserInputUtils.readUsername();

        UserKeys keys;
        char[] password;
        if (IdentityManager.isKeyStoreAvailable()) {
            LOGGER.info("Key Store is available. Trying to load it...");

            password = UserInputUtils.readPassword();
            try {
                keys = IdentityManager.loadKeys(password);
            } catch (WrongPasswordException e) {
                LOGGER.error("Wrong password provided.");
                return;
            }
        } else {
            LOGGER.info("Key Store is not available. Creating it...");

            password = UserInputUtils.readNewPassword();
            keys = IdentityManager.createKeys(password, username);
        }

        LOGGER.info("Identity loaded. Signing key fingerprint: {}",
                CertUtils.getFingerprint(keys.signing().getPublic())
        );
        LOGGER.info("Encryption key fingerprint: {}",
                CertUtils.getFingerprint(keys.encryption().getPublic())
        );

        ClientConfig config;
        if (!configManager.isConfigPresent()) {
            LOGGER.debug("No config present. Creating it...");

            String hostname = UserInputUtils.readHostname();
            int port = UserInputUtils.readPort();

            config = new ClientConfig(hostname, port);
            configManager.saveConfig(config);
        } else {
            LOGGER.debug("Config present. Loading it...");
            config = configManager.loadConfig();
        }

        LOGGER.info("Used config: {}", config.toPrettyString());

        ServerTrustManager serverTrustManager = new ServerTrustManager(password);
        if (UserInputUtils.askYesNo("Do you want to add server's certificate?")) {
            Optional<X509Certificate> certificateOptional = UserInputUtils.readCertificate();
            if (certificateOptional.isEmpty()) {
                LOGGER.error("Failed to load certificate.");
                return;
            }

            X509Certificate certificate = certificateOptional.get();
            serverTrustManager.addTrustedCertificate(certificate);
        }
        password = PasswordUtils.cleanPassword(password);

        try (NetworkClient client = new NetworkClient(/*serverTrustManager, keys, config.hostname(), config.port()*/)) {
            var future = client.connect(null, -1, null, null);
            future.join();

            if (UserInputUtils.askYesNo("Register?")) {
                if (!client.register(username, keys)) {
                    throw new NetworkServiceException("Failed to register your user.");
                }
            }

            if (UserInputUtils.askYesNo("Login?")) {
                if (!client.login(username, keys.signing().getPrivate())) {
                    throw new NetworkServiceException("Failed to login.");
                }
            }

            try {
                String targetUsername = ConsoleUtils.readString("Whom to send message?", s -> !s.isBlank(), "f", 1);
                String plainText = ConsoleUtils.readString("Message: ", s -> !s.isBlank(), "f", 1);

                boolean success = client.sendDirectMessage(username, keys.signing().getPrivate(), targetUsername, plainText);
                if (success) {
                    LOGGER.info("Message sent successfully");
                } else {
                    LOGGER.error("Failed to send message");
                }

                /*var optionalUserPublicKeys = client.requestUserPublicKeys(targetUsername);

                if (optionalUserPublicKeys.isPresent()) {
                    var userPublicKeys = optionalUserPublicKeys.get();
                    String signingKeyDigest = CertUtils.getFingerprint(userPublicKeys.signing());
                    String encryptionKeyDigest = CertUtils.getFingerprint(userPublicKeys.encryption());

                    System.out.println("Signing key fingerprint: " + signingKeyDigest);
                    System.out.println("Encryption key fingerprint: " + encryptionKeyDigest);
                } else {
                    System.out.println("Failed to request public keys.");
                }*/
            } catch (InputRetryException e) {
                System.out.println("Failed: " + e.getMessage());
            }

            if (UserInputUtils.askYesNo("Logout?")) {
                if (!client.logout()) {
                    throw new NetworkServiceException("Failed to logout.");
                }
            }

            /*
            CommunicationManager communicationManager = new CommunicationManager(
                    username,
                    keys,
                    client
            );
            communicationManager.start();*/

            LOGGER.info("Goodbye!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
