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

package org.apache.geronimo.gshell.model.application;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.model.common.ModelElement;
import org.codehaus.plexus.util.StringUtils;

import java.util.Properties;
import java.io.File;

/**
 * Branding configuration element.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("branding")
public class Branding
    extends ModelElement
{
    //
    // TODO: Provide some linkage to parent
    //

    private transient Application parent;

    private Properties properties;

    private String name;

    private String displayName;

    private String programName;

    private String version;

    private String userDirectory;

    private String sharedDirectory;

    private String profileScriptName;

    private String historyFileName;

    private String interactiveScriptName;

    private String aboutMessage;

    private String welcomeMessage;

    private String goodbyeMessage;

    public Application getParent() {
        if (parent == null) {
            throw new IllegalStateException("Not attached to parent");
        }

        return parent;
    }

    public void setParent(final Application parent) {
        this.parent = parent;
    }

    public String getName() {
        if (name == null) {
            return getParent().getArtifactId();
        }

        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return StringUtils.capitalise(getName());
        }

        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getProgramName() {
        if (programName == null) {
            return System.getProperty("program.name", getName());
        }
        
        return programName;
    }

    public void setProgramName(final String programName) {
        this.programName = programName;
    }

    public String getVersion() {
        if (version == null) {
            return getParent().getVersion();
        }

        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    //
    // TODO: Add type File accessors, need to rename these, so the accessor can be *File or *Directory
    //

    public String getUserDirectory() {
        if (userDirectory == null) {
            File userHome = new File(System.getProperty("user.home"));
            File dir = new File(userHome, "." + getName());

            return dir.getAbsolutePath();
        }

        return userDirectory;
    }

    public void setUserDirectory(final String userDirectory) {
        this.userDirectory = userDirectory;
    }

    public String getSharedDirectory() {
        //
        // TODO: Default this to root under the application's directory
        //

        return sharedDirectory;
    }

    public void setSharedDirectory(final String sharedDirectory) {
        this.sharedDirectory = sharedDirectory;
    }

    public String getProfileScriptName() {
        if (profileScriptName == null) {
            return getName() + ".profile";
        }

        return profileScriptName;
    }

    public File getUserProfileScriptFile() {
        return new File(getUserDirectory(), getProfileScriptName());
    }

    public File getSharedProfileScriptFile() {
        return new File(getSharedDirectory(), getProfileScriptName());
    }

    public void setProfileScriptName(final String profileScriptName) {
        this.profileScriptName = profileScriptName;
    }

    public String getHistoryFileName() {
        if (historyFileName == null) {
            return getName() + ".history";
        }

        return historyFileName;
    }

    public File getHistoryFile() {
        return new File(getUserDirectory(), getHistoryFileName());
    }
    
    public void setHistoryFileName(final String historyFileName) {
        this.historyFileName = historyFileName;
    }

    public String getInteractiveScriptName() {
        if (interactiveScriptName == null) {
            return getName() + ".rc";
        }

        return interactiveScriptName;
    }

    public File getInteractiveScriptFile() {
        return new File(getUserDirectory(), getInteractiveScriptName());
    }

    public void setInteractiveScriptName(final String interactiveScriptName) {
        this.interactiveScriptName = interactiveScriptName;
    }

    public String getAboutMessage() {
        return aboutMessage;
    }

    public void setAboutMessage(final String aboutMessage) {
        this.aboutMessage = aboutMessage;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(final String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getGoodbyeMessage() {
        return goodbyeMessage;
    }

    public void setGoodbyeMessage(final String goodbyeMessage) {
        this.goodbyeMessage = goodbyeMessage;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }
}