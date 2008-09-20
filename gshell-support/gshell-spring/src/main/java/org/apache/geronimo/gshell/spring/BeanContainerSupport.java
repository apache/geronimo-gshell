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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.Map;

/**
 * Support for {@link BeanContainer} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class BeanContainerSupport
    implements BeanContainer
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected abstract ConfigurableApplicationContext getContext();

    public void start() {
        log.trace("Starting");

        getContext().start();
    }

    public void stop() {
        log.trace("Stopping");

        getContext().stop();
    }

    public void close() {
        log.trace("Closing");

        getContext().close();
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

        return (Map<String,T>)getContext().getBeansOfType(type);
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
}