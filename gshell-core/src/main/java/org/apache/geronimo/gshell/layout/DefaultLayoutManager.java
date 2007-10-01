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

import org.apache.geronimo.gshell.command.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.layout.loader.LayoutLoader;
import org.apache.geronimo.gshell.layout.model.Layout;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.shell.Environment;
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
@Component(role=LayoutManager.class, hint="default")
public class DefaultLayoutManager
    implements LayoutManager, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private CommandRegistry commandRegistry;

    @Requirement
    private LayoutLoader loader;

    @Requirement
    private Environment env;
    
    private Layout layout;
    
    public void initialize() throws InitializationException {
        assert loader != null;

        //
        // FIXME: Turn this off for now...
        //

        /*
        try {
            layout = loader.load();
        }
        catch (IOException e) {
            throw new InitializationException(e.getMessage(), e);
        }
        */
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
        // HACK: For now, assume the path is just the id... should eventually change this
        //

        return commandRegistry.lookup(path);
    }
}