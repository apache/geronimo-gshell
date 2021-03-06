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

import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandCompleter;
import org.apache.geronimo.gshell.command.CommandDocumenter;
import org.apache.geronimo.gshell.i18n.MessageSource;

/**
 * Stateless {@link org.apache.geronimo.gshell.command.Command} component.
 *
 * @version $Rev$ $Date$
 */
public class StatelessCommand
    extends CommandSupport
{
    // Expose some of our super-classes properties for spring configuration

    @Override
    public void setAction(final CommandAction action) {
        super.setAction(action);
    }

    @Override
    public void setDocumenter(final CommandDocumenter documenter) {
        super.setDocumenter(documenter);
    }

    @Override
    public void setCompleter(final CommandCompleter completer) {
        super.setCompleter(completer);
    }

    @Override
    public void setMessages(final MessageSource messages) {
        super.setMessages(messages);
    }
}