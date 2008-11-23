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

package org.apache.geronimo.gshell.artifact;

import java.io.File;
import java.io.Serializable;

/**
 * Defines an artifact (groupId, artifactId, version, etc).
 *
 * @version $Rev$ $Date$
 */
public class Artifact
    implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1;
    
    public static final String DEFAULT_TYPE = "jar";

    private String group;

    private String name;

    private String classifier;

    private String type;

    private String version;

    private File file;

    public String toString() {
        return getId();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(final String classifier) {
        this.classifier = classifier;
    }

    public String getType() {
        if (type == null) {
            return DEFAULT_TYPE;
        }
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public File getFile() {
        return file;
    }

    public void setFile(final File file) {
        this.file = file;
    }

    public String getId() {
        if (getClassifier() != null) {
            return getGroup() + ":" + getName() + ":" + getClassifier() + ":" + getVersion() + ":" + getType();
        }
        else {
            return getGroup() + ":" + getName() + ":" + getVersion() + ":" + getType();
        }
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
