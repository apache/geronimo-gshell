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

/**
 * ???
 *
 * @version $Id$
 */
public interface Command
{
    /** Standard command success status code. */
    int SUCCESS = 0;
    
    /** Standard command failure status code. */
    int FAILURE = -1;
    
    String getName();
    
    void init(CommandContext context); // throws Exception ?

    //
    // TODO: Return an Object!
    //
    
    int execute(String[] args) throws Exception;
    
    void abort(); // throws Exception ?
    
    void destroy(); // throws Exception ?

    //
    // 'help' command helpers to allow external inspection of command help
    //

    // String usage() // single line used to render help page

    // String about() // single line to describe the command

    // String help() // full help page (includes usage + about + command line options)
}
