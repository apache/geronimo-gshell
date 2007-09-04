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

package org.apache.geronimo.gshell.commands.builtins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Read and execute commands from a file/url in the current shell environment.
 *
 * @version $Rev$ $Date$
 */
@Component(role= Command.class, hint="source")
public class SourceCommand
    extends CommandSupport
{
    @Requirement
    private Shell shell;

    @Argument(required=true, description="Source file")
    private File file;

    public SourceCommand() {
        super("source");
    }

    public void setShell(final Shell shell) {
        this.shell = shell;
    }

    protected String getUsage() {
        return super.getUsage() + " <file|url>";
    }

    protected Object doExecute() throws Exception {
        BufferedReader reader = openReader(file);

        String line;
        while ((line = reader.readLine()) != null) {
            shell.execute(line);
        }

        return SUCCESS;
    }

    private BufferedReader openReader(final Object source) throws IOException {
        BufferedReader reader;

        if (source instanceof File) {
            File file = (File)source;
            log.info("Using source file: " + file);

            reader = new BufferedReader(new FileReader(file));
        }
        else if (source instanceof URL) {
            URL url = (URL)source;
            log.info("Using source URL: " + url);

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        else {
            String tmp = String.valueOf(source);

            // First try a URL
            try {
                URL url = new URL(tmp);
                log.info("Using source URL: " + url);

                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            }
            catch (MalformedURLException ignore) {
                // They try a file
                File file = new File(tmp);
                log.info("Using source file: " + file);
                
                reader = new BufferedReader(new FileReader(tmp));
            }
        }
        
        return reader;
    }
}
