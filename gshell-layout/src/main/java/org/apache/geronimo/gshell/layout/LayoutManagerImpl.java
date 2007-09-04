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

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of a {@link LayoutManager}.
 *
 * @version $Rev$ $Date$
 */
public class LayoutManagerImpl
    implements LayoutManager, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    // @Requirement
    private PlexusContainer container;

    public void initialize() throws InitializationException {
        //
        // TODO: Load up the model... from configuration
        //
    }
}