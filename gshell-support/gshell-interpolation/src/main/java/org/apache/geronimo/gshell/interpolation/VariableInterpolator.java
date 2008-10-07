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

import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides interpolation for shell variables using Jexl.
 *
 * Still using Jexl here for now, since it can handle expression like <tt>${env.TERM}</tt>
 * (where <tt>env</tt> is a variable bound to a map, ...).
 *
 * @version $Rev$ $Date$
 */
public class VariableInterpolator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

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
        assert vars != null;
        
        Interpolator interp = new RegexBasedInterpolator();
        interp.addValueSource(new VariablesValueSource(vars));
        return interp;
    }
}