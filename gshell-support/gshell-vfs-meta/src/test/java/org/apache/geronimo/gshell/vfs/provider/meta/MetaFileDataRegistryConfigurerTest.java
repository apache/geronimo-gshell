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

package org.apache.geronimo.gshell.vfs.provider.meta;

import org.apache.geronimo.gshell.spring.SpringTestSupport;
import org.apache.commons.vfs.FileName;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Map;

/**
 * Unit tests for the {@link MetaFileDataRegistryConfigurer} class.
 *
 * @version $Rev$ $Date$
 */
public class MetaFileDataRegistryConfigurerTest
    extends SpringTestSupport
{
    private AccessibleMetaFileDataRegistry registry;

    private MetaFileDataRegistryConfigurer config;

    protected void setUp() throws Exception {
        super.setUp();

        registry = getBeanContainer().getBean(AccessibleMetaFileDataRegistry.class);
        assertNotNull(registry);

        config = new MetaFileDataRegistryConfigurer(registry);
    }

    public void testAddFolder() throws Exception {
        MetaFileData data = config.addFolder("testing");
        assertNotNull(data);

        Map<FileName, MetaFileData> nodes = registry.getNodes();
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
    }

    public void testAddNestedFiles() throws Exception {
        MetaFileData d1 = config.addFolder("testing");
        assertNotNull(d1);

        MetaFileData d2 = config.addFile("testing/stuff");
        assertNotNull(d2);

        MetaFileData d3 = config.addFile("testing/more-stuff");
        assertNotNull(d3);

        Map<FileName, MetaFileData> nodes = registry.getNodes();
        assertNotNull(nodes);
        assertEquals(4, nodes.size());
    }

    public void testAddNestedFilesWithScheme() throws Exception {
        MetaFileData d1 = config.addFolder("meta:/testing");
        assertNotNull(d1);

        MetaFileData d2 = config.addFile("meta:/testing/stuff");
        assertNotNull(d2);

        MetaFileData d3 = config.addFile("meta:/testing/more-stuff");
        assertNotNull(d3);

        Map<FileName, MetaFileData> nodes = registry.getNodes();
        assertNotNull(nodes);
        assertEquals(4, nodes.size());

        XStream xs = new XStream(new DomDriver());
        String xml = xs.toXML(registry);
        System.out.println(xml);
    }
}