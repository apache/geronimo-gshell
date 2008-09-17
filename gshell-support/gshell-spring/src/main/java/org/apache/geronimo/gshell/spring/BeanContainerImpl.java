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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Default {@link BeanContainer} implementation.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerImpl
    implements BeanContainer
{
    private static final String REALM_ID = "gshell";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private BeanContainer parent;

    private BeanContainerContext context;

    private ClassRealm classRealm;
    
    public BeanContainerImpl(final ClassLoader classLoader) {
        assert classLoader != null;

        ClassRealm realm;
        try {
            realm = new ClassWorld().newRealm(REALM_ID, classLoader);
        }
        catch (DuplicateRealmException e) {
            // Should never happen
            throw new Error(e);
        }

        configureContext(realm, null);
    }

    /**
     * Child container constructor.
     */
    private BeanContainerImpl(final ClassRealm classRealm, final BeanContainerImpl parent) {
        assert parent != null;
        assert classRealm != null;

        configureContext(classRealm, parent);
    }

    private void configureContext(final ClassRealm classRealm, final BeanContainerImpl parent) {
        assert classRealm != null;
        // parent may be null

        this.parent = parent;
        this.classRealm = classRealm;

        // Construct the container and add customizations
        context = new BeanContainerContext(classRealm, parent != null ? parent.context : null);
        context.registerShutdownHook();
        context.addBeanPostProcessor(new BeanContainerAwareProcessor(this));

        // Refresh to load things up
        context.refresh();

        // Start components
        context.start();
    }

    public BeanContainer getParent() {
        return parent;
    }

    public <T> T getBean(final Class<T> type) {
        assert type != null;

        String[] names = context.getBeanNamesForType(type);

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

        return (T) context.getBean(name, requiredType);
    }

    @SuppressWarnings({"unchecked"})
    public <T> Map<String,T> getBeans(final Class<T> type) {
        assert type != null;

        return (Map<String,T>)context.getBeansOfType(type);
    }

    public String[] getBeanNames(final Class type) {
        assert type != null;

        return context.getBeanNamesForType(type);
    }

    public BeanContainer createChild(final String id, final List<URL> classPath) throws DuplicateRealmException {
        assert id != null;
        assert classPath != null;

        log.debug("Creating child container: {}", id);
        
        ClassRealm childRealm = classRealm.createChildRealm(id);

        for (URL url : classPath) {
            childRealm.addURL(url);
        }

        return new BeanContainerImpl(childRealm, this);
    }
}