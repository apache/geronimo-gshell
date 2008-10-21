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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Support for {@link BeanContainerContext} implementations.
 *
 * This is basically a merged and trimmed down version of a Spring AbstractXmlApplicationContext.
 *
 * @version $Rev$ $Date$
 */
public abstract class BeanContainerContextSupport
    extends DefaultResourceLoader
    implements BeanContainerContext, DisposableBean
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

	private String id = ObjectUtils.identityToString(this);

	private final BeanContainerContext parent;

	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<BeanFactoryPostProcessor>();

	private boolean active = false;

	private final Object activeMonitor = new Object();

	private final Object startupShutdownMonitor = new Object();

	private final Thread shutdownHook;

	private final ResourcePatternResolver resourcePatternResolver;

	public BeanContainerContextSupport() {
		this(null);
	}

	public BeanContainerContextSupport(final BeanContainerContext parent) {
        // parent could be null

		this.parent = parent;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(this);
        this.shutdownHook = new Thread() {
            public void run() {
                doClose();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

    //
    // BeanContainerContext
    //

	public void setId(final String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public BeanContainerContext getParent() {
		return parent;
	}

	public void refresh() throws BeansException, IllegalStateException {
		synchronized (startupShutdownMonitor) {
			// Prepare this context for refreshing.
			synchronized (activeMonitor) {
                active = true;
            }

            log.debug("Refreshing: {}", this);

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);

			try {
				// Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);

				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);
			}

			catch (BeansException e) {
                log.error("Refresh failed", e);

				// Destroy already created singletons to avoid dangling resources.
				beanFactory.destroySingletons();

				// Reset 'active' flag.
                synchronized (this.activeMonitor) {
                    this.active = false;
                }

				// Propagate exception to caller.
				throw e;
			}
		}
	}

	private ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		refreshBeanFactory();
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();

		log.info("Bean factory for application context [{}]: {}", getId(), ObjectUtils.identityToString(beanFactory));
        log.debug("{} beans defined in {}", beanFactory.getBeanDefinitionCount(), this);

		return beanFactory;
	}

	private void prepareBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
        assert beanFactory != null;

		// Tell the internal bean factory to use the context's class loader.
		beanFactory.setBeanClassLoader(getClassLoader());

		// Populate the bean factory with context-specific resource editors.
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this));

		// BeanFactory interface not registered as resolvable type in a plain factory.
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(BeanContainerContext.class, this);
	}

    private void finishBeanFactoryInitialization(final ConfigurableListableBeanFactory beanFactory) {
        assert beanFactory != null;

		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
	}

    public void addBeanFactoryPostProcessor(final BeanFactoryPostProcessor beanFactoryPostProcessor) {
		beanFactoryPostProcessors.add(beanFactoryPostProcessor);
	}

	private void invokeBeanFactoryPostProcessors(final ConfigurableListableBeanFactory beanFactory) {
        assert beanFactory != null;

		// Invoke factory processors registered with the context instance.
        for (BeanFactoryPostProcessor factoryProcessor : beanFactoryPostProcessors) {
            factoryProcessor.postProcessBeanFactory(beanFactory);
        }

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered, Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();

        for (String name : postProcessorNames) {
            if (isTypeMatch(name, PriorityOrdered.class)) {
                priorityOrderedPostProcessors.add((BeanFactoryPostProcessor) beanFactory.getBean(name));
            }
            else if (isTypeMatch(name, Ordered.class)) {
                orderedPostProcessorNames.add(name);
            }
            else {
                nonOrderedPostProcessorNames.add(name);
            }
        }

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		// noinspection unchecked
        Collections.sort(priorityOrderedPostProcessors, new OrderComparator());
		invokeBeanFactoryPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
        for (String name : orderedPostProcessorNames) {
            orderedPostProcessors.add((BeanFactoryPostProcessor) getBean(name));
        }
        // noinspection unchecked
		Collections.sort(orderedPostProcessors, new OrderComparator());
		invokeBeanFactoryPostProcessors(beanFactory, orderedPostProcessors);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
        for (String name : nonOrderedPostProcessorNames) {
            nonOrderedPostProcessors.add((BeanFactoryPostProcessor) getBean(name));
        }
		invokeBeanFactoryPostProcessors(beanFactory, nonOrderedPostProcessors);
	}

	private void invokeBeanFactoryPostProcessors(final ConfigurableListableBeanFactory beanFactory, final List<BeanFactoryPostProcessor> postProcessors) {
        assert beanFactory != null;
        assert postProcessors != null;

        for (BeanFactoryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
	}

	private void registerBeanPostProcessors(final ConfigurableListableBeanFactory beanFactory) {
        assert beanFactory != null;

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered, Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();

        for (String name : postProcessorNames) {
            if (isTypeMatch(name, PriorityOrdered.class)) {
                priorityOrderedPostProcessors.add((BeanPostProcessor)beanFactory.getBean(name));
            }
            else if (isTypeMatch(name, Ordered.class)) {
                orderedPostProcessorNames.add(name);
            }
            else {
                nonOrderedPostProcessorNames.add(name);
            }
        }

		// First, register the BeanPostProcessors that implement PriorityOrdered.
        // noinspection unchecked
        Collections.sort(priorityOrderedPostProcessors, new OrderComparator());
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<BeanPostProcessor>();
        for (String name : orderedPostProcessorNames) {
            orderedPostProcessors.add((BeanPostProcessor)getBean(name));
        }

        // noinspection unchecked
        Collections.sort(orderedPostProcessors, new OrderComparator());
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Finally, register all other BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
        for (String name : nonOrderedPostProcessorNames) {
            nonOrderedPostProcessors.add((BeanPostProcessor)getBean(name));
        }

		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
	}

	private void registerBeanPostProcessors(final ConfigurableListableBeanFactory beanFactory, final List<BeanPostProcessor> postProcessors) {
        assert beanFactory != null;
        assert postProcessors != null;

        for (BeanPostProcessor postProcessor : postProcessors) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
	}

	public void destroy() {
		close();
	}

	public void close() {
		synchronized (startupShutdownMonitor) {
			doClose();

            Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
	}

	private void doClose() {
		if (isActive()) {
			log.info("Closing: {}", this);

			// Destroy all cached singletons in the context's BeanFactory.
			destroyBeans();

            // Close the state of this context itself.
			closeBeanFactory();

            synchronized (activeMonitor) {
				active = false;
			}
		}
	}

	private void destroyBeans() {
		getBeanFactory().destroySingletons();
	}

	public boolean isActive() {
		synchronized (activeMonitor) {
			return active;
		}
	}

    //
    // BeanFactory
    //

	public Object getBean(final String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	public Object getBean(final String name, final Class requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	public Object getBean(final String name, final Object[] args) throws BeansException {
		return getBeanFactory().getBean(name, args);
	}

	public boolean containsBean(final String name) {
		return getBeanFactory().containsBean(name);
	}

	public boolean isSingleton(final String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	public boolean isPrototype(final String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isPrototype(name);
	}

	public boolean isTypeMatch(final String name, final Class targetType) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isTypeMatch(name, targetType);
	}

	public Class getType(final String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name);
	}

	public String[] getAliases(final String name) {
		return getBeanFactory().getAliases(name);
	}

    //
    // ListableBeanFactory
    //
    
	public boolean containsBeanDefinition(final String name) {
		return getBeanFactory().containsBeanDefinition(name);
	}

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public String[] getBeanNamesForType(final Class type) {
		return getBeanFactory().getBeanNamesForType(type);
	}

	public String[] getBeanNamesForType(final Class type, final boolean includePrototypes, final boolean allowEagerInit) {
		return getBeanFactory().getBeanNamesForType(type, includePrototypes, allowEagerInit);
	}

	public Map getBeansOfType(final Class type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}

	public Map getBeansOfType(final Class type, final boolean includePrototypes, final boolean allowEagerInit) throws BeansException {
		return getBeanFactory().getBeansOfType(type, includePrototypes, allowEagerInit);
	}

    //
    // HierarchicalBeanFactory
    //

	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	public boolean containsLocalBean(final String name) {
		return getBeanFactory().containsLocalBean(name);
	}

    //
    // ResourcePatternResolver
    //
    
	public Resource[] getResources(final String locationPattern) throws IOException {
		return resourcePatternResolver.getResources(locationPattern);
	}

    //
    // BeanPostProcessorChecker
    //

	/**
	 * Logs a message when a bean is created during BeanPostProcessor instantiation.
	 * i.e. when a bean is not eligible for getting processed by all BeanPostProcessors.
	 */
	private class BeanPostProcessorChecker
        implements BeanPostProcessor
    {
		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(final ConfigurableListableBeanFactory beanFactory, final int beanPostProcessorTargetCount) {
            assert beanFactory != null;

			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
			return bean;
		}

		public Object postProcessAfterInitialization(final Object bean, final String beanName) {
            assert bean != null;
            assert beanName != null;

			if (!(bean instanceof BeanPostProcessor) && beanFactory.getBeanPostProcessorCount() < beanPostProcessorTargetCount) {
                log.info("Bean '{}' is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)", beanName);
			}
			return bean;
		}
	}

    //
    // AbstractRefreshableApplicationContext
    //

	private DefaultListableBeanFactory beanFactory;

	private final Object beanFactoryMonitor = new Object();

	private void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}

		try {
			DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory(getParent() != null ? getParent().getBeanFactory() : null);
            beanFactory.setAllowBeanDefinitionOverriding(true);
            beanFactory.setAllowCircularReferences(true);
            beanFactory.setAllowEagerClassLoading(false);
			loadBeanDefinitions(beanFactory);

			synchronized (beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException e) {
			throw new BeanContainerContextException("I/O error parsing XML document for application context: " + this, e);
		}
	}

    private void loadBeanDefinitions(final DefaultListableBeanFactory beanFactory) throws IOException {
        assert beanFactory != null;

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.setResourceLoader(this);
		reader.setEntityResolver(new ResourceEntityResolver(this));
		loadBeanDefinitions(reader);
	}

	protected abstract void loadBeanDefinitions(final XmlBeanDefinitionReader reader) throws BeansException, IOException;

	private void closeBeanFactory() {
		synchronized (beanFactoryMonitor) {
			beanFactory = null;
		}
	}

	private boolean hasBeanFactory() {
		synchronized (beanFactoryMonitor) {
			return beanFactory != null;
		}
	}

	public final ConfigurableListableBeanFactory getBeanFactory() {
		synchronized (beanFactoryMonitor) {
			if (beanFactory == null) {
				throw new IllegalStateException("BeanFactory not initialized or already closed");
			}

			return beanFactory;
		}
	}
}