/**
 * JWhisper GUI client.
 */
module jwhisper.client.gui {
    requires static lombok;

    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.slf4j;
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;

    requires jwhisper.common;

    exports io.github.artshp.jwhisper.client.gui;
    opens io.github.artshp.jwhisper.client.gui.controller to javafx.fxml;
    opens io.github.artshp.jwhisper.client.gui.config to tools.jackson.databind;
}
