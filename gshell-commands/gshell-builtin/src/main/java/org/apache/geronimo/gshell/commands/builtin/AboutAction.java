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

package org.apache.geronimo.gshell.commands.builtin;

import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.application.model.ApplicationModel;
import org.apache.geronimo.gshell.application.model.Branding;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;

/**
 * Display information about the current GShell application.
 *
 * @version $Rev$ $Date$
 */
public class AboutAction
    implements CommandAction
{
    private final Application application;

    public AboutAction(final Application application) {
        assert application != null;
        this.application = application;
    }

    public Object execute(CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        ApplicationModel model = application.getModel();
        Branding branding = application.getModel().getBranding();
        
        String id = application.getId();
        String name = model.getName();

        if (name == null) {
            io.info("{}", id);
        }
        else {
            io.info("{} ({})", name, id);
        }

        io.info("{}", model.getVersion());

        io.out.println();
        io.out.println(branding.getAboutMessage());

        //
        // TODO: Add more options to get specific details out.  Hook up the branding muck here too.
        //
        
        return Result.SUCCESS;
    }
}
