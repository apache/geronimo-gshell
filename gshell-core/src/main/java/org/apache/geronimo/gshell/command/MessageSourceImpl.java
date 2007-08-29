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

package org.apache.geronimo.gshell.command;

import java.util.Formatter;
import java.util.ResourceBundle;

//
// TODO: Move this to util or something, it is generally useful
//

/**
 * Message source backed up by a {@link ResourceBundle}.
 *
 * @version $Rev$ $Date$
 */
public class MessageSourceImpl
    implements MessageSource
{
    //
    // TODO: Add a global message set that is overridden by command messages
    //
    
    private final ResourceBundle bundle;

    public MessageSourceImpl(final String name) {
        assert name != null;
        
        bundle = ResourceBundle.getBundle(name);
    }

    public String getMessage(final String code) {
        return bundle.getString(code);
    }

    public String getMessage(final String code, final Object... args) {
        String format = getMessage(code);

        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        
        f.format(format, args);

        return sb.toString();
    }
}