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

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.resolver.FlatResolver;
import org.apache.geronimo.gshell.command.Variables;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides interpolation for shell variables.
 * 
 * @version $Rev$ $Date$
 */
public class VariableInterpolator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FlatResolver resolver = new FlatResolver(true);

    public String interpolate(final String input, final Variables vars) {
        assert input != null;
        assert vars != null;

        // If there is no $ in the expression, then skip the interpolation muck to speed things up
        if (input.indexOf('$') == -1) {
            return input;
        }

        Interpolator interp = createInterpolator(vars);

        log.trace("Interpolating: {}", input);

        String result;
        try {
            result = interp.interpolate(input);
        }
        catch (InterpolationException e) {
            throw new ErrorNotification("Failed to interpolate expression: " + input, e);
        }

        log.trace("Iterpolated result: {}", result);

        return result;
    }

    private Interpolator createInterpolator(final Variables vars) {
        Interpolator interp = new RegexBasedInterpolator();

        // This complex crap here is to adapt our Variables to a JexlContext w/the least overhead
        interp.addValueSource(new ValueSource()
        {
            final Map map = new Map() {
                private String key(final Object key) {
                    return String.valueOf(key);
                }

                public Object get(final Object key) {
                    return vars.get(key(key));
                }

                public Object put(final Object key, final Object value) {
                    Object prev = vars.get(key(key));

                    vars.set(key(key), value);

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
            };

            final JexlContext jc = new JexlContext()
            {
                public Map getVars() {
                    return map;
                }

                // Jexl never calls setVars

                public void setVars(Map map) {
                    throw new UnsupportedOperationException();
                }
            };

            public Object getValue(final String s) {
                try {
                    // Still using Jexl here for now, since it can handle expression like ${env.TERM}
                    // (where "env" is a variable bound to a map, ...)
                    Expression expr = ExpressionFactory.createExpression(s);
                    expr.addPreResolver(resolver);
                    
                    return expr.evaluate(jc);
                }
                catch (Exception e) {
                    throw new ErrorNotification("Failed to evaluate expression: " + s, e);
                }
            }
        });

        return interp;
    }
}