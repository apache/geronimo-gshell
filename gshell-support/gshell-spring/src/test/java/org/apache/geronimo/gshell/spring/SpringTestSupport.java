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
 * Support for Spring-based tests.
 *
 * @version $Rev$ $Date$
 */
public abstract class SpringTestSupport
    extends TestSupport
{
    private BeanContainerImpl container;

    protected void setUp() throws Exception {
        container = new BeanContainerImpl(getClass().getClassLoader());
        configure(container);
    }
    
    protected void tearDown() throws Exception {
        container = null;
    }

    protected void configure(final BeanContainerImpl container) throws Exception {
        container.loadBeans(getConfigLocations());
    }

    protected String getDefaultConfigLocation() {
        return "classpath:" + getClass().getName().replace('.', '/') + "-context.xml";
    }

    protected String getDefaultComponentsConfigLocation() {
        return "classpath*:META-INF/spring/components.xml";
    }
    
    protected String[] getConfigLocations() {
        return new String[] {
            getDefaultConfigLocation()
        };
    }

    protected BeanContainerImpl getBeanContainer() {
        if (container == null) {
            throw new IllegalStateException();
        }
        return container;
    }

    protected <T> T getBean(final Class<T> type) {
        return getBeanContainer().getBean(type);
    }

    protected <T> T getBean(final String name, final Class<T> requiredType) {
        return getBeanContainer().getBean(name, requiredType);    
    }
}