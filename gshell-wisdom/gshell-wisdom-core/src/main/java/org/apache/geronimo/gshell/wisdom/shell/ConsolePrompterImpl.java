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

package org.apache.geronimo.gshell.wisdom.shell;

import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.model.application.Branding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link Console.Prompter} component.
 *
 * @version $Rev$ $Date$
 */
public class ConsolePrompterImpl
    implements Console.Prompter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Application application;

    private Renderer renderer = new Renderer();

    //
    // TODO: Need to create a PatternPrompter, which can use interpolation of a variable to render the prompt
    //       so the following variable value would set the same prompt as we are hardcoding here:
    //
    //    set gshell.prompt="@|bold ${application.username}|@${application.localHost.hostName}:@|bold ${application.branding.name}|> "
    //

    public String prompt() {
        assert application != null;
        Branding branding = application.getModel().getBranding();

        StringBuilder buff = new StringBuilder();
        buff.append(Renderer.encode(application.getUserName(), Code.BOLD));
        buff.append("@");
        buff.append(application.getLocalHost().getHostName());
        buff.append(":");
        buff.append(Renderer.encode(branding.getName(), Code.BOLD));
        buff.append("> ");

        return renderer.render(buff.toString());
    }
}