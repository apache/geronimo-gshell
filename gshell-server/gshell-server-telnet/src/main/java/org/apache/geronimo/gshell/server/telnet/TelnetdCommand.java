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

package org.apache.geronimo.gshell.server.telnet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;

import java.net.Socket;
import java.util.Properties;

import net.wimpi.telnetd.TelnetD;

/**
 * Starts a Telnet shell server.
 *
 * @version $Rev$ $Date$
 */
public class TelnetdCommand
    extends CommandSupport
{
    private int port = 5057;

    public TelnetdCommand() {
        super("telnetd");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

        options.addOption(OptionBuilder.withLongOpt("port")
            .withDescription(messages.getMessage("cli.option.port"))
            .hasArg()
            .create('p'));

        return options;
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        if (line.hasOption('p')) {
            String tmp = line.getOptionValue('p');
            port = Integer.parseInt(tmp);
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("telnetd.properties"));

        //
        // TODO: Stuff in the port and other settings into the properties
        //
        
        TelnetD telnetd = TelnetD.createTelnetD(props);
        telnetd.start();

        //
        // TODO: Spit out something useful to indicate we are listening
        //
        
        return Command.SUCCESS;
    }
}
