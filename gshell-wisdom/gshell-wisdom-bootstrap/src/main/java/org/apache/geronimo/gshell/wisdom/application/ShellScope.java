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

import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.ObjectFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ShellScope
    implements Scope
{
    public Object get(final String name, final ObjectFactory objectFactory) {
        return null;
    }

    public Object remove(final String name) {
        return null;
    }

    public void registerDestructionCallback(final String name, final Runnable callback) {

    }

    public String getConversationId() {
        return null;
    }
}