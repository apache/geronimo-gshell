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

import org.apache.geronimo.gshell.chronos.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Default {@link BeanContainer} implementation.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerImpl
    implements BeanContainer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ClassLoader classLoader;

    private final BeanContainerImpl parent;

    private final BeanContainerContextImpl context;

    public BeanContainerImpl(final ClassLoader cl) {
        this(cl, null);
    }

    private BeanContainerImpl(final ClassLoader classLoader, final BeanContainerImpl parent) {
        assert classLoader != null;
        // parent may be null

        this.parent = parent;
        this.classLoader = classLoader;

        // Construct the container and add customizations
        context = new BeanContainerContextImpl(classLoader, parent != null ? parent.context : null);

        // Add support for BeanContainerAware
        context.addBeanPostProcessor(new BeanContainerAwareProcessor(this));

        // Hook up annotation processing
        // context.addBeanPostProcessor(new RequiredAnnotationBeanPostProcessor());
        // context.addBeanPostProcessor(new LifecycleProcessor());

        // Add automatic trace logging of loaded beans
        context.addBeanFactoryPostProcessor(new LoggingProcessor());
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public BeanContainer getParent() {
        return parent;
    }

    public BeanContainerContext getContext() {
        return context;
    }

    public void loadBeans(final String[] locations) throws Exception {
        assert locations != null;

        log.debug("Loading beans");

        StopWatch watch = new StopWatch(true);

        context.configure(locations);
        context.refresh();

        log.debug("Loaded beans after: {}", watch);
    }

    public BeanContainer createChild(final String id, final Collection<URL> classPath) {
        assert id != null;
        // classPath may be null

        log.debug("Creating child container: {}", id);

        if (log.isTraceEnabled()) {
            if (classPath != null) {
                log.trace("Classpath:");

                for (URL url : classPath) {
                    log.trace("    {}", url);
                }
            }
        }

        URLClassLoader childLoader;
        if (classPath != null) {
            childLoader = new URLClassLoader(classPath.toArray(new URL[classPath.size()]), classLoader);
        }
        else {
            childLoader = new URLClassLoader(new URL[0], classLoader);
        }

        //
        // FIXME: Id is not used, either use it or drop it
        //
        
        return new BeanContainerImpl(childLoader, this);
    }

    public BeanContainer createChild(final String id) {
        assert id != null;

        return createChild(id, null);
    }
    
    public <T> T getBean(final Class<T> type) {
        assert type != null;

        log.trace("Getting bean of type: {}", type);

        String[] names = getContext().getBeanNamesForType(type);

        if (names.length == 0) {
            throw new NoSuchBeanDefinitionException(type, "No bean defined for type: " + type);
        }
        if (names.length > 1) {
            throw new NoSuchBeanDefinitionException(type, "No unique bean defined for type: " + type + ", found matches: " + Arrays.asList(names));
        }

        return getBean(names[0], type);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getBean(final String name, final Class<T> requiredType) {
        assert name != null;
        assert requiredType != null;

        log.trace("Getting bean named '{}' of type: {}", name, requiredType);

        return (T) getContext().getBean(name, requiredType);
    }

    @SuppressWarnings({"unchecked"})
    public <T> Map<String,T> getBeans(final Class<T> type) {
        assert type != null;

        log.trace("Getting beans of type: {}", type);

        return (Map<String,T>) getContext().getBeansOfType(type);
    }

    public String[] getBeanNames() {
        log.trace("Getting bean names");

        return getContext().getBeanDefinitionNames();
    }

    public String[] getBeanNames(final Class type) {
        assert type != null;

        log.trace("Getting bean names of type: {}", type);

        return getContext().getBeanNamesForType(type);
    }
}