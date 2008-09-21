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

package org.apache.geronimo.gshell.whisper.transport.base;

import org.apache.geronimo.gshell.yarn.Yarn;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.ThreadModel;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class BaseConfiguration
{
    private IoHandler handler;

    private ThreadModel threadModel;

    protected BaseConfiguration() {}

    public void setHandler(final IoHandler handler) {
        this.handler = handler;
    }

    public IoHandler getHandler() {
        return handler;
    }

    public ThreadModel getThreadModel() {
        return threadModel;
    }

    public void setThreadModel(final ThreadModel threadModel) {
        this.threadModel = threadModel;
    }

    public String toString() {
        return Yarn.render(this);
    }

    //
    // TODO: Add a list of filters to tack on
    //
}
