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

package org.apache.geronimo.gshell.wisdom.config;

import org.apache.geronimo.gshell.spring.LoggingProcessor;
import org.apache.geronimo.gshell.spring.SpringTestSupport;

/**
 * Unit tests for the {@link LoggingProcessor} class.
 *
 * @version $Rev: 698145 $ $Date: 2008-09-23 18:39:31 +0700 (Tue, 23 Sep 2008) $
 */
public class PluginParserTest
    extends SpringTestSupport
{
    /*
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:META-INF/spring/components.xml",
            "classpath:" + getClass().getName().replace('.', '/') + "-context.xml"
        };
    }
    */
    
    public void testParser() throws Exception {
        LoggingProcessor processor = new LoggingProcessor();
        String xml = processor.render(applicationContext.getBeanFactory());
        assertNotNull(xml);

        System.out.println("XML:\n" + xml);
    }
}