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

package org.apache.geronimo.gshell.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides externalization of the GShell version details.
 *
 * <p>This facilitates syncing up version details with the build system.
 *
 * @version $Rev$ $Date$
 */
public class Version
{
    private static Version instance;

    public static Version getInstance() {
        if (instance == null) {
            instance = new Version();
        }

        return instance;
    }

    private Properties props = new Properties();

    public Version() {
        InputStream input = getClass().getResourceAsStream("version.properties");
        assert input != null;

        try {
            props.load(input);
            input.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load version.properties", e);
        }
    }

    public String toString() {
        return props.getProperty("version");
    }
}
