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

package org.apache.geronimo.gshell.commands.builtins;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Set a variable or property.
 *
 * @version $Rev$ $Date$
 */
public class SetAction
    implements CommandAction
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    enum Mode
    {
        VARIABLE,
        PROPERTY
    }

    @Option(name="-m", aliases={"--mode"})
    private Mode mode = Mode.VARIABLE;

    @Argument
    private List<String> args = null;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        IO io = context.getIo();
        Variables variables = context.getVariables();

        // No args... list all properties or variables
        if (args == null || args.size() == 0) {
            switch (mode) {
                case PROPERTY: {
                    Properties props = System.getProperties();

                    for (Object o : props.keySet()) {
                        String name = (String) o;
                        String value = props.getProperty(name);

                        io.out.print(name);
                        io.out.print("=");
                        io.out.print(value);
                        io.out.println();
                    }
                    break;
                }

                case VARIABLE: {
                    Iterator<String> iter = variables.names();

                    while (iter.hasNext()) {
                        String name = iter.next();
                        Object value = variables.get(name);

                        io.out.print(name);
                        io.out.print("=");
                        io.out.print(value);
                        io.out.println();
                    }
                    break;
                }
            }

            return Result.SUCCESS;
        }

        //
        // FIXME: This does not jive well with the parser, and stuff like foo = "b a r"
        //

        //
        // NOTE: May want to make x=b part of the CL grammar
        //

        for (Object arg : args) {
            String namevalue = String.valueOf(arg);

            switch (mode) {
                case PROPERTY:
                    setProperty(namevalue);
                    break;

                case VARIABLE:
                    setVariable(variables, namevalue);
                    break;
            }
        }

        return Result.SUCCESS;
    }

    class NameValue
    {
        String name;
        String value;
    }

    private NameValue parse(final String input) {
        NameValue nv = new NameValue();

        int i = input.indexOf("=");
        int firstDoubleQuote = input.indexOf("\"");
        int firstSingleQuote = input.indexOf("'");

        if (i == -1) {
            nv.name = input;
            nv.value = "true";
        }
        else if ( firstDoubleQuote != -1) {
        	nv.name = input.substring(0,i);
        	nv.value = input.substring(firstDoubleQuote + 1, input.length()-1); 
        } 
        else if ( firstSingleQuote != -1) {
        	nv.name = input.substring(0,i);
        	nv.value = input.substring(firstSingleQuote + 1, input.length()-1); 
        } 
        else {
            nv.name = input.substring(0, i);
            nv.value = input.substring(i + 1, input.length());
        }

        nv.name = nv.name.trim();

        return nv;
    }

    private void setProperty(final String namevalue) {
        NameValue nv = parse(namevalue);

        log.info("Setting system property: {}={}", nv.name, nv.value);

        System.setProperty(nv.name, nv.value);
    }

    private void setVariable(final Variables vars, final String namevalue) {
        NameValue nv = parse(namevalue);

        log.info("Setting variable: {}={}", nv.name, nv.value);

        // Command vars always has a parent, set only makes sence when setting in parent's scope
        vars.parent().set(nv.name, nv.value);
    }
}
