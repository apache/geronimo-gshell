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

package org.apache.geronimo.gshell.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.MessageLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ivy {@link MessageLogger} to SLF4J adapter.
 *
 * @version $Rev$ $Date$
 */
public final class Slf4jMessageLogger
    extends AbstractMessageLogger
{
    private static final Logger log = LoggerFactory.getLogger(Ivy.class);

    public void log(final String msg, final int level) {
        switch (level) {
            case Message.MSG_ERR:
                log.error(msg);
                break;

            case Message.MSG_WARN:
                log.warn(msg);
                break;

            case Message.MSG_INFO:
                log.debug(msg);
                break;

            case Message.MSG_VERBOSE:
            case Message.MSG_DEBUG:
                log.trace(msg);
                break;

            default:
                log.error("Unknown level: {}, message: {}", level, msg);
        }
    }

    public void rawlog(final String msg, final int level) {
        log(msg, level);
    }

    public void doProgress() {
        // ignore
    }

    public void doEndProgress(final String msg) {
        // ignore
    }
}