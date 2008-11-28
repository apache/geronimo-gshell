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

package org.apache.geronimo.gshell.commands.shellutils;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.Arguments;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Execute a Java standard application.
 *
 * <p>By default looks for static main(String[]) to execute, but
 * you can specify a different static method that takes a String[]
 * to execute instead.
 *
 * @version $Rev$ $Date$
 */
public class JavaAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(name="-m", aliases={"--method"})
    private String methodName = "main";

    @Argument(index=0, required=true)
    private String className;

    @Argument(index=1)
    private List<String> args;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        log.debug("Loading class: {}", className);
        Class type = Thread.currentThread().getContextClassLoader().loadClass(className);
        log.info("Using type: {}", type);

        log.debug("Locating method: {}", methodName);
        Method method = type.getMethod(methodName, String[].class);
        log.info("Using method: {}", method);

        log.info("Invoking w/arguments: {}", Arguments.asString(args));
        Object result = method.invoke(null, args);
        log.info("Result: {}", result);
        
        return result;
    }
}
