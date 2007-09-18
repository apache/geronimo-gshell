/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.gshell.remote.message;

/**
 * Enumeration of supported message types and factory for message instances.
 *
 * @version $Rev$ $Date$
 */
public enum MessageType
{
    ECHO            (EchoMessage.class),
    HANDSHAKE       (HandShakeMessage.class),
    OPEN_SHELL      (OpenShellMessage.class),
    CLOSE_SHELL     (CloseShellMessage.class),
    EXECUTE         (ExecuteMessage.class),
    WRITE_STREAM    (WriteStreamMessage.class),
    ;

    private final Class<? extends Message> type;

    MessageType(final Class<? extends Message> type) {
        assert type != null;

        this.type = type;
    }

    public Class<? extends Message> getType() {
        return type;
    }
    
    public static Message create(final MessageType type) {
        assert type != null;

        Class impl = type.getType();

        try {
            return (Message) impl.newInstance();
        }
        catch (InstantiationException e) {
            throw new Error(e);
        }
        catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}