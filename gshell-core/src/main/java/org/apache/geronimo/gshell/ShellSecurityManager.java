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

package org.apache.geronimo.gshell;

import java.security.Permission;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ShellSecurityManager
    extends SecurityManager
{
    private final SecurityManager parent;

    public ShellSecurityManager(final SecurityManager parent) {
        assert parent != null;

        this.parent = parent;
    }

    public ShellSecurityManager() {
        this(System.getSecurityManager());
    }

    public void checkPermission(final Permission perm) {
        if (parent != null) {
            parent.checkPermission(perm);
        }
    }

    public void checkExit(final int code) {
        super.checkExit(code);

        throw new SecurityException("Use of System.exit() if forbidden!");
    }

    /*
    public void checkPermission(final Permission perm) {
        assert perm != null;
        
        if (perm.getName().equals("exitVM")) {
            System.out.println("exitVM");
        }
    }
    */
}