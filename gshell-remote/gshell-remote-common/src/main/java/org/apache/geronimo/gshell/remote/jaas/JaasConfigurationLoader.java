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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.net.URL;

/**
 * Component to configure JAAS.
 *
 * @version $Rev$ $Date$
 */
public class JaasConfigurationLoader
    implements BeanClassLoaderAware
{
    //
    // TODO: Look at using JSecurity or spring-security to handle auth and such?
    //

    private static final String KEY = "java.security.auth.login.config";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader classLoader;

    private String resourceName;

    public void setBeanClassLoader(final ClassLoader classLoader) {
        // classLoader could be null

        if (classLoader == null) {
            this.classLoader = ClassUtils.getDefaultClassLoader();
        }
        else {
            this.classLoader = classLoader;
        }
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    @PostConstruct
    public void init() {
        if (resourceName == null) {
            throw new IllegalStateException("Missing required property: resourceName");
        }

        // Initialize the JAAS configuration
        String path = System.getProperty(KEY);

        if (path == null) {
            URL resource = classLoader.getResource(resourceName);

            //
            // FIXME: This is not very friendly for threaded environments
            //
            
            if (resource != null) {
                path = resource.toExternalForm();
                System.setProperty(KEY, path);
            }
        }

        if (path == null) {
            log.warn("Unable to locate JAAS login config");
        }
        else {
            log.info("Using JAAS login config: {}", path);
        }
    }
}