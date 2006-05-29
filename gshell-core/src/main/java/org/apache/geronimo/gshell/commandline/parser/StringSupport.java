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

package org.apache.geronimo.gshell.commandline.parser;

/**
 * Support for string types.
 *
 * @version $Id$
 */
public abstract class StringSupport
    extends SimpleNode
{
    protected Token token;

    public StringSupport(final int id) {
        super(id);
    }

    public StringSupport(final CommandLineParser p, final int id) {
        super(p, id);
    }

    public void setToken(final Token token) {
        assert token != null;

        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        Token t = getToken();
        if (t == null) {
            throw new IllegalStateException("Token not set");
        }

        return t.image;
    }

    public String toString() {
        return super.toString() + "( " + getToken() + " )";
    }

    /**
     * Returns an unquoted value.
     *
     * @param value     String to unquote, must not be null; length must be at least 2
     * @return          Unquoted value
     */
    protected String unquote(String value) {
        assert value != null;
        assert value.length() >= 2;

        return value.substring(1, value.length() - 1);
    }
}