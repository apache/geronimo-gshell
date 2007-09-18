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

import org.apache.mina.common.ByteBuffer;

/**
 * Execute a command-line.
 *
 * @version $Rev$ $Date$
 */
public class ExecuteMessage
    extends MessageSupport
{
    private String line;

    public ExecuteMessage(final String line) {
        super(MessageType.EXECUTE);

        this.line = line;
    }

    public ExecuteMessage() {
        this(null);
    }

    public String getLine() {
        return line;
    }

    public void readExternal(final ByteBuffer buff) throws Exception {
        assert buff != null;

        super.readExternal(buff);

        line = readString(buff);
    }

    public void writeExternal(final ByteBuffer buff) throws Exception {
        assert buff != null;

        super.writeExternal(buff);

        writeString(buff, line);
    }

    public void process(final MessageVisitor visitor) throws Exception {
        assert visitor != null;

        visitor.visitExecute(this);
    }
}