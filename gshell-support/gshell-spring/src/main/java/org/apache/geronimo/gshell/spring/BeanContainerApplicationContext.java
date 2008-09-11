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

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * Custom Spring {@link org.springframework.context.ApplicationContext} for {@link BeanContainer} instances.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerApplicationContext
    extends ClassPathXmlApplicationContext
{
    public BeanContainerApplicationContext(final String[] configLocations) {
        super(configLocations, false);
    }

    public BeanContainerApplicationContext(final String[] configLocations, BeanContainerApplicationContext parent) {
        super(configLocations, false, parent);
    }

    @Override
    public void addListener(final ApplicationListener listener) {
        assert listener != null;
        
        super.addListener(listener);
    }
}