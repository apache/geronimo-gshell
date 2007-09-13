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

import org.codehaus.plexus.util.StringUtils;

/**
 * Support for {@link Branding} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BrandingSupport
    implements Branding
{
    public String getDisplayName() {
        return StringUtils.capitalise(getName());
    }

    public String getProgramName() {
        return System.getProperty("program.name", getName());
    }

    public File getUserDirectory() {
        File userHome = new File(System.getProperty("user.home"));

        File dir = new File(userHome, "." + getName());

        return dir.getAbsoluteFile();
    }

    public File getSharedDirectory() {
        //
        // FIXME: Need to get the home directory of the shell here...  Can't use ShellInfo, since it depends on the Branding
        //

        File dir = new File("/etc");

        return dir.getAbsoluteFile();
    }

    public String getProfileScriptName() {
        return getName() + ".profile";
    }

    public String getInteractiveScriptName() {
        return getName() + ".rc";
    }

    public String getHistoryFileName() {
        return getName() + ".history";
    }

    public String getPropertyName(final String name) {
        assert name != null;

        return getName() + "." + name;
    }

    public String getProperty(final String name) {
        return System.getProperty(getPropertyName(name));
    }

    public String getProperty(final String name, final String defaultValue) {
        String value = getProperty(name);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }
}