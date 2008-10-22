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

package org.apache.geronimo.gshell.spring;

/**
 * Unit tests for the {@link BeanContainerAwareProcessor} class.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerAwareProcessorTest
    extends SpringTestSupport
{
    public void testProcessor() throws Exception {
        Target target = getBeanContainer().getBean("target", Target.class);
        assertNotNull(target);
        assertNotNull(target.container);
    }

    //
    // Target
    //

    public static class Target
        implements BeanContainerAware
    {
        public BeanContainer container;

        public boolean initialized = false;

        public void setBeanContainer(BeanContainer container) {
            this.container = container;
        }
    }
}