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
import java.io.Reader;
import java.io.StringReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;

import org.apache.geronimo.gshell.model.ModelRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for model {@link Marshaller} implementations.
 *
 * @version $Rev$ $Date$
 */
public class MarshallerSupport<T extends ModelRoot>
    implements Marshaller<T>
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    private final Class rootType;

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

        configure(xs);
        
        return xs;
    }

    protected void configure(final XStream xs) {
        assert xs != null;

        xs.processAnnotations(rootType);
    }

    protected void configureAnnotations(final XStream xs, final Class... classes) {
        assert xs != null;
        assert classes != null;

        xs.processAnnotations(classes);
    }
    
    public void marshal(final T root, final OutputStream output) {
        assert root != null;
        assert output != null;

        createXStream().toXML(root, output);
    }

    public void marshal(final T root, final Writer writer) {
        assert root != null;
        assert writer != null;

        createXStream().toXML(root, writer);
    }

    public String marshal(final T root) {
        assert root != null;

        return createXStream().toXML(root);
    }

    @SuppressWarnings({"unchecked"})
    public T unmarshal(final InputStream input) {
        assert input != null;

        T model = (T)createXStream().fromXML(input);

        model.setMarshaller(this);

        return model;
    }

    @SuppressWarnings({"unchecked"})
    public T unmarshal(final Reader reader) {
        assert reader != null;

        T model = (T)createXStream().fromXML(reader);

        model.setMarshaller(this);
        
        return model;
    }

    public T unmarshal(final String xml) {
        assert xml != null;

        return unmarshal(new StringReader(xml));
    }
    
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