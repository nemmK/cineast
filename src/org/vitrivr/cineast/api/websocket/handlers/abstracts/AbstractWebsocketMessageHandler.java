package org.vitrivr.cineast.api.websocket.handlers.abstracts;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.websocket.handlers.interfaces.WebsocketMessageHandler;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.json.JsonWriter;

/**
 * This abstract class implements the WebsocketMessageHandler interface and provides basic functionality like a convenience
 * method to write information back to the underlying WebSocket stream.
 *
 * @author rgasser
 * @version 1.0
 * @created 22.01.17
 */
public abstract class AbstractWebsocketMessageHandler<A> implements WebsocketMessageHandler<A> {

    protected static Logger LOGGER = LogManager.getLogger();

    /* JsonWriter used to serialize resulting objects to a JSON representation. */
    private JsonWriter writer = new JacksonJsonProvider();

    /**
     * Writes a message back to the stream.
     *
     * @param session
     * @param message
     */
    protected final void write(Session session, Message message) {
        try {
            session.getRemote().sendString(this.writer.toJson(message));
            session.getRemote().flush();
        } catch (IOException e) {
            LOGGER.fatal("Failed to write message to WebSocket stream!", LogHelper.getStackTrace(e));
        }
    }
}
