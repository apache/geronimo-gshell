/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.command;

import java.util.ResourceBundle;
import java.util.Formatter;

/**
 * ???
 *
 * @version $Id$
 */
public class MessageSourceImpl
    implements MessageSource
{
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