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

package org.apache.geronimo.gshell.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;

/**
 * Local repository configuration.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("localRepository")
public class LocalRepository
    extends ModelElement
{
    private String directory;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    public File getDirectoryFile() {
        String path = getDirectory();
        assert path != null;
        
        return new File(path);
    }

    public void setDirectoryFile(final File directory) {
        assert directory != null;

        setDirectory(directory.getAbsolutePath());
    }
}