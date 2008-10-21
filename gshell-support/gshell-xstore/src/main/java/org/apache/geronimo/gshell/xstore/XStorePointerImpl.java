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

package org.apache.geronimo.gshell.xstore;

/**
 * {@link XStorePointer} implementation.
 *
 * @version $Rev$ $Date$
 */
public class XStorePointerImpl
    implements XStorePointer
{
    private transient XStore xstore;

    private String path;

    public XStorePointerImpl(final XStore xstore, final String path) {
        assert xstore != null;
        this.xstore = xstore;
        assert path != null;
        this.path = path;
    }

    public XStorePointerImpl() {}
    
    public String getPath() {
        return path;
    }

    public XStoreRecord getRecord() {
        return xstore.resolveRecord(getPath());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private Object readResolve() {
        xstore = XStoreHolder.get();
        if (xstore == null) {
            throw new IllegalStateException("Unable to attach to XStore instance");
        }
        return this;
    }
}