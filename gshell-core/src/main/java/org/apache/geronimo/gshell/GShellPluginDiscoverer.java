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

package org.apache.geronimo.gshell;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.DefaultComponentDiscoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Plexus component discovery compoentn to handle the GShell plugin.xml muck.
 *
 * @version $Rev$ $Date$
 */
@Component(role=GShellPluginDiscoverer.class)
public class GShellPluginDiscoverer
    extends DefaultComponentDiscoverer// AbstractComponentDiscoverer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    //
    // TODO: Put the custom plugin.xml descriptor muck here...
    //
}