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

package org.apache.geronimo.gshell.artifact.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for {@link TransferListener} implementations.
 *
 * @version $Rev$ $Date$
 */
public class TransferListenerSupport
    implements TransferListener
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public void transferStarted(final TransferEvent event) {
        log.trace("Transfer started: {}", event);
    }

    public void transferProgress(final TransferEvent event) {
        log.trace("Transfer progress: {}", event);
    }

    public void transferCompleted(final TransferEvent event) {
        log.trace("Transfer completed: {}", event);
    }

    public void transferFailed(final TransferEvent event) {
        log.trace("Transfer failure: {}", event);
    }

    protected String renderRequestType(final TransferEvent event) {
        assert event != null;

        return event.getRequestType() == TransferEvent.RequestType.UPLOAD ? "Uploading" : "Downloading";
    }

    protected String renderRequestTypeFinished(final TransferEvent event) {
        assert event != null;

        return event.getRequestType() == TransferEvent.RequestType.UPLOAD ? "Uploaded" : "Downloaded";
    }
}