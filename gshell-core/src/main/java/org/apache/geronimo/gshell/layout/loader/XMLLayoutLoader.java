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

package org.apache.geronimo.gshell.layout.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gshell.layout.model.Layout;
import org.apache.geronimo.gshell.shell.ShellInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple XML to {@link Layout} loader, uses XStream to handle the dirty work.
 * 
 * @version $Rev$ $Date$
 */
@Component(role=LayoutLoader.class)
public class XMLLayoutLoader
    implements LayoutLoader, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());
        
    @Requirement
    private ShellInfo info;

    private URI location;

    public void initialize() throws InitializationException {
        assert info != null;

        //
        // HACK: Hard code this for now...
        //

        location = new File(info.getHomeDir(), "etc/layout.xml").toURI();
    }

    private Layout load(final URI location) throws IOException {
        assert location != null;

        log.debug("Loading layout from XML: {}", location);

        URL url = location.toURL();
        
        InputStream input = url.openStream();

        Layout layout;
        try {
            layout = Layout.fromXML(input);
        }
        finally {
           IOUtil.close(input);
        }

        log.debug("Loaded layout: {}", layout);

        return layout;
    }

    public Layout load() throws IOException {
        return load(location);
    }
}