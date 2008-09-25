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

import org.apache.geronimo.gshell.wisdom.alias.AliasCommand;
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

        //
        // <gshell:plugin>
        //

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
            // noinspection unchecked
            rules.add(rule.getBeanDefinition());
            plugin.addPropertyValue("activationRules", rules);

            return plugin;
        }

        private BeanDefinitionBuilder parsePlugin(final Element element) {
            assert element != null;

            log.trace("Parse plugin; element: {}", element);

            BeanDefinitionBuilder plugin = BeanDefinitionBuilder.rootBeanDefinition(PluginImpl.class);
            plugin.addPropertyValue("id", element.getAttribute("name"));

            return plugin;
        }

        //
        // <gshell:command-bundle>
        //

        private List<BeanDefinitionHolder> parseCommandBundles(final Element element) {
            assert element != null;

            log.trace("Parse command bundles; element: {}", element);

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

            log.trace("Parse command bundle; element; {}", element);

            BeanDefinitionBuilder bundle = BeanDefinitionBuilder.rootBeanDefinition(CommandBundle.class);
            bundle.addPropertyValue("id", element.getAttribute("name"));
            bundle.setLazyInit(true);

            List commands = parseCommands(element);
            List aliases = parseAliases(element);

            // noinspection unchecked
            commands.addAll(aliases);

            bundle.addPropertyValue("commands", commands);
            
            return bundle;
        }

        //
        // <gshell:command>
        //

        private List parseCommands(final Element element) {
            assert element != null;

            log.trace("Parse commands; element; {}", element);

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

            log.trace("Parse command; element; {}", element);

            CommandType type = CommandType.parse(element.getAttribute("type"));
            BeanDefinitionBuilder command = BeanDefinitionBuilder.childBeanDefinition(type.getTemplateName());

            // TODO: Currently name is pulled from the documentor, need to change that
            // command.addPropertyValue("name", element.getAttribute("name"));

            Element child;

            // Required children elements

            child = getChildElement(element, "action");
            BeanDefinitionHolder action = parseCommandAction(child);
            type.wire(command, action);

            // Optional children elements

            child = getChildElement(element, "documenter");
            if (child != null) {
                BeanDefinitionHolder holder = parseBeanDefinitionElement(child);
                command.addPropertyValue("documenter", holder.getBeanDefinition());
            }

            child = getChildElement(element, "completer");
            if (child != null) {
                BeanDefinitionHolder holder = parseBeanDefinitionElement(child);
                command.addPropertyValue("completer", holder.getBeanDefinition());
            }

            child = getChildElement(element, "message-source");
            if (child != null) {
                BeanDefinitionHolder holder = parseBeanDefinitionElement(child);
                command.addPropertyValue("messages", holder.getBeanDefinition());
            }

            return command;
        }

        //
        // <gshell:action>
        //

        private BeanDefinitionHolder parseCommandAction(final Element element) {
            assert element != null;

            log.trace("Parse command action; element; {}", element);

            // Construct the action
            BeanDefinition action = parseBeanDefinitionElement(element).getBeanDefinition();
            
            // All actions are configured as prototypes
            action.setScope("prototype");

            // Generate id and register the bean
            String id = resolveId(element, action);
            return register(action, id);
        }

        //
        // <gshell:alias>
        //

        private List parseAliases(final Element element) {
            assert element != null;

            log.trace("Parse aliases; element; {}", element);

            List<Element> children = getChildElements(element, "alias");
            ManagedList defs = new ManagedList();

            for (Element child : children) {
                BeanDefinitionBuilder command = parseAlias(child);

                // noinspection unchecked
                defs.add(command.getBeanDefinition());
            }

            return defs;
        }

        private BeanDefinitionBuilder parseAlias(final Element element) {
            assert element != null;

            log.trace("Parse alias; element; {}", element);

            BeanDefinitionBuilder alias = BeanDefinitionBuilder.rootBeanDefinition(AliasCommand.class);
            alias.addPropertyValue("name", element.getAttribute("name"));
            alias.addPropertyValue("target", element.getAttribute("target"));

            return alias;
        }

    }
}