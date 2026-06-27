package io.github.artshp.jwhisper.client.gui;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

/**
 * Client app entry point.
 */
@Slf4j
final class Main {

    /**
     * Constructor to prohibit instantiating.
     */
    private Main() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Client app entry point.
     */
    static void main() {
        try {
            Application.launch(ClientApp.class);
        } catch (Exception e) {
            LOGGER.error("Unexpected error:", e);
        }
    }
}
