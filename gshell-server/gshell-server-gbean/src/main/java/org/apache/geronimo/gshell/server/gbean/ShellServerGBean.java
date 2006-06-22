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

package org.apache.geronimo.gshell.server.gbean;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Shell server GBean.
 *
 * @version $Id$
 */
public class ShellServerGBean
    implements GBeanLifecycle
{
    private static final Log log = LogFactory.getLog(ShellServerGBean.class);

    //
    // GBeanLifecycle
    //

    public void doStart() throws Exception {
        //
        // TODO:
        //
    }

    public void doStop() throws Exception {
        //
        // TODO:
        //
    }

    public void doFail() {
        //
        // TODO:
        //
    }

    //
    // GBeanInfo
    //

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ShellServerGBean", ShellServerGBean.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
