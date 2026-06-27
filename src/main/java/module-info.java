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

    requires jwhisper.common;
}
