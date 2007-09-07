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

package org.apache.geronimo.gshell.layout.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The root container for a layout tree.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("layout")
public class Layout
{
    protected String name;

    protected List<Node> nodes = new ArrayList<Node>();

    public Layout(final String name) {
        assert name != null;

        this.name = name;
    }

    public List<Node> nodes() {
        return nodes;
    }

    public String getName() {
        return name;
    }

    //
    // XML Conversion
    //
    
    private static XStream createXStream() {
        XStream xs = new XStream(new DomDriver());
        Annotations.configureAliases(xs, Layout.class, CommandNode.class, AliasNode.class);

        return xs;
    }

    public static Layout fromXML(final InputStream input) {
        assert input != null;

        return (Layout) createXStream().fromXML(input);
    }

    public static String toXML(final Layout layout) {
        assert layout != null;

        return createXStream().toXML(layout);
    }
}