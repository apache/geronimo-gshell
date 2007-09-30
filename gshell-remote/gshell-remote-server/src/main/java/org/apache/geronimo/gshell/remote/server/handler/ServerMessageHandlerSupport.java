package org.apache.geronimo.gshell.remote.server.handler;

import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageHandlerSupport;
import org.apache.mina.common.IoSession;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public abstract class ServerMessageHandlerSupport<T extends Message>
    extends MessageHandlerSupport<T>
    implements ServerMessageHandler<T>
{
    protected ServerMessageHandlerSupport(final Message.Type type) {
        super(type);
    }

    public void messageReceived(final IoSession session, final T message) throws Exception {
        log.debug("Processing: {}", message);

        ServerSessionContext context = ServerSessionContext.BINDER.lookup(session);

        handle(session, context, message);
    }
}
