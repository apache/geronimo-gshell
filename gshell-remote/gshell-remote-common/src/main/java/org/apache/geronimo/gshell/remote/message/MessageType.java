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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.gshell.remote.message.rsh.CloseShellMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ConnectMessage;
import org.apache.geronimo.gshell.remote.message.rsh.EchoMessage;
import org.apache.geronimo.gshell.remote.message.rsh.ExecuteMessage;
import org.apache.geronimo.gshell.remote.message.rsh.LoginMessage;
import org.apache.geronimo.gshell.remote.message.rsh.OpenShellMessage;
import org.apache.geronimo.gshell.remote.stream.WriteStreamMessage;

/**
 * Enumeration of supported message types and factory for message instances.
 *
 * @version $Rev$ $Date$
 */
public enum MessageType
{
    //
    // FIXME: Abstract me...
    //
    
    ECHO                    (EchoMessage.class),
    CONNECT                 (ConnectMessage.class),
    CONNECT_RESULT          (ConnectMessage.Result.class),
    LOGIN                   (LoginMessage.class),
    LOGIN_SUCCESS           (LoginMessage.Success.class),
    LOGIN_FAILURE           (LoginMessage.Failure.class),
    OPEN_SHELL              (OpenShellMessage.class),
    CLOSE_SHELL             (CloseShellMessage.class),
    EXECUTE                 (ExecuteMessage.class),
    EXECUTE_RESULT          (ExecuteMessage.Result.class),
    EXECUTE_NOTIFICATION    (ExecuteMessage.Notification.class),
    EXECUTE_FAULT           (ExecuteMessage.Fault.class),
    WRITE_STREAM            (WriteStreamMessage.class),
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

    public static Set<Class<?>> types() {
        Set<Class<?>> set = new HashSet<Class<?>>();

        for (MessageType type : values()) {
            set.add(type.type);
        }

        return Collections.unmodifiableSet(set);
    }
}