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

package org.apache.geronimo.gshell.artifact.monitor;

import static org.apache.maven.wagon.WagonConstants.UNKNOWN_LENGTH;
import org.apache.maven.wagon.events.TransferEvent;
import static org.apache.maven.wagon.events.TransferEvent.REQUEST_PUT;
import org.apache.geronimo.gshell.io.IO;

/**
 * A download monitor providing a simple spinning progress interface.
 *
 * @version $Rev$ $Date$
 */
public class ProgressSpinnerMonitor
    extends TransferListenerSupport
{
    private IO io;

    private ProgressSpinner spinner = new ProgressSpinner();

    private long complete;

    public ProgressSpinnerMonitor(final IO io) {
        assert io != null;

        this.io = io;
    }

    public void transferInitiated(TransferEvent event) {
        complete = 0;

        spinner.reset();

        String message = event.getRequestType() == REQUEST_PUT ? "Uploading" : "Downloading";

        String url = event.getWagon().getRepository().getUrl();

        io.info("{}: {}/{}", message, url, event.getResource().getName());
    }

    public void transferProgress(final TransferEvent event, final byte[] buffer, final int length) {
        long total = event.getResource().getContentLength();
        complete += length;

        String message;

        if (total >= 1024) {
            message = complete / 1024 + "/" + (total == UNKNOWN_LENGTH ? "?" : total / 1024 + "K");
        }
        else {
            message = complete + "/" + (total == UNKNOWN_LENGTH ? "?" : total + "b");
        }

        io.info(spinner.spin(message));
    }

    public void transferCompleted(final TransferEvent event) {
        long length = event.getResource().getContentLength();

        if (length != UNKNOWN_LENGTH) {
            String type = (event.getRequestType() == REQUEST_PUT ? "uploaded" : "downloaded");
            String l = length >= 1024 ? (length / 1024) + "K" : length + "b";

            io.info("{} {}", l, type);
        }
    }
}