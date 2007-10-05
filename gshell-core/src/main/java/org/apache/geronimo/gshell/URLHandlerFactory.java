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

package org.apache.geronimo.gshell;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to URL internals.
 *
 * @version $Rev$ $Date$
 */
@Component(role=URLHandlerFactory.class, instantiationStrategy="singleton-keep-alive")
public class URLHandlerFactory
    implements Initializable
{
    private static URLHandlerFactory SINGLETON;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Factory factory = new Factory();

    @Requirement(role=URLStreamHandler.class)
    private Map<String,URLStreamHandler> handlers;

    public URLHandlerFactory() {
        // Just sanity check that only one of these puppies gets constructed... ever
        synchronized (URLHandlerFactory.class) {
            if (SINGLETON != null) {
                throw new IllegalStateException("Singleton instance already constructed");
            }
            SINGLETON = this;
        }
    }

    public void initialize() throws InitializationException {
        try {
            URL.setURLStreamHandlerFactory(factory);
            
            log.debug("URL stream handler factory installed");
        }
        catch (Throwable t) {
            throw new InitializationException("Failed to install URL stream handler factory", t);
        }

        // Log the initial handlers which were injected
        if (!handlers.isEmpty()) {
            log.debug("Initial URL stream handlers:");
            for (Map.Entry entry : handlers.entrySet()) {
                log.debug("    {} -> {}", entry.getKey(), entry.getValue());
            }
        }
        else {
            log.warn("No URL stream handlers are currently registered; somethings probably misconfigured");
        }
    }

    public void register(final String protocol, final URLStreamHandler handler) {
        factory.register(protocol, handler);
    }

    public URLStreamHandler getHandler(final String protocol) {
        return factory.getHandler(protocol);
    }

    public Map<String,URLStreamHandler> handlers() {
        return factory.handlers();
    }

    /*
    public static void forceInstall() throws Error, SecurityException {
        if (!installed) {
            // This way is "naughty" but works great
            Throwable t = (Throwable) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        // get a reference to the URL stream handler lock... we need to
                        // synchronize on this field to be safe
                        Field streamHandlerLockField = URL.class.getDeclaredField("streamHandlerLock");
                        streamHandlerLockField.setAccessible(true);
                        Object streamHandlerLock = streamHandlerLockField.get(null);

                        synchronized (streamHandlerLock) {
                            // get a reference to the factory field and change the permissions
                            // to make it accessable (factory is a package protected field)
                            Field factoryField = URL.class.getDeclaredField("factory");
                            factoryField.setAccessible(true);

                            // get a reference to the handlers field and change the permissions
                            // to make it accessable (handlers is a package protected field)
                            Field handlersField = URL.class.getDeclaredField("handlers");
                            handlersField.setAccessible(true);

                            // the the handlers map first
                            Map handlers = (Map) handlersField.get(null);

                            // set the factory field to our factory
                            factoryField.set(null, factory);

                            // clear the handlers
                            handlers.clear();
                        }
                    } catch (Throwable e) {
                        return e;
                    }
                    return null;
                }
            });

            if (t != null) {
                if (t instanceof SecurityException) {
                    throw (SecurityException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                }
                throw new Error("Unknown error while force installing URL factory", t);
            }
            installed = true;
        }
    }
    */

    //
    // Factory
    //

    private class Factory
        implements URLStreamHandlerFactory
    {
        private final List<String> handlerPackages = new LinkedList<String>();

        private Factory() {
            // Add the packages listed in the standard system property
            String systemPackages = System.getProperty("java.protocol.handler.pkgs");

            if (systemPackages != null) {
                StringTokenizer stok = new StringTokenizer(systemPackages, "|");

                while (stok.hasMoreTokens()) {
                    handlerPackages.add(stok.nextToken().trim());
                }
            }

            // Always add the sun handlers
            handlerPackages.add("sun.net.www.protocol");
        }

        public URLStreamHandler createURLStreamHandler(String protocol) {
            assert protocol != null;

            protocol = protocol.trim();

            log.trace("Create URL stream handler: {}", protocol);

            URLStreamHandler handler;

            // First check the registered handlers
            synchronized (this) {
                handler = handlers.get(protocol);
            }

            if (handler != null) {
                log.trace("Using registered handler: {}", handler);

                return handler;
            }
            
            // Try to get the stream handler from the registered package list
            Class<?> type = findProtocolHandler(protocol);
            
            if (type == null) {
                throw new IllegalArgumentException("Unknown protocol: " + protocol);
            }

            try {
                return (URLStreamHandler) type.newInstance();
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Failed to construct handler for protocol: " + protocol, e);
            }
        }

        private synchronized void register(final String protocol, final URLStreamHandler handler) {
            assert protocol != null;
            assert handler != null;

            if (handlers.containsKey(protocol)) {
                throw new IllegalStateException("Protocol already has a registered handler: " + protocol);
            }

            handlers.put(protocol, handler);

            log.debug("Registered {} -> {}", protocol, handler);
        }

        private synchronized URLStreamHandler getHandler(final String protocol) {
            assert protocol != null;

            return handlers.get(protocol);
        }

        private synchronized Map<String,URLStreamHandler> handlers() {
            return Collections.unmodifiableMap(handlers);
        }
        
        private Class<?> findProtocolHandler(final String protocol) {
            assert protocol != null;

            log.trace("Finding protocol handler: {}", protocol);
            
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }

            for (String pkg : handlerPackages) {
                String classname = pkg + "." + protocol + ".Handler";

                try {
                    return cl.loadClass(classname);
                }
                catch (Throwable ignore) {}
            }

            return null;
        }
    }
}
