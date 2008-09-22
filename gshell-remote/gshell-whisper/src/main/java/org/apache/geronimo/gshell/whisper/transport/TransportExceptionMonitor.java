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

package org.apache.geronimo.gshell.whisper.transport;

import org.apache.mina.common.ExceptionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging exception monitor.
 *
 * @version $Rev$ $Date$
 */
public class TransportExceptionMonitor
    extends ExceptionMonitor
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    public void exceptionCaught(final Throwable cause) {
        assert cause != null;

        log.error("Unhandled exception: " + cause, cause);
    }
}