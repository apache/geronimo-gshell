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

package org.apache.geronimo.gshell.bootstrap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Bootstrap launcher.
 *
 * @version $Rev$ $Date$
 */
public class Launcher
{
    private static final int SUCCESS_EXIT_CODE = 0;

    private static final int FAILURE_EXIT_CODE = 100;

    private static final String MAIN_CLASS = "org.apache.geronimo.gshell.cli.Main";

    public static void main(final String[] args) {
        assert args != null;

        Configuration config = new Configuration();

        try {
            Log.debug("Configuring");
            
            config.configure();

            ClassLoader cl = config.getClassLoader();
            Class type = cl.loadClass(MAIN_CLASS);
            Method method = getMainMethod(type);

            Thread.currentThread().setContextClassLoader(cl);

            if (Log.DEBUG) {
                Log.debug("Launching: " + method);
            }

            method.invoke(null, new Object[] { args });

            Log.debug("Exiting");

            System.exit(SUCCESS_EXIT_CODE);
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
            System.err.flush();
            System.exit(FAILURE_EXIT_CODE);
        }
    }

    private static Method getMainMethod(final Class type) throws Exception {
        assert type != null;

        Method m = type.getMethod("main", String[].class);

        int modifiers = m.getModifiers();

        if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                return m;
            }
        }

        throw new NoSuchMethodException("public static void main(String[] args) in " + type);
    }
}
