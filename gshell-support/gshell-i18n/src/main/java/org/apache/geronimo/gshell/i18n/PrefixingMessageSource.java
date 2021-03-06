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

package org.apache.geronimo.gshell.i18n;

/**
 * A message source which prefixes message codes.
 *
 * @version $Rev$ $Date$
 */
public class PrefixingMessageSource
    implements MessageSource
{
    private final MessageSource messages;

    private final String prefix;

    public PrefixingMessageSource(final MessageSource messages, final String prefix) {
        assert messages != null;
        assert prefix != null;

        this.messages = messages;
        this.prefix = prefix;
    }

    protected String createCode(final String code) {
        assert code != null;
        
        return prefix + code;
    }

    public String getMessage(final String code) {
        return messages.getMessage(createCode(code));
    }

    public String format(final String code, final Object... args) {
        return messages.format(createCode(code), args);
    }
}