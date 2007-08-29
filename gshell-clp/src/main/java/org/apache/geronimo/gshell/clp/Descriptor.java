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

package org.apache.geronimo.gshell.clp;

import org.apache.geronimo.gshell.clp.handler.Handler;

/**
 * Basic container for option and argument descriptors.
 *
 * @version $Rev$ $Date$
 */
public abstract class Descriptor
{
	private final String description;

    private final String metaVar;

    private final boolean required;

    private final boolean multiValued;

    private final Class<? extends Handler> handler;

	protected Descriptor(final String description, final String metaVar, final boolean required, final Class<? extends Handler> handler, final boolean multiValued) {
		this.description = description;
		this.metaVar = metaVar;
		this.required = required;
		this.handler = handler;
		this.multiValued = multiValued;
	}

	public String description() {
		return description;
	}

	public String metaVar() {
		return metaVar;
	}

	public boolean required() {
		return required;
	}

	public Class<? extends Handler> handler() {
		return handler;
	}

	public boolean isMultiValued() {
		return multiValued;
	}
}