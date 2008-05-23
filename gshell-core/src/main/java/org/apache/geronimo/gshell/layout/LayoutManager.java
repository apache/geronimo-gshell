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

import org.apache.geronimo.gshell.model.layout.Layout;
import org.apache.geronimo.gshell.model.layout.Node;

/**
 * Provies the shell with a simple mechanism to organize commands.
 *
 * @version $Rev$ $Date$
 */
public interface LayoutManager
{
    String CURRENT_NODE = LayoutManager.class.getName() + ".currentNode";

    String ROOT = "/";

    // FIXME: Rename, this is a file name sep, not a path sep
    String PATH_SEPARATOR = "/";
    
    String COMMAND_PATH = "path";
    
    String SEARCH_PATH_SEPARATOR = ":";

    Layout getLayout();
    
    Node findNode(String path) throws NotFoundException;
    
    Node findNode(String path, String searchPath) throws NotFoundException;

    Node findNode(Node start, String path) throws NotFoundException;
}
