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

package org.apache.geronimo.gshell.remote.message.rsh;

import org.apache.geronimo.gshell.remote.marshall.Marshaller;
import org.apache.geronimo.gshell.remote.message.MessageSupport;
import org.apache.geronimo.gshell.remote.message.MessageType;
import org.apache.geronimo.gshell.remote.message.MessageVisitor;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

/**
 * Echo text.
 *
 * @version $Rev$ $Date$
 */
public class EchoMessage
    extends MessageSupport
{
    private String text;
    
    public EchoMessage(final String text) {
        super(MessageType.ECHO);
        
        this.text = text;
    }

    public EchoMessage() {
        this(null);
    }

    public String getText() {
        return text;
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        super.readExternal(in);

        text = Marshaller.readString(in);
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        super.writeExternal(out);

        Marshaller.writeString(out, text);
    }

    public void process(final IoSession session, final MessageVisitor visitor) throws Exception {
        assert visitor != null;

        visitor.visitEcho(session, this);
    }
}