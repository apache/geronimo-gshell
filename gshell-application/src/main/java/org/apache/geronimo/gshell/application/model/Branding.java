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

package org.apache.geronimo.gshell.application.model;

import org.apache.geronimo.gshell.yarn.Yarn;

import java.io.File;

/**
 * Branding configuration element.
 *
 * @version $Rev$ $Date$
 */
public class Branding
{
    //
    // FIXME: Merge with Application
    //
    
    private transient ApplicationModel parent;

    // private String displayName;

    private String programName;

    private String userDirectory;

    // private String sharedDirectory;

    // private String stateDirectory;

    private String profileScriptName;

    private String historyFileName;

    private String interactiveScriptName;

    private String aboutMessage;

    private String welcomeMessage;

    // private String goodbyeMessage;

    private String prompt;

    public String toString() {
        return Yarn.render(this);
    }

    public ApplicationModel getParent() {
        if (parent == null) {
            throw new IllegalStateException("Not attached to parent");
        }

        return parent;
    }

    public void setParent(final ApplicationModel parent) {
        this.parent = parent;
    }

    public String getName() {
        return getParent().getArtifactId();
    }

    public String getProgramName() {
        if (programName == null) {
            return System.getProperty("gshell.program", getName());
        }
        
        return programName;
    }

    public String getUserDirectory() {
        if (userDirectory == null) {
            File userHome = new File(System.getProperty("user.home"));
            File dir = new File(userHome, "." + getName());

            return dir.getAbsolutePath();
        }

        return userDirectory;
    }

    public String getProfileScriptName() {
        if (profileScriptName == null) {
            return getName() + ".profile";
        }

        return profileScriptName;
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

    public String getInteractiveScriptName() {
        if (interactiveScriptName == null) {
            return getName() + ".rc";
        }

        return interactiveScriptName;
    }

    public String getAboutMessage() {
        return aboutMessage;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getPrompt() {
        return prompt;
    }
}