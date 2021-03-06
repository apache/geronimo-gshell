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
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.Closeable;

/**
 * Bean container context.
 *
 * This is basically a merged and trimmed down version of a Spring ConfigurableApplicationContext.
 *
 * @version $Rev$ $Date$
 */
public interface BeanContainerContext
    extends ListableBeanFactory, HierarchicalBeanFactory, ResourcePatternResolver, Closeable
{
	String getId();

	BeanContainerContext getParent();

	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);

	void refresh() throws BeansException, IllegalStateException;

	boolean isActive();

	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;
}