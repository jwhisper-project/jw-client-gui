package io.github.artshp.jwhisper.client.gui.model;

/**
 * Message wrapper for UI.
 * @param sender sender username
 * @param message message text
 * @param isFromMe is sender of the message me or somebody else?
 */
public record UiMessage(
        String sender,
        String message,
        boolean isFromMe
) {
}
