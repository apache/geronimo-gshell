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

import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.model.layout.GroupNode;
import org.apache.geronimo.gshell.model.layout.Layout;
import org.apache.geronimo.gshell.model.layout.Node;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link LayoutManager}.
 *
 * @version $Rev$ $Date$
 */
@Component(role=LayoutManager.class)
public class DefaultLayoutManager
    implements LayoutManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ApplicationManager applicationManager;
    
    private Layout layout;

    public DefaultLayoutManager() {}
    
    public DefaultLayoutManager(final ApplicationManager applicationManager) {
        assert applicationManager != null;

        this.applicationManager = applicationManager;
    }

    private Layout lookupLayout() {
        assert applicationManager != null;

        Layout layout = applicationManager.getContext().getApplication().getLayout();

        if (layout == null) {
            throw new IllegalStateException("Layout has not been configured for application");
        }

        return layout;
    }

    public Layout getLayout() {
        if (layout == null) {
            layout = lookupLayout();

            log.debug("Using layout: {}", layout);
        }
        
        return layout;
    }

    public Node findNode(final String path) throws NotFoundException {
        return findNode(path, null);
    }

    public Node findNode(final String path, final String searchPath) throws NotFoundException {
        assert path != null;

        // Make sure we have initialized the layout
        getLayout();

        Node start;

        if (path.startsWith(PATH_SEPARATOR)) {
            start = layout;
            return findNode(start, path);
        }
        else if (searchPath != null) {
            String[] pathList = searchPath.split(SEARCH_PATH_SEPARATOR);
            Node foundNode = null;
            
            for (String commandPath : pathList) {
                try {
                    Node pathNode = findNode(commandPath);
                    foundNode = findNode(pathNode, path);
                    
                    if (foundNode != null) {
                        break;
                    }
                }
                catch (NotFoundException e) {
                    // Ignore this for now.  We might still have paths to check
                }
            }
            
            if (foundNode == null) {
                foundNode = findNode(layout, path);
            }
            
            return foundNode;
        }
        else {
            assert applicationManager != null;
            
            start = (Node) applicationManager.getContext().getEnvironment().getVariables().get(CURRENT_NODE);

            if (start == null) {
                start = layout;
            }
            
            return findNode(start, path);
        }
    }

    public Node findNode(final Node start, final String path) throws NotFoundException {
        assert start != null;
        assert path != null;

        Node current = start;

        String[] elements = path.split(PATH_SEPARATOR);
        
        for (String element : elements) {
            if (current instanceof GroupNode) {
                Node node = ((GroupNode)current).find(element);

                if (node == null) {
                    throw new NotFoundException(path);
                }

                current = node;
            }
            else {
                throw new NotFoundException(path);
            }
        }

        return current;
    }
}