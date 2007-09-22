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

package org.apache.geronimo.gshell.remote.request;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.message.Message;
import org.apache.geronimo.gshell.remote.message.MessageEncoder;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoderFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RequestEncoder
    extends MessageEncoder
{
    @SuppressWarnings({"FieldCanBeLocal"})
    private static final Set<Class<?>> MESSAGE_TYPES;

    static {
        Set<Class<?>> types = new HashSet<Class<?>>();

        types.add(Request.class);

        MESSAGE_TYPES = Collections.unmodifiableSet(types);
    }

    public RequestEncoder(final CryptoContext crypto) {
        super(crypto);
    }

    public Set<Class<?>> getMessageTypes() {
        return MESSAGE_TYPES;
    }

    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        Request request = (Request) message;

        Message msg = request.getMessage();

        super.encode(session, msg, out);
    }

    //
    // Factory
    //

    public static class Factory
        extends FactorySupport
        implements MessageEncoderFactory
    {
        public Factory(final CryptoContext crypto) {
            super(crypto);
        }

        public MessageEncoder getEncoder() throws Exception {
            return new RequestEncoder(crypto);
        }
    }
}