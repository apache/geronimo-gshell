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

package org.apache.geronimo.gshell.wisdom.config;

import org.apache.geronimo.gshell.wisdom.plugin.PluginImpl;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Parser for the &lt;gshell:plugin/&gt; element.
 *
 * @version $Rev$ $Date$
 */
public class PluginParser
    extends AbstractBeanDefinitionParser
{
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        assert element != null;
        assert parserContext != null;

        BeanDefinitionBuilder plugin = BeanDefinitionBuilder.rootBeanDefinition(PluginImpl.class);
        plugin.addPropertyValue("id", element.getAttribute("name"));

        @SuppressWarnings({"unchecked"})
        List<Element> children = DomUtils.getChildElementsByTagName(element, "command-bundle");
        if (children != null && children.size() > 0) {
            parseCommandBundles(plugin, children);
        }

        return plugin.getBeanDefinition();
    }

    private void parseCommandBundles(final BeanDefinitionBuilder plugin, final List<Element> elements) {
        assert elements != null;
        assert plugin != null;


    }
}