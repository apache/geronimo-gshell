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

package org.apache.geronimo.gshell.branding;

import java.io.File;

/**
 * Defines the basic elements for branding a GShell application.
 *
 * @version $Rev$ $Date$
 */
public interface Branding
{
    String HOME = "home";

    File getUserDirectory();

    File getSharedDirectory();
    
    String getName();

    String getDisplayName();

    String getAbout();

    String getVersion();
    
    String getWelcomeBanner();

    String getProfileScriptName();

    String getInteractiveScriptName();

    String getHistoryFileName();

    String getPropertyName(String name);

    String getProperty(String name);

    String getProperty(String name, String defaultValue);

    //
    // TODO: Add getGoodbyeMessage() ?
    //

    //
    // TODO: Maybe this should just be a map of stuff, with well defined keys?  That would support
    //       handy loading of flavors from properties files...
    //
}