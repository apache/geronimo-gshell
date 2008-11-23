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

package org.apache.geronimo.gshell.commands.admin;

import org.apache.geronimo.gshell.artifact.Artifact;
import org.apache.geronimo.gshell.application.plugin.PluginManager;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install a GShell plugin.
 *
 * @version $Rev$ $Date$
 */
public class InstallPluginAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PluginManager pluginManager;

    @Option(name="-g", aliases={"--groupId"}, argumentRequired=true, required=true)
    private String groupId;

    @Option(name="-a", aliases={"--artifactId"}, argumentRequired=true, required=true)
    private String artifactId;

    @Option(name="-v", aliases={"--version"}, argumentRequired=true, required=true)
    private String version;

    public InstallPluginAction(final PluginManager pluginManager) {
        assert pluginManager != null;
        this.pluginManager = pluginManager;
    }
    
    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        Artifact artifact = new Artifact();
        artifact.setGroup(groupId);
        artifact.setName(artifactId);
        artifact.setVersion(version);

        io.info("Loading plugin: {}", artifact.getId());

        log.debug("Plugin manager: {}", pluginManager);

        try {
            pluginManager.loadPlugin(artifact);

            return Result.SUCCESS;
        }
        catch (Exception e) {
            log.error("Failed to load plugin", e);
        }

        return Result.FAILURE;
    }
}