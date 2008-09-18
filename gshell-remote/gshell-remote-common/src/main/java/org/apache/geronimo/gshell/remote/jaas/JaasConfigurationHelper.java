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

package org.apache.geronimo.gshell.remote.jaas;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class JaasConfigurationHelper
{
    private static final String KEY = "java.security.auth.login.config";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String resourceName;

    public JaasConfigurationHelper(final String resourceName) {
        this.resourceName = resourceName;
    }

    public JaasConfigurationHelper() {}

    @PostConstruct
    public void init() {
        // Initialize the JAAS configuration
        String path = System.getProperty(KEY);

        if (path == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL resource = cl.getResource(resourceName);

            if (resource != null) {
                path = resource.toExternalForm();
                System.setProperty(KEY, path);
            }
        }

        log.debug("Using JAAS login config: {}", path);
    }
}