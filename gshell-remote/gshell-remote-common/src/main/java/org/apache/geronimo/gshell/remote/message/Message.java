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

import java.io.Serializable;

import org.apache.geronimo.gshell.remote.marshal.MarshalAware;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

/**
 * Defines the basic attributes of all messages.
 *
 * @version $Rev$ $Date$
 */
public interface Message
    extends MarshalAware
{
    ID getId();

    ID getCorrelationId();

    void setCorrelationId(ID id);

    MessageType getType();

    long getTimestamp();

    long getSequence();

    void setSession(IoSession session);

    IoSession getSession();

    void process(IoSession session, MessageVisitor visitor) throws Exception;

    void freeze();

    boolean isFrozen();

    WriteFuture reply(Message msg);

    interface ID
        extends MarshalAware, Serializable
    {
        // Marker
    }

    interface IDGenerator
    {
        ID generate();
    }
}
