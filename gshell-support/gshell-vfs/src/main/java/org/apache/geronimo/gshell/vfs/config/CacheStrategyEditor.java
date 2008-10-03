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

package org.apache.geronimo.gshell.vfs.config;

import org.apache.commons.vfs.CacheStrategy;

import java.beans.PropertyEditorSupport;

/**
 * Property editor for {@link CacheStrategy} types.
 *
 * @version $Rev$ $Date$
 */
public class CacheStrategyEditor
    extends PropertyEditorSupport
{
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        assert text != null;

        if (text.equalsIgnoreCase("MANUAL")) {
            setValue(CacheStrategy.MANUAL);
        }
        else if (text.equalsIgnoreCase("ON_RESOLVE")) {
            setValue(CacheStrategy.ON_RESOLVE);
        }
        else if (text.equalsIgnoreCase("ON_CALL")) {
            setValue(CacheStrategy.ON_CALL);
        }
        else {
            throw new IllegalArgumentException("Unknown cache strategy: " + text);
        }
    }
}