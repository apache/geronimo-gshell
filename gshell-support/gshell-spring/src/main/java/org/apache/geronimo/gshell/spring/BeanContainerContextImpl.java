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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.apache.geronimo.gshell.chronos.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * {@link BeanContainerContext} implementation used by {@link BeanContainerImpl} instances.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerContextImpl
    extends BeanContainerContextSupport
{
    private final Set<Resource> resources = new LinkedHashSet<Resource>();

    public BeanContainerContextImpl(final ClassLoader classLoader) {
        this(classLoader, null);
    }

    public BeanContainerContextImpl(final ClassLoader classLoader, final BeanContainerContextImpl parent) {
        super(parent);
        assert classLoader != null;

        setClassLoader(classLoader);
    }

    public void configure(final String[] locations) {
        assert locations != null;

        StopWatch watch = new StopWatch(true);

        log.debug("Configuring with locations: {}", locations);

        for (String location : locations) {
            Resource[] resources;
            try {
                resources = getResources(location);
            }
            catch (IOException e) {
                throw new BeanContainerContextException("Failed to load resources from location: " + location, e);
            }

            BeanContainerContext parent = getParent();
            if (parent instanceof BeanContainerContextImpl) {
                resources = ((BeanContainerContextImpl)parent).filterOwnedResources(resources);
            }

            // Track which resources we own
            this.resources.addAll(Arrays.asList(resources));
        }

        log.debug("Configured {} resources in: {}", resources.size(), watch);
    }

    /**
     * Filter owned resources from the given resources list.
     */
    private Resource[] filterOwnedResources(final Resource[] resources) {
        assert resources != null;

        List<Resource> list = new ArrayList<Resource>();

        for (Resource resource : resources) {
            if (this.resources.contains(resource)) {
                log.trace("Filtered owned resource: {}", resource);
            }
            else {
                list.add(resource);
            }
        }

        Resource[] filteredResources = list.toArray(new Resource[list.size()]);

        // If we have a parent, then ask it to filter resources as well
        BeanContainerContext parent = getParent();
        if (parent instanceof BeanContainerContextImpl) {
            filteredResources = ((BeanContainerContextImpl)parent).filterOwnedResources(filteredResources);
        }

        return filteredResources;
    }

    protected void loadBeanDefinitions(final XmlBeanDefinitionReader reader) throws BeansException, IOException {
        assert reader != null;

        log.debug("Loading bean definitions from resources: {}", resources);

        reader.loadBeanDefinitions(resources.toArray(new Resource[resources.size()]));
    }

    public void addBeanPostProcessor(final BeanPostProcessor processor) {
        assert processor != null;

        addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
                assert beanFactory != null;

                if (processor instanceof BeanFactoryAware) {
                    ((BeanFactoryAware)processor).setBeanFactory(beanFactory);
                }

                beanFactory.addBeanPostProcessor(processor);
            }
        });
    }
}
