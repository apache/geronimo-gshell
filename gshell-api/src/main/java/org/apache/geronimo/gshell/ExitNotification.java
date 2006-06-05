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

package org.apache.geronimo.gshell;

/**
 * Thrown to indicate that the current shell should exit.
 *
 * <p>
 * Commands should use this instead of {@link System#exit}.
 *
 * @version $Id$
 */
public class ExitNotification
    extends Error
{
    ///CLOVER:OFF
    
    //
    // TODO: Need to find a better home for this...
    //
    
    private final int code;

    public ExitNotification(final int code) {
        this.code = code;
    }

    public ExitNotification() {
        this(0);
    }

    public int getCode() {
        return code;
    }
}