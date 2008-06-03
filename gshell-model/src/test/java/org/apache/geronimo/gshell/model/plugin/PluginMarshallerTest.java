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

package org.apache.geronimo.gshell.model.plugin;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.model.common.Dependency;
import org.apache.geronimo.gshell.model.common.DependencyGroup;

import java.net.URL;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test for the {@link PluginMarshaller} class.
 *
 * @version $Rev$ $Date$
 */
public class PluginMarshallerTest
    extends TestCase
{
    private PluginMarshaller marshaller = new PluginMarshaller();

    public void testMarshal1() throws Exception {
        Plugin root = new Plugin();

        root.setGroupId("plugin.test");
        root.setArtifactId("plugin-test");
        root.setVersion("1.0");
        root.setName("Plugin Test");
        root.setDescription("A test plugin descriptor.");

        Properties props = new Properties();
        props.setProperty("a", "b");
        root.setProperties(props);

        String xml = marshaller.marshal(root);
        assertNotNull(xml);

        System.out.println(xml);
    }

    public void testUnmarshal1_FromStream() throws Exception {
        InputStream input = getClass().getResourceAsStream("plugin1.xml");

        Plugin root = marshaller.unmarshal(input);

        System.out.println(root);
    }

    public void testUnmarshal1_FromURL() throws Exception {
        URL url = getClass().getResource("plugin1.xml");

        Plugin root = marshaller.unmarshal(url);

        System.out.println(root);
    }
}