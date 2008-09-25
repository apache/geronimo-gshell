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
import org.apache.geronimo.gshell.wisdom.plugin.activation.DefaultCommandBundleActivationRule;
import org.apache.geronimo.gshell.wisdom.plugin.bundle.CommandBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the &lt;gshell:plugin/&gt; element.
 *
 * @version $Rev$ $Date$
 */
public class PluginParser
    extends AbstractBeanDefinitionParser
{
    @Override
    protected boolean shouldGenerateId() {
		return true;
	}

    @Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext context) {
        assert element != null;
        assert context != null;

        Builder builder = new Builder(context);
        BeanDefinitionBuilder plugin = builder.buildPlugin(element);
        return plugin.getBeanDefinition();
    }

    /**
     * Helper to deal with command type.
     */
    private enum CommandType
    {
        STATELESS,
        STATEFUL;

        public static CommandType parse(final String text) {
            assert text != null;

            return valueOf(text.toUpperCase());
        }

        public String getTemplateName() {
            return name().toLowerCase() + "CommandTemplate";
        }

        public void wire(final BeanDefinitionBuilder command, final BeanDefinitionHolder action) {
            assert command != null;
            assert action != null;

            switch (this) {
                case STATELESS:
                    command.addPropertyReference("action", action.getBeanName());
                    break;

                case STATEFUL:
                    command.addPropertyValue("actionId", action.getBeanName());
                    break;
            }
        }
    }

    /**
     * Helper to build plugin related bean definitions.
     */
    private class Builder
    {
        private final Logger log = LoggerFactory.getLogger(getClass());

        private ParserContext context;

        public Builder(final ParserContext context) {
            assert context != null;

            this.context = context;
        }

        private String resolveId(final Element element, final BeanDefinition def) throws BeanDefinitionStoreException {
            assert element != null;
            assert def != null;

            if (shouldGenerateId()) {
                return context.getReaderContext().generateBeanName(def);
            }

            String id = element.getAttribute(ID_ATTRIBUTE);

            if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
                id = context.getReaderContext().generateBeanName(def);
            }

            return id;
        }

        @SuppressWarnings({"unchecked"})
        private List<Element> getChildElements(final Element element, final String name) {
            assert element != null;
            assert name != null;

            return DomUtils.getChildElementsByTagName(element, name);
        }

        @SuppressWarnings({"unchecked"})
        private Element getChildElement(final Element element, final String name) {
            assert element != null;
            assert name != null;

            List<Element> elements = DomUtils.getChildElementsByTagName(element, name);
            if (elements != null && !elements.isEmpty()) {
                return elements.get(0);
            }
            return null;
        }

        private BeanDefinitionHolder parseBeanDefinitionElement(final Element element) {
            assert element != null;

            BeanDefinitionParserDelegate parser = new BeanDefinitionParserDelegate(context.getReaderContext());
            parser.initDefaults(element.getOwnerDocument().getDocumentElement());
            return parser.parseBeanDefinitionElement(element);
        }

        private BeanDefinitionHolder register(final BeanDefinitionHolder holder) {
            assert holder != null;

            registerBeanDefinition(holder, context.getRegistry());
            return holder;
        }

        private BeanDefinitionHolder register(final BeanDefinition def, final String id) {
            assert def != null;
            assert id != null;

            BeanDefinitionHolder holder = new BeanDefinitionHolder(def, id);
            return register(holder);
        }

        public BeanDefinitionBuilder buildPlugin(final Element element) {
            assert element != null;

            BeanDefinitionBuilder plugin = parsePlugin(element);

            List<BeanDefinitionHolder> bundles = parseCommandBundles(element);

            for (BeanDefinitionHolder holder : bundles) {
                // TODO: Handle registration of the bundles?
            }

            //
            // HACK: For now hard-code a single activation rule
            //

            BeanDefinitionBuilder rule = BeanDefinitionBuilder.rootBeanDefinition(DefaultCommandBundleActivationRule.class);
            rule.addPropertyValue("bundleId", "default");
            ManagedList rules = new ManagedList();
            rules.add(rule.getBeanDefinition());
            plugin.addPropertyValue("activationRules", rules);

            return plugin;
        }

        private BeanDefinitionBuilder parsePlugin(final Element element) {
            assert element != null;

            log.info("Parse plugin; element: {}", element);

            BeanDefinitionBuilder plugin = BeanDefinitionBuilder.rootBeanDefinition(PluginImpl.class);
            plugin.addPropertyValue("id", element.getAttribute("name"));

            return plugin;
        }

        private List<BeanDefinitionHolder> parseCommandBundles(final Element element) {
            assert element != null;

            log.info("Parse command bundles; element: {}", element);

            List<Element> children = getChildElements(element, "command-bundle");
            List<BeanDefinitionHolder> holders = new ArrayList<BeanDefinitionHolder>();

            for (Element child : children) {
                BeanDefinitionBuilder bundle = parseCommandBundle(child);

                // Generate id and register the bean
                BeanDefinition def = bundle.getBeanDefinition();
                String id = resolveId(child, def);
                BeanDefinitionHolder holder = register(def, id);

                // noinspection unchecked
                holders.add(holder);
            }

            return holders;
        }

        private BeanDefinitionBuilder parseCommandBundle(final Element element) {
            assert element != null;

            log.info("Parse command bundle; element; {}", element);

            BeanDefinitionBuilder bundle = BeanDefinitionBuilder.rootBeanDefinition(CommandBundle.class);
            bundle.addPropertyValue("id", element.getAttribute("name"));
            bundle.setLazyInit(true);

            List commands = parseCommands(element);
            bundle.addPropertyValue("commands", commands);
            
            return bundle;
        }

        private List parseCommands(final Element element) {
            assert element != null;

            log.info("Parse commands; element; {}", element);

            List<Element> children = getChildElements(element, "command");
            ManagedList defs = new ManagedList();

            for (Element child : children) {
                BeanDefinitionBuilder command = parseCommand(child);

                // noinspection unchecked
                defs.add(command.getBeanDefinition());
            }

            return defs;
        }

        private BeanDefinitionBuilder parseCommand(final Element element) {
            assert element != null;

            log.info("Parse command; element; {}", element);

            CommandType type = CommandType.parse(element.getAttribute("type"));
            BeanDefinitionBuilder command = BeanDefinitionBuilder.childBeanDefinition(type.getTemplateName());

            // TODO: Currently name is pulled from the documentor, need to change that
            // command.addPropertyValue("name", element.getAttribute("name"));

            BeanDefinitionHolder action = parseCommandAction(element);

            // Wire up the action based on the type
            type.wire(command, action);

            return command;
        }

        private BeanDefinitionHolder parseCommandAction(final Element element) {
            assert element != null;

            log.info("Parse command action; element; {}", element);

            Attr actionAttr = element.getAttributeNode("action");
            Element actionElement = getChildElement(element, "action");

            // Validate that we only have one action
            if (actionAttr != null && actionElement != null) {
                throw new RuntimeException("Must specify only one action attribute or action element");
            }
            if (actionAttr == null && actionElement == null) {
                throw new RuntimeException("Missing action attribute or action element");
            }

            // Construct the action
            BeanDefinition action;

            if (actionAttr != null) {
                action = BeanDefinitionBuilder.rootBeanDefinition(actionAttr.getValue()).getBeanDefinition();
            }
            else {
                // TODO: Can probably just treat the "action" element as a "bean" element?
                
                Attr classAttr = actionElement.getAttributeNode("class");
                Element beanElement = getChildElement(actionElement, "bean");

                // Validate we only have one configuration for the action
                if (classAttr != null && beanElement != null) {
                    throw new RuntimeException("Must specify only one class attribute or bean element");
                }
                if (classAttr == null && beanElement == null) {
                    throw new RuntimeException("Missing action class attribute or action bean element");
                }

                if (classAttr != null) {
                    action = BeanDefinitionBuilder.rootBeanDefinition(classAttr.getValue()).getBeanDefinition();
                }
                else {
                    action = parseBeanDefinitionElement(beanElement).getBeanDefinition();
                }
            }

            // All actions are configured as prototypes
            action.setScope("prototype");

            // Generate id and register the bean
            String id = resolveId(element, action);
            return register(action, id);
        }
    }
}