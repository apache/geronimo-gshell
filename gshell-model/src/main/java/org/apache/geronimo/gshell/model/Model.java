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

package org.apache.geronimo.gshell.model;

/**
 * Base class for root model elements.
 *
 * @version $Rev$ $Date$
 */
public abstract class Model
    extends Element
    // implements MarshallerAware
{
    // private transient Marshaller marshaller;

    /*
    @XStreamAlias("version")
    @XStreamAsAttribute
    private String modelVersion;
    */

    /*
    public Marshaller getMarshaller() {
    	if (marshaller == null) {
    		throw new IllegalStateException("Marshaller is not bound");
    	}
    	
        return marshaller;
    }

    public void setMarshaller(final Marshaller marshaller) {
        this.marshaller = marshaller;
    }
    */

    /*
    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(final String modelVersion) {
        this.modelVersion = modelVersion;
    }
    */
}