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

import org.apache.geronimo.gshell.application.Application;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.model.ApplicationModel;
import org.apache.geronimo.gshell.model.Branding;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Display information about the current GShell application.
 *
 * @version $Rev$ $Date$
 */
public class AboutAction
    implements CommandAction
{
    @Autowired
    private ApplicationManager applicationManager;
    
    public Object execute(CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        assert applicationManager != null;
        Application app = applicationManager.getApplication();
        ApplicationModel model = app.getModel();
        Branding branding = app.getModel().getBranding();
        
        String id = app.getId();
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
