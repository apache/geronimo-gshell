package org.apache.geronimo.gshell.remote.server.handler;

import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.geronimo.gshell.whisper.message.MessageHandler;
import org.apache.mina.common.IoSession;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public interface ServerMessageHandler<T extends Message>
    extends MessageHandler<T>
{
    void handle(final IoSession session, final ServerSessionContext context, final T message) throws Exception;
}
