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


package org.apache.geronimo.gshell.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Permission;
import java.util.PropertyPermission;

/**
 * Custom security manager to prevent commands from doing bad things.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationSecurityManager
    extends SecurityManager
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SecurityManager parent;

    public ApplicationSecurityManager(final SecurityManager parent) {
        // parent may be null if there is no installed secrutiy manager

        this.parent = parent;
    }

    public ApplicationSecurityManager() {
        this(System.getSecurityManager());
    }

    public void checkPermission(final Permission perm) {
        assert perm != null;

        log.trace("Checking permission of: {}", perm);

        //
        // TODO: See if there is a more efficent and/or recommended way to implement custom permission handling
        //

        //
        // FIXME: These don't work as desired ATM, so disable and re-implement a little bit later
        //
        
        /*
        if (perm instanceof RuntimePermission) {
            // Prevent System.exit()
            if (perm.implies(new RuntimePermission("exitVM"))) {
                throw new SecurityException();
            }

            // Prevent unhijacking of the system streams
            if (perm.implies(new RuntimePermission("setIO"))) {
                throw new SecurityException();
            }
        }

        if (perm instanceof PropertyPermission) {
            // Never allow application to change ${gshell.home}
            if (perm.implies(new PropertyPermission("gshell.home", "write"))) {
                throw new SecurityException();
            }
        }
        */
        
        if (parent != null) {
            parent.checkPermission(perm);
        }
    }
}