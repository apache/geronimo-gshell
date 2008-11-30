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

package org.apache.geronimo.gshell.artifact.maven;

import org.apache.geronimo.gshell.artifact.transfer.TransferEvent;
import org.apache.geronimo.gshell.artifact.transfer.TransferListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts {@link TransferListener} to Maven Wagon's {@link org.apache.maven.wagon.events.TransferListener}.
 *
 * @version $Rev$ $Date$
 */
public class TransferListenerAdapter
    implements org.apache.maven.wagon.events.TransferListener
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TransferListener listener;

    public TransferListenerAdapter(final TransferListener listener) {
        assert listener != null;
        this.listener = listener;
    }

    public void transferInitiated(final org.apache.maven.wagon.events.TransferEvent source) {
        // ignore
    }

    public void transferStarted(final org.apache.maven.wagon.events.TransferEvent source) {
        listener.transferStarted(convert(source));
    }

    public void transferProgress(final org.apache.maven.wagon.events.TransferEvent source, final byte[] buffer, final int length) {
        listener.transferProgress(convert(source, length));
    }

    public void transferCompleted(final org.apache.maven.wagon.events.TransferEvent source) {
        listener.transferCompleted(convert(source));
    }

    public void transferError(final org.apache.maven.wagon.events.TransferEvent source) {
        listener.transferFailed(convert(source));
    }

    public void debug(final String message) {
        log.trace(message);
    }

    private TransferEvent convert(final org.apache.maven.wagon.events.TransferEvent source, final int length) {
        assert source != null;

        return new TransferEvent()
        {
            public RequestType getRequestType() {
                switch (source.getRequestType()) {
                    case org.apache.maven.wagon.events.TransferEvent.REQUEST_GET:
                        return RequestType.DOWNLOAD;

                    case org.apache.maven.wagon.events.TransferEvent.REQUEST_PUT:
                        return RequestType.UPLOAD;
                }
                throw new InternalError();
            }

            public String getLocation() {
                return source.getWagon().getRepository().getUrl() + "/" + source.getResource().getName();
            }

            public long getContentLength() {
                return source.getResource().getContentLength();
            }

            public long getLength() {
                return length;
            }

            public Throwable getFailureCause() {
                return source.getException();
            }
        };
    }

    private TransferEvent convert(final org.apache.maven.wagon.events.TransferEvent source) {
        return convert(source, TransferEvent.UNKNOWN_LENGTH);
    }
}