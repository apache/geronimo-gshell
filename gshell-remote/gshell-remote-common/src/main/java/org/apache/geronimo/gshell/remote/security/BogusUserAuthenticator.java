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

package org.apache.geronimo.gshell.remote.security;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A super bogus user authenticator which simply agrees to anything you give it,
 * unless the username or password is "bogus", which it will reject.
 *
 * @version $Rev$ $Date$
 */
@Component(role=UserAuthenticator.class, hint="default") // FIXME: hint="bogus")
public class BogusUserAuthenticator
    implements UserAuthenticator
{
    private Logger log = LoggerFactory.getLogger(getClass());

    //
    // TODO: Maybe we can use some JAAS crap or something instead of this... ?
    //
    
    public boolean authenticate(final String username, final String password) {
        assert username != null;
        assert password != null;

        log.info("Authenticating; username={}, password={}", username, StringUtils.repeat("*", password.length()));

        // Unless the username or password is "bogus", then successfully authenticate
        if ("bogus".equals(username)) {
            return false;
        }
        else if ("bogus".equals(password)) {
            return false;
        }

        return true;
    }
}