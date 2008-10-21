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

package org.apache.geronimo.gshell.wisdom.application;

import org.apache.geronimo.gshell.application.ClassPath;
import org.apache.geronimo.gshell.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 * Helper to manage caching of {@link ClassPath} instances.
 *
 * @version $Rev$ $Date$
 */
public class ClassPathCache
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final File file;

    public ClassPathCache(final File file) {
        assert file != null;
        this.file = file;
    }

    public void set(final ClassPath classPath) throws IOException {
        assert classPath != null;

        // noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        try {
            output.writeObject(classPath);
            log.debug("Saved classpath to cache: {}", file);
        }
        finally {
            Closer.close(output);
        }
    }

    public ClassPath get() {
        if (!file.exists()) {
            return null;
        }

        ClassPath classPath;

        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                classPath = (ClassPath)input.readObject();
                log.debug("Loaded classpath from cache: {}", file);
            }
            finally {
                Closer.close(input);
            }
        }
        catch (Exception e) {
            log.warn("Failed to load classpath from cache", e);
            return null;
        }
        
        if (classPath.isValid()) {
            return classPath;
        }

        log.debug("Classpath is not valid");
        
        return null;
    }
}