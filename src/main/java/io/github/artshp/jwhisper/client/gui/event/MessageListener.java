package io.github.artshp.jwhisper.client.gui.event;

/**
 * Listener of incoming messages.
 */
public interface MessageListener {

    /**
     * Do action when received incoming message.
     * @param sender message sender
     * @param message message text
     */
    void onMessageReceived(String sender, String message);
}
