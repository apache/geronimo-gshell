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

import junit.framework.TestCase;
import org.apache.geronimo.gshell.model.application.ApplicationMarshaller;
import org.apache.geronimo.gshell.model.application.Application;
import org.apache.geronimo.gshell.model.settings.SettingsMarshaller;
import org.apache.geronimo.gshell.model.settings.Settings;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.ClassWorld;

import java.net.URL;
import java.util.Collection;

/**
 * Unit tests for the {@link GShellBuilder} class.
 *
 * @version $Rev$ $Date$
 */
public class GShellBuilderTest
    extends TestCase
{
    private GShellBuilder builder;

    protected void setUp() throws Exception {
        builder = new GShellBuilder();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuild1() throws Exception {
        SettingsMarshaller settingsMarshaller = new SettingsMarshaller();
        URL settingsUrl = getClass().getResource("settings1.xml");
        assertNotNull(settingsUrl);

        Settings settings = settingsMarshaller.unmarshal(settingsUrl);
        assertNotNull(settings);
        System.out.println(settings);
        
        builder.setSettings(settings);

        ApplicationMarshaller applicationMarshaller = new ApplicationMarshaller();
        URL applicationUrl = getClass().getResource("application1.xml");
        assertNotNull(applicationUrl);

        Application application = applicationMarshaller.unmarshal(applicationUrl);
        assertNotNull(application);
        System.out.println(application);

        builder.setApplication(application);

        GShell shell = builder.build();
        // assertNotNull(shell);
    }
}