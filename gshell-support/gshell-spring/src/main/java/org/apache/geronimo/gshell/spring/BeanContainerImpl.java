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
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.util.List;

/**
 * Default {@link BeanContainer} implementation.
 *
 * @version $Rev$ $Date$
 */
public class BeanContainerImpl
    extends BeanContainerSupport
{
    private static final String REALM_ID = "gshell";

    private final BeanContainer parent;

    private final BeanContainerContext context;

    private final ClassRealm classRealm;

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

    public BeanContainerImpl(final ClassLoader cl) {
        this(createDefaultClassRealm(cl), null);
    }

    private BeanContainerImpl(final ClassRealm classRealm, final BeanContainerImpl parent) {
        assert classRealm != null;
        // parent may be null

        this.parent = parent;
        this.classRealm = classRealm;

        // Construct the container and add customizations
        context = new BeanContainerContext(classRealm, parent != null ? parent.context : null);
        context.setDisplayName(classRealm.getId());
        context.registerShutdownHook();

        // Attach some processors
        context.addBeanFactoryPostProcessor(new LoggingProcessor());
        context.addBeanPostProcessor(new BeanContainerAwareProcessor(this));

        // Refresh to load things up
        context.refresh();
    }

    protected ConfigurableApplicationContext getContext() {
        return context;
    }

    public BeanContainer getParent() {
        return parent;
    }

    public ClassRealm getClassRealm() {
        return classRealm;
    }

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

    /*
    public Object configure(final Object bean) {
        assert bean != null;

        context.getAutowireCapableBeanFactory().autowireBean(bean);

        return bean;
    }
    */
}