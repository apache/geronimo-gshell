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

package org.apache.geronimo.gshell.artifact.transfer.monitor;

import org.apache.geronimo.gshell.artifact.transfer.TransferEvent;
import org.apache.geronimo.gshell.artifact.transfer.TransferListenerSupport;
import org.apache.geronimo.gshell.io.IO;

import java.io.IOException;

/**
 * A simple download monitor.
 *
 * @version $Rev$ $Date$
 */
public class SimpleMonitor
    extends TransferListenerSupport
{
    private final IO io;

    public SimpleMonitor(final IO io) throws IOException {
        assert io != null;

        this.io = io;
    }

    private void println(final String message) {
        if (!io.isQuiet()) {
            io.out.println(message);
            io.out.flush();
        }
    }

    public void transferStarted(final TransferEvent event) {
        assert event != null;

        super.transferStarted(event);

        println(renderRequestType(event) + ": " + event.getLocation());
    }

    public void transferCompleted(final TransferEvent event) {
        assert event != null;

        super.transferCompleted(event);

        println(renderRequestTypeFinished(event) + " " + renderBytes(event.getContentLength()));
    }
}