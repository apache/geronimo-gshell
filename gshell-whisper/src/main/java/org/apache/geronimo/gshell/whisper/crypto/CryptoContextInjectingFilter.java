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

package org.apache.geronimo.gshell.whisper.crypto;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ????
 *
 * @version $Rev$ $Date$
 */
public class CryptoContextInjectingFilter
    extends IoFilterAdapter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private CryptoContext crypto;

    public CryptoContextInjectingFilter(final CryptoContext crypto) {
        this.crypto = crypto;
    }

    private void set(final Object message) {
        try {
            if (message instanceof CryptoContextAware) {
                CryptoContextAware aware = (CryptoContextAware) message;
        
                aware.setCryptoContext(crypto);
            }
        }
        catch (Exception e) {
            log.warn("Failed to set crypto context; ignoring", e);
        }
    }

    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
        set(message);

        nextFilter.messageReceived(session, message);
    }

    public void filterWrite(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
        Object message = writeRequest.getMessage();

        set(message);

        nextFilter.filterWrite(session, writeRequest);
    }
}