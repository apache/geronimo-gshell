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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.geronimo.gshell.command.Environment;
import org.apache.geronimo.gshell.layout.model.Alias;
import org.apache.geronimo.gshell.layout.model.Command;
import org.apache.geronimo.gshell.layout.model.Layout;
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
    private Environment env;

    private Layout layout;
    
    public void initialize() throws InitializationException {
        assert env != null;

        URL url = null;
        try {
            url = new File(env.getHomeDir(), "etc/layout.xml").toURI().toURL();
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

        // Setup the XStream marshallar and configure it with the aliases for the model we are working with
        XStream xs = new XStream(new DomDriver());
        Annotations.configureAliases(xs, Layout.class, Command.class, Alias.class);

        Layout layout;
        try {
            layout = (Layout)xs.fromXML(input);
            
            assert layout != null;
        }
        finally {
           input.close();
        }

        log.debug("Loaded layout: {}", layout);

        //
        // TODO: Do some kind post-parsing validation or someting?

        return layout;
    }

    public Layout getLayout() {
        if (layout == null) {
            throw new IllegalStateException("Layout has not been initalized");
        }
        
        return layout;
    }
}