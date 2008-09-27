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

package org.apache.geronimo.gshell.wisdom.command;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandAware;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;

/**
 * Command {@link MessageSource}.
 *
 * @version $Rev$ $Date$
 */
public class CommandMessageSource
    implements MessageSource, CommandAware
{
    private Command command;

    private MessageSource messages;

    public void setCommand(final Command command) {
        assert command != null;
        this.command = command;
    }

    //
    // FIXME: See if we can use more gshell: ns glue to automatically configure this message source to *NOT* require the Action instance,
    //        only use the class-name, that should speed up help rendering a lot.
    //
    
    private MessageSource getMessages() {
        if (messages == null) {
            assert command != null;
            messages = new ResourceBundleMessageSource(new Class[] {
                command.getAction().getClass(),
                HelpSupport.class
            });
        }

        return messages;
    }

    public String getMessage(final String code) {
        return getMessages().getMessage(code);
    }

    public String format(final String code, final Object... args) {
        return getMessages().format(code, args);
    }
}
