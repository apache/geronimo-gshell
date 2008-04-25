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

package org.apache.geronimo.gshell.console;

import java.io.File;
import java.io.IOException;
import jline.History;
import org.apache.geronimo.gshell.branding.Branding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role=History.class, hint="default")
public class FileHistory extends History {
	
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private Branding branding;

    public FileHistory() {
    }

    public FileHistory(final Branding branding) throws IOException {
        this.branding = branding;
        initialize();
    }

    public void initialize() throws IOException {
        setHistoryFile(new File(branding.getUserDirectory(), branding.getHistoryFileName()));
    }

    public void setHistoryFile(final File file) throws IOException {
        assert file != null;
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
            log.debug("Created base directory for history file: {}", dir);
        }
        log.debug("Using history file: {}", file);
        super.setHistoryFile(file);
    }

}