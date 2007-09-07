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

package org.apache.geronimo.gshell.layout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.geronimo.gshell.command.ShellInfo;
import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.layout.model.Layout;
import org.apache.geronimo.gshell.plugin.PluginCollector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of a {@link LayoutManager}.
 *
 * @version $Rev$ $Date$
 */
@Component(role=LayoutManager.class)
public class LayoutManagerImpl
    implements LayoutManager, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Requirement
    private ShellInfo info;

    @Requirement
    private PluginCollector pluginCollector;

    private Layout layout;
    
    public void initialize() throws InitializationException {
        assert info != null;

        //
        // TODO: Move to a layout loader to abstract how this is loaded and allow for better configuration
        //
        
        URL url = null;
        try {
            url = new File(info.getHomeDir(), "etc/layout.xml").toURI().toURL();
        } catch (MalformedURLException e) {
            throw new InitializationException("Invalid URL for layout configuration", e);
        }

        try {
            this.layout = load(url);
        }
        catch (IOException e) {
            throw new InitializationException("Failed to load layout from URL: " + url, e);
        }
    }

    private Layout load(final URL url) throws IOException {
        assert url != null;

        log.debug("Loading layout from XML: {}", url);

        InputStream input = url.openStream();

        Layout layout;
        try {
            layout = Layout.fromXML(input);
            
            assert layout != null;
        }
        finally {
           input.close();
        }

        log.debug("Loaded layout: {}", layout);

        return layout;
    }

    public Layout getLayout() {
        if (layout == null) {
            throw new IllegalStateException("Layout has not been initalized");
        }
        
        return layout;
    }

    public CommandDescriptor find(final String path) {
        assert path != null;

        log.debug("Searching for command descriptor for path: {}", path);

        //
        // TODO: Need the current environment, so we can get the current group (pwd) and search path, and then
        //       use that to search the layout tree.
        //

        //
        // HACK: For now, assume the path is just the id... should eventually change this
        //

        return pluginCollector.getCommandDescriptor(path);
    }
}