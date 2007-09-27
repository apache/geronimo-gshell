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

import org.apache.geronimo.gshell.whisper.message.BaseMessage;
import org.apache.geronimo.gshell.whisper.message.Message;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RshMessage
    extends BaseMessage
{
    protected RshMessage(final Type type) {
        super(type);
    }

    public static enum Type
        implements Message.Type
    {
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
        ;

        private final Class<? extends RshMessage> type;

        private Type(Class<? extends RshMessage> type) {
            assert type != null;

            this.type = type;
        }

        public Class<? extends RshMessage> getType() {
            return type;
        }
    }
}