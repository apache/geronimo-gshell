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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.io.xml.DomDriver;
import junit.framework.TestCase;

/**
 * Basic validation of the layout model muck.
 *
 * @version $Rev$ $Date$
 */
public class BasicModelValidationTest
    extends TestCase
{
    private XStream xs;

    protected void setUp() throws Exception {
        xs = new XStream(new DomDriver());
        Annotations.configureAliases(xs, Layout.class, Command.class, Alias.class);
    }

    public void testReadLayout1() throws Exception {
        Layout layout = (Layout) xs.fromXML(getClass().getResourceAsStream("layout1.xml"));
        assertNotNull(layout);
    }

    public void testReadLayout2() throws Exception {
        Layout layout = (Layout) xs.fromXML(getClass().getResourceAsStream("layout2.xml"));
        assertNotNull(layout);
    }
    
    public void testDumpLayout1() throws Exception {
        Layout layout = new Layout("default");

        layout.nodes().add(new Command("foo", "bar"));
        layout.nodes().add(new Alias("f", "foo"));

        Group g = new  Group("test");
        g.nodes().add(new Command("a", "b"));
        g.nodes().add(new Command("c", "d"));

        layout.nodes().add(g);
        
        String xml = xs.toXML(layout);

        System.err.println("XML: " + xml);
    }
}
