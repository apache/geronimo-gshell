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

package org.apache.geronimo.gshell.whisper.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.mina.common.ByteBuffer;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public interface ProtocolWireFormat
{
    void setVersion(int version);

    int getVersion();

    void marshal(DataOutput out, Message message) throws IOException;

    Message unmarshal(DataInput in) throws IOException;

    ByteBuffer marshal(Message message) throws IOException;

    Message unmarshal(ByteBuffer buffer) throws IOException;

    //
    // Factory
    //

    interface Factory
    {
        ProtocolWireFormat create();
    }
}
