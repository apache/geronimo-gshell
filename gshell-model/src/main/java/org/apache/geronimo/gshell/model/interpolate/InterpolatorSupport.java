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

package org.apache.geronimo.gshell.model.interpolate;

import org.apache.geronimo.gshell.model.Model;
import org.apache.geronimo.gshell.marshal.Marshaller;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for {@link Interpolator} implemntations.
 *
 * @version $Rev$ $Date$
 */
public class InterpolatorSupport<T extends Model>
	implements Interpolator<T>
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final org.codehaus.plexus.interpolation.Interpolator interpolator;
	
	private String prefixPattern;
	
	private RecursionInterceptor recursionInterceptor;
	
	public InterpolatorSupport() {
		this.interpolator = new RegexBasedInterpolator();
	}
	
	public void setPrefixPattern(final String pattern) {
		this.prefixPattern = pattern;
	}
	
	public void setRecursionInterceptor(final RecursionInterceptor interceptor) {
		this.recursionInterceptor = interceptor;
	}
	
	public void addValueSource(final ValueSource source) {
		assert source != null;
		
		interpolator.addValueSource(source);
	}

	public T interpolate(final T input) throws InterpolationException {
		assert input != null;

        //noinspection unchecked
        Marshaller<T> marshaller = input.getMarshaller();
		String xml = marshaller.marshal(input);
		
		log.trace("Interpolating: {}", xml);
		
		String result = interpolator.interpolate(xml, prefixPattern, recursionInterceptor);
		
		log.trace("Interpolated result: {}", result);
		
		return marshaller.unmarshal(result);
	}
}
