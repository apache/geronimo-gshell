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

package org.apache.geronimo.gshell.model.marshal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Support for model {@link Marshaller} implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class MarshallerSupport<T>
    implements Marshaller<T>
{
    private Class rootType;

    protected MarshallerSupport(final Class rootType) {
        assert rootType != null;

        this.rootType = rootType;
    }


    protected XStream createXStream() {
        XStream xs;

        try {
            Class.forName("org.xmlpull.mxp1.MXParser");
            xs = new XStream(new XppDriver());
        }
        catch (ClassNotFoundException ignore) {
            xs = new XStream(new DomDriver());
        }

        xs.processAnnotations(rootType);

        return xs;
    }

    public String marshal(final T root) {
        assert root != null;

        return createXStream().toXML(root);
    }

    public T unmarshal(final InputStream input) {
        assert input != null;

        //noinspection unchecked
        return (T)createXStream().fromXML(input);
    }

    //
    // Helpers
    //
    
    public T unmarshal(final URL url) throws IOException {
        assert url != null;

        InputStream input = url.openStream();

        try {
            return unmarshal(input);
        }
        finally {
            input.close();
        }
    }
}