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
 * A download monitor providing a simple spinning progress interface.
 *
 * @version $Rev$ $Date$
 */
public class ProgressSpinnerMonitor
    extends TransferListenerSupport
{
    private static final String CARRIAGE_RETURN = "\r";

    private final IO io;

    private final ProgressSpinner spinner = new ProgressSpinner();

    private long complete;

    public ProgressSpinnerMonitor(final IO io) throws IOException {
        assert io != null;

        this.io = io;
    }
    
    private void print(final String message) {
        if (!io.isQuiet()) {
            io.out.print(message);
            io.out.print(CARRIAGE_RETURN);
            io.out.flush();
        }
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

        complete = 0;

        spinner.reset();

        String type = renderRequestType(event);
        String location = event.getLocation();

        String message = type + ": " + location;

        println(message);
    }

    public void transferProgress(final TransferEvent event) {
        assert event != null;

        super.transferProgress(event);

        long total = event.getContentLength();
        complete += event.getLength();

        String message;

        if (total >= 1024) {
            message = complete / 1024 + "/" + (total == TransferEvent.UNKNOWN_LENGTH ? "?" : total / 1024 + "K");
        }
        else {
            message = complete + "/" + (total == TransferEvent.UNKNOWN_LENGTH ? "?" : total + "b");
        }

        
        print(spinner.spin(message));
    }

    public void transferCompleted(final TransferEvent event) {
        assert event != null;

        super.transferCompleted(event);

        long total = event.getContentLength();
        String type = renderRequestTypeFinished(event);
        String bytes = total >= 1024 ? ( total / 1024 ) + "K" : total + "b";

        // HACK: pad at end just incase, should really blank the reset of the line
        print(type + " " + bytes + "          ");
    }
}