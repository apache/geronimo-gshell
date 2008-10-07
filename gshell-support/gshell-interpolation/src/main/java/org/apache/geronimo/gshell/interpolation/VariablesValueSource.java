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

package org.apache.geronimo.gshell.interpolation;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.resolver.FlatResolver;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.codehaus.plexus.interpolation.AbstractValueSource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * {@link org.codehaus.plexus.interpolation.ValueSource} implementation pulling data from a {@link Variables} instance.
 *
 * @version $Rev$ $Date$
 */
public class VariablesValueSource
    extends AbstractValueSource
{
    private final FlatResolver resolver = new FlatResolver(true);

    private final JexlContext jexlContext;

    private Variables variables;

    public VariablesValueSource(final Variables vars) {
        super(false);

        jexlContext = new JexlContext() {
            private final Map map = new VariablesMapAdapter();

            public Map getVars() {
                return map;
            }

            public void setVars(Map map) {
                throw new UnsupportedOperationException();
            }
        };

        if (vars != null) {
            setVariables(vars);
        }
    }

    public VariablesValueSource() {
        this(null);
    }
    
    public Variables getVariables() {
        return variables;
    }

    public void setVariables(final Variables variables) {
        this.variables = variables;
    }

    public Object getValue(final String input) {
        assert input != null;

        if (variables == null) {
            throw new IllegalStateException("Variables have not yet been set");
        }
        
        try {
            Expression expr = ExpressionFactory.createExpression(input);
            expr.addPreResolver(resolver);

            return expr.evaluate(jexlContext);
        }
        catch (Exception e) {
            throw new ErrorNotification("Failed to evaluate expression: " + input, e);
        }
    }

    //
    // VariablesMapAdapter
    //

    private class VariablesMapAdapter
        implements Map
    {
        private String key(final Object key) {
            return String.valueOf(key);
        }

        public Object get(final Object key) {
            return variables.get(key(key));
        }

        public Object put(final Object key, final Object value) {
            Object prev = variables.get(key(key));

            variables.set(key(key), value);

            return prev;
        }

        // Jexl does not use any of these Map methods

        public int size() {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public Set keySet() {
            throw new UnsupportedOperationException();
        }

        public Collection values() {
            throw new UnsupportedOperationException();
        }

        public Set entrySet() {
            throw new UnsupportedOperationException();
        }
    }
}