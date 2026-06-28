package io.github.artshp.jwhisper.client.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Factory helping to create dialogs (e.g. alerts).
 */
public final class DialogFactory {

    /**
     * Constructor to prohibit instantiating.
     */
    private DialogFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Create a new alert object with app icon.
     * @param type alert type
     * @param title alert title
     * @param header alert header
     * @param content alert content
     * @return a new prepared alert object
     */
    public static Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

        alertStage.getIcons().add(new Image(
                Objects.requireNonNull(DialogFactory.class.getResourceAsStream("/icons/app-icon.png"))
        ));

        return alert;
    }
}
