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

package org.apache.geronimo.gshell.prefs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

/**
 * Processes an object for preference annotations.
 *
 * @version $Rev$ $Date$
 */
public class PreferencesProcessor
{
    public PreferencesProcessor() {}

    public PreferencesProcessor(final Object bean) {
        addBean(bean);
    }

    public void addBean(final Object bean) {
        assert bean != null;

        // TODO:
    }

    //
    // Discovery
    //

    /*
    private void discoverDescriptors() {
        // Recursively process all the methods/fields.
        for (Class type=bean.getClass(); type!=null; type=type.getSuperclass()) {
            // Discover methods
            for (Method method : type.getDeclaredMethods()) {
                Preference pref = method.getAnnotation(Preference.class);
                if (pref != null) {
                    // addOption(new MethodSetter(bean, method), option);
                }
            }

            // Discover fields
            for (Field field : type.getDeclaredFields()) {
                Preference pref = field.getAnnotation(Preference.class);
                if (pref != null) {
                    // addOption(createFieldSetter(field), option);
                }
            }
        }
    }
    */
    
    //
    // Processing
    //

    public void process(final Preferences node) throws ProcessingException {
        assert node != null;
    }
}