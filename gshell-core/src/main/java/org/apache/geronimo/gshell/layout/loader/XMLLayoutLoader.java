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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.geronimo.gshell.ShellInfo;
import org.apache.geronimo.gshell.layout.model.Layout;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
// FIXME: Should not need to specify a hint of "default" here, but the @Component will default to "default"
//        instead of null, as it should.
//

/**
 * A simple XML to {@link Layout} loader, uses XStream to handle the dirty work.
 * 
 * @version $Rev$ $Date$
 */
@Component(role= LayoutLoader.class, hint="default") // hint="xml"
public class XMLLayoutLoader
    implements LayoutLoader, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());
        
    @Requirement
    private ShellInfo info;

    //
    // FIXME: Need to fix the @Configuration annotation so that it works...
    //

    // @Configuration(key="url", value={"etc/layout.xml"})
    private URL url;

    public void initialize() throws InitializationException {
        assert info != null;

        try {
            //
            // HACK: Hard code this for now...
            //
            
            url = new File(info.getHomeDir(), "etc/layout.xml").toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new InitializationException("Invalid URL for layout configuration", e);
        }
    }

    private Layout load(final URL url) throws IOException {
        assert url != null;

        log.debug("Loading layout from XML: {}", url);

        InputStream input = url.openStream();

        Layout layout;
        try {
            layout = Layout.fromXML(input);
        }
        finally {
           input.close();
        }

        log.debug("Loaded layout: {}", layout);

        return layout;
    }

    public Layout load() throws IOException {
        return load(url);
    }
}