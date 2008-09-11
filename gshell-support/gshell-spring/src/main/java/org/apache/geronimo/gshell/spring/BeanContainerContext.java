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

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Custom Spring {@link org.springframework.context.ApplicationContext} for {@link BeanContainer} instances.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerContext
    extends GenericApplicationContext
{
    private static final String[] CONFIG_LOCATIONS = {
        "classpath*:META-INF/spring/components.xml"
    };

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<Resource> ownedResources = new HashSet<Resource>();

    public BeanContainerContext(final ClassLoader classLoader) {
        this(classLoader, null);
    }

    public BeanContainerContext(final ClassLoader classLoader, BeanContainerContext parent) {
        super(parent);
        assert classLoader != null;

        setClassLoader(classLoader);

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

        for (String location : CONFIG_LOCATIONS) {
            Resource[] resources;
            try {
                resources = getResources(location);
            }
            catch (IOException e) {
                throw new FatalBeanException("Failed to load resources from location: " + location, e);
            }

            if (parent != null) {
                resources = parent.filterOwnedResources(resources);
            }

            // Track which resources we own
            ownedResources.addAll(Arrays.asList(resources));
            
            reader.loadBeanDefinitions(resources);
        }
    }

    /**
     * Filter owned resources from the given resources list.
     */
    private Resource[] filterOwnedResources(final Resource[] resources) {
        assert resources != null;

        List<Resource> list = new ArrayList<Resource>();

        for (Resource resource : resources) {
            if (ownedResources.contains(resource)) {
                log.debug("Filtered owned resource: {}", resource);
            }
            else {
                list.add(resource);
            }
        }

        return list.toArray(new Resource[list.size()]);
    }

    @Override
    public void addListener(final ApplicationListener listener) {
        assert listener != null;
        
        super.addListener(listener);
    }

    public void addBeanPostProcessor(final BeanPostProcessor processor) {
        assert processor != null;

        addBeanFactoryPostProcessor(new BeanFactoryPostProcessor()
        {
            public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
                assert beanFactory != null;

                beanFactory.addBeanPostProcessor(processor);
            }
        });
    }

    /*
    @Override
    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
	}
    */
}