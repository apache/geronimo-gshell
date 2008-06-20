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

package org.apache.geronimo.gshell.commands.optional;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.util.Arguments;
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
@CommandComponent(id="gshell-optional:java", description="Execute a Java standard application")
public class JavaCommand
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(name="-m", aliases={"--method"}, metaVar="METHOD", description="Invoke a named method")
    private String methodName = "main";

    @Argument(index=0, metaVar="CLASSNAME", description="The name of the class to invoke", required=true)
    private String className;

    @Argument(index=1, metaVar="ARG", description="Arguments to pass to the METHOD of CLASSNAME")
    private List<String> args;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        
        boolean info = log.isInfoEnabled();

        Class type = Thread.currentThread().getContextClassLoader().loadClass(className);
        if (info) {
            log.info("Using type: " + type);
        }

        Method method = type.getMethod(methodName, String[].class);
        if (info) {
            log.info("Using method: " + method);
        }

        if (info) {
            log.info("Invoking w/arguments: " + Arguments.asString(args));
        }

        Object result = method.invoke(null, args);

        if (info) {
            log.info("Result: " + result);
        }

        return Result.SUCCESS;
    }
}
