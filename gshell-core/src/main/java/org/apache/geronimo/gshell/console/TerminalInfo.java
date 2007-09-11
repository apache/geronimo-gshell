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

package org.apache.geronimo.gshell.console;

import jline.Terminal;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=TerminalInfo.class)
public class TerminalInfo
    implements Initializable
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private Terminal terminal;

    public void initialize() throws InitializationException {
        log.debug("Using terminal: {}", terminal);
        log.debug("  Supported: {}", terminal.isSupported());
        log.debug("  H x W: {} x {}", terminal.getTerminalHeight(), terminal.getTerminalWidth());
        log.debug("  Echo: {}", terminal.getEcho());
        log.debug("  ANSI: {} ", terminal.isANSISupported());

        if (terminal instanceof jline.WindowsTerminal) {
            log.debug("  Direct: {}", ((jline.WindowsTerminal)terminal).getDirectConsole());
        }
    }
}