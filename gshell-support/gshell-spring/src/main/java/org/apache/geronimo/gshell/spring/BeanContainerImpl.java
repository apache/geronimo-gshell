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

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link BeanContainer} implementation.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerImpl
    implements BeanContainer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String REALM_ID = "gshell";

    private final ClassRealm classRealm;
    
    private final BeanContainerImpl parent;

    private final ResourcePatternResolver resourceLoader;

    private final BeanContainerContext context;

    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<BeanFactoryPostProcessor>();

    private final Set<Resource> resources = new LinkedHashSet<Resource>();

    public BeanContainerImpl(final ClassLoader cl) {
        this(createDefaultClassRealm(cl), null);
    }

    private BeanContainerImpl(final ClassRealm classRealm, final BeanContainerImpl parent) {
        assert classRealm != null;
        // parent may be null

        this.parent = parent;
        this.classRealm = classRealm;

        //
        // TODO: Move most of this to BeanContainerContext
        //
        
        // Construct the bean factory
        context = new BeanContainerContext(parent != null ? parent.getContext() : null);
        context.setBeanClassLoader(classRealm);
        context.registerResolvableDependency(BeanFactory.class, context);

        // Setup resource loading
        resourceLoader = new PathMatchingResourcePatternResolver(new DefaultResourceLoader(classRealm));
        context.addPropertyEditorRegistrar(new ResourceEditorRegistrar(resourceLoader));
        context.registerResolvableDependency(ResourceLoader.class, resourceLoader);

        // Add support for BeanContainerAware
        context.addBeanPostProcessor(new BeanContainerAwareProcessor(this));
        context.ignoreDependencyInterface(BeanContainerAware.class);

        // Hook up annotation processing
        context.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
        context.addBeanPostProcessor(new LifecycleProcessor());

        // Add automatic trace logging of loaded beans
        beanFactoryPostProcessors.add(new LoggingProcessor());
    }

    public ClassRealm getClassRealm() {
        return classRealm;
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

        for (String location : locations) {
            Resource[] resources = resourceLoader.getResources(location);

            for (Resource resource : resources) {
                if (parent != null && parent.isOwnedResource(resource)) {
                    log.trace("Omitting resource owned by parent: {}", resource);
                }
                else {
                    log.trace("Adding resource: {}", resource);
                    this.resources.add(resource);
                }
            }
        }

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        reader.setResourceLoader(resourceLoader);
		reader.setEntityResolver(new ResourceEntityResolver(resourceLoader));

        log.debug("Loading bean definitions from {} resources", resources.size());

        reader.loadBeanDefinitions(resources.toArray(new Resource[resources.size()]));

        for (BeanFactoryPostProcessor processor : beanFactoryPostProcessors) {
            processor.postProcessBeanFactory(context);
        }
    }

    private boolean isOwnedResource(final Resource resource) {
        assert resource != null;

        if (resources.contains(resource)) {
            return true;
        }
        else if (parent != null) {
            return parent.isOwnedResource(resource);
        }

        return false;
    }

    //
    // TODO: Bring back start/stop/destroy support
    //

    public BeanContainer createChild(final String id, final List<URL> classPath) {
        assert id != null;
        // classPath may be null

        if (log.isTraceEnabled()) {
            log.trace("Creating child container: {}", id);
            
            if (classPath != null) {
                log.trace("Classpath:");

                for (URL url : classPath) {
                    log.trace("    {}", url);
                }
            }
        }

        ClassRealm childRealm;
        try {
            childRealm = classRealm.createChildRealm(id);
        }
        catch (DuplicateRealmException e) {
            throw new FatalBeanException("Failed to create child container realm: " + id, e);
        }

        if (classPath != null) {
            for (URL url : classPath) {
                childRealm.addURL(url);
            }
        }

        return new BeanContainerImpl(childRealm, this);
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

    public BeanContainer createChild(final String id) {
        assert id != null;

        return createChild(id, null);
    }

    //
    // TODO: See if we can drop the need for this.
    //

    private static ClassRealm createDefaultClassRealm(final ClassLoader cl) {
        assert cl != null;

        try {
            return new ClassWorld().newRealm(REALM_ID, cl);
        }
        catch (DuplicateRealmException e) {
            // Should never happen
            throw new Error(e);
        }
    }
}