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

package org.apache.geronimo.gshell.commands.log4j;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.CommandAction;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.io.IO;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to query/modify {@link Logger} configuration.
 *
 * @version $Rev$ $Date$
 */
public class LoggerAction
    implements CommandAction
{
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    @Argument(index=0, required=true)
    private String loggerName;

    @Argument(index=1)
    private String levelName;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        Logger logger = getLogger(loggerName);

        if (levelName == null) {
            io.info("{}", logger.getLevel());
        }
        else {
            Level level = Level.toLevel(levelName);
            logger.setLevel(level);

            log.debug("Set logger '{}' to level '{}'", logger.getName(), level);
        }

        return Result.SUCCESS;
    }

    private Logger getLogger(final String name) {
        assert name != null;

        Logger logger;

        if (loggerName.equals("/")) {
            logger = Logger.getRootLogger();
        }
        else {
            logger = Logger.getLogger(loggerName);
        }

        return logger;
    }
}