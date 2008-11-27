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

package org.apache.geronimo.gshell.wisdom.shell;

import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.shell.ShellContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Console.ErrorHandler} component.
 *
 * @version $Rev$ $Date$
 */
public class ConsoleErrorHandlerImpl
    implements Console.ErrorHandler
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ConsoleErrorHandlerImpl() {
    }

    public Result handleError(final Throwable error) {
        assert error != null;

        displayError(error);

        return Result.CONTINUE;
    }

    private void displayError(final Throwable error) {
        assert error != null;

        // Decode any error notifications
        Throwable cause = error;
        if (error instanceof ErrorNotification) {
            cause = error.getCause();
        }

        IO io = ShellContextHolder.get().getIo();

        // Spit out the terse reason why we've failed
        io.err.print("@|bold,red ERROR| ");
        io.err.print(cause.getClass().getSimpleName());
        io.err.println(": @|bold,red " + cause.getMessage() + "|");

        // Determine if the stack trace flag is set
        String stackTraceProperty = System.getProperty("gshell.show.stacktrace");
        boolean stackTraceFlag = false;
        if (stackTraceProperty != null) {
        	stackTraceFlag = stackTraceProperty.trim().equals("true");
        }

        if (io.isDebug()) {
            // If we have debug enabled then skip the fancy bits below, and log the full error, don't decode shit
            log.debug(error.toString(), error);
        }
        else if (io.isVerbose() || stackTraceFlag) {
            // Render a fancy ansi colored stack trace
            StackTraceElement[] trace = cause.getStackTrace();
            StringBuilder buff = new StringBuilder();

            //
            // TODO: Move this to helper in gshell-ansi
            //

            for (StackTraceElement e : trace) {
                buff.append("        @|bold at| ").
                    append(e.getClassName()).
                    append(".").
                    append(e.getMethodName()).
                    append(" (@|bold ");

                buff.append(e.isNativeMethod() ? "Native Method" :
                        (e.getFileName() != null && e.getLineNumber() != -1 ? e.getFileName() + ":" + e.getLineNumber() :
                            (e.getFileName() != null ? e.getFileName() : "Unknown Source")));

                buff.append("|)");

                //
                // FIXME: This does not properly display the full exception detail when cause contains nested exceptions
                //

                io.err.println(buff);

                buff.setLength(0);
            }
        }
        io.err.flush();
    }
}