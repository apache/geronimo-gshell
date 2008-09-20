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

package org.apache.geronimo.gshell.whisper;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.apache.geronimo.gshell.spring.BeanContainerAwareProcessor;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.net.URL;

/**
 * Suport for Spring-based tests.
 *
 * @version $Rev$ $Date$
 */
public abstract class SpringTestSupport
    extends AbstractDependencyInjectionSpringContextTests
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:" + getClass().getName().replace('.', '/') + "-context.xml"
        };
    }

    private BeanContainer container;

    protected void prepareApplicationContext(final GenericApplicationContext context) {
        assert context != null;
        
	    log.debug("Prepare applciation context");

        //
        // FIXME: Need to refactor the BeanContainerImpl muck to allow some of this to be shared
        //
        
        container = new BeanContainer() {
            public BeanContainer getParent() {
                return null;
            }

            public ClassRealm getClassRealm() {
                throw new UnsupportedOperationException();
            }

            public void start() {
                context.start();
            }

            public void stop() {
                context.stop();
            }

            public void close() {
                context.close();
            }

            public <T> T getBean(Class<T> type) {
                throw new UnsupportedOperationException();
            }

            @SuppressWarnings({"unchecked"})
            public <T> T getBean(String name, Class<T> requiredType) {
                return (T)context.getBean(name, requiredType);
            }

            @SuppressWarnings({"unchecked"})
            public <T> Map<String, T> getBeans(Class<T> type) {
                return (Map<String, T>)context.getBeansOfType(type);
            }

            public String[] getBeanNames() {
                return context.getBeanDefinitionNames();
            }

            public String[] getBeanNames(Class type) {
                return context.getBeanNamesForType(type);
            }

            public BeanContainer createChild(String id, List<URL> classPath) {
                throw new UnsupportedOperationException();
            }

            public BeanContainer createChild(String id) {
                return createChild(id, null);
            }
        };
    }

    protected void customizeBeanFactory(final DefaultListableBeanFactory beanFactory) {
        assert beanFactory != null;

        log.debug("Customize bean factory");

        assert container != null;
        beanFactory.addBeanPostProcessor(new BeanContainerAwareProcessor(container));
	}
}