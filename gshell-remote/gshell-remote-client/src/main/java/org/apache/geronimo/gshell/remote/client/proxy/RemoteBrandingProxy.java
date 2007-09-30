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

package org.apache.geronimo.gshell.remote.client.proxy;

import java.io.File;

import org.apache.geronimo.gshell.branding.Branding;
import org.apache.geronimo.gshell.remote.client.RshClient;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class RemoteBrandingProxy
    implements Branding
{
    private final RshClient client;

    public RemoteBrandingProxy(final RshClient client) {
        assert client != null;

        this.client = client;
    }

    public File getUserDirectory() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getSharedDirectory() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDisplayName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProgramName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAbout() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getWelcomeBanner() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProfileScriptName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getInteractiveScriptName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHistoryFileName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPropertyName(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProperty(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProperty(String name, String defaultValue) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}