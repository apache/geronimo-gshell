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

import org.apache.geronimo.gshell.wisdom.plugin.bundle.CommandBundle;
import org.apache.geronimo.gshell.wisdom.command.LinkCommand;
import org.apache.geronimo.gshell.application.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for the &lt;gshell:plugin/&gt; element.
 *
 * @version $Rev$ $Date$
 */
public class PluginParser
    extends AbstractBeanDefinitionParser
{
    private static final String ID = ID_ATTRIBUTE;

    private static final String DESCRIPTION = "description";

    private static final String PLUGIN_TEMPLATE = "pluginTemplate";

    private static final String ACTION = "action";

    private static final String ACTION_ID = "actionId";

    private static final String COMMAND_TEMPLATE_SUFFIX = "CommandTemplate";

    private static final String COMMAND_BUNDLE = "command-bundle";

    private static final String NAME = "name";

    private static final String COMMANDS = "commands";

    private static final String COMMAND = "command";

    private static final String TYPE = "type";

    private static final String DOCUMENTER = "documenter";

    private static final String COMPLETER = "completer";

    private static final String MESSAGE_SOURCE = "message-source";

    private static final String MESSAGES = "messages";

    private static final String PROTOTYPE = "prototype";

    private static final String ALIAS = "alias";
    
    private static final String ALIASES = "aliases";

    private static final String LINK = "link";

    private static final String TARGET = "target";

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
            return name().toLowerCase() + COMMAND_TEMPLATE_SUFFIX;
        }

        public void wire(final BeanDefinitionBuilder command, final BeanDefinitionHolder action) {
            assert command != null;
            assert action != null;

            switch (this) {
                case STATELESS:
                    command.addPropertyReference(ACTION, action.getBeanName());
                    break;

                case STATEFUL:
                    command.addPropertyValue(ACTION_ID, action.getBeanName());
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

        private void parseAndApplyDescription(final Element element, final BeanDefinition def) {
            assert element != null;
            assert def != null;

            Element desc = getChildElement(element, DESCRIPTION);
            if (desc != null) {
                if (def instanceof AbstractBeanDefinition) {
                    ((AbstractBeanDefinition)def).setDescription(desc.getTextContent());
                }
            }
        }

        private void parseAndApplyDescription(final Element element, final BeanDefinitionBuilder builder) {
            assert element != null;
            assert builder != null;

            parseAndApplyDescription(element, builder.getRawBeanDefinition());
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

            Map<String,BeanDefinitionHolder> bundles = parseCommandBundles(element);

            ManagedList bundleNames = new ManagedList();
            // noinspection unchecked
            bundleNames.addAll(bundles.keySet());

            plugin.addPropertyValue("bundleNames", bundleNames);

            return plugin;
        }

        private BeanDefinitionBuilder parsePlugin(final Element element) {
            assert element != null;

            log.trace("Parse plugin; element: {}", element);

            BeanDefinitionBuilder plugin = BeanDefinitionBuilder.childBeanDefinition(PLUGIN_TEMPLATE);

            String name = element.getAttribute(NAME);
            plugin.addConstructorArgValue(name);

            //
            // FIXME: Give the Plugin bean a more meaningful ID, need to stop using AbstractBeanDefinitionParser to do this.
            //

            // String id = Plugin.class.getName() + "#" + name;
            // plugin.addPropertyValue(ID, id);

            parseAndApplyDescription(element, plugin);

            return plugin;
        }

        //
        // <gshell:command-bundle>
        //

        private Map<String,BeanDefinitionHolder> parseCommandBundles(final Element element) {
            assert element != null;

            log.trace("Parse command bundles; element: {}", element);

            Map<String,BeanDefinitionHolder> bundles = new LinkedHashMap<String,BeanDefinitionHolder>();
            List<Element> children = getChildElements(element, COMMAND_BUNDLE);

            for (Element child : children) {
                String name = child.getAttribute(NAME);
                BeanDefinitionBuilder bundle = parseCommandBundle(child);

                // Generate id and register the bean
                BeanDefinition def = bundle.getBeanDefinition();
                String id = resolveId(child, def);
                BeanDefinitionHolder holder = register(def, id);

                bundles.put(name, holder);
            }

            return bundles;
        }

        private BeanDefinitionBuilder parseCommandBundle(final Element element) {
            assert element != null;

            log.trace("Parse command bundle; element; {}", element);

            BeanDefinitionBuilder bundle = BeanDefinitionBuilder.rootBeanDefinition(CommandBundle.class);
            bundle.addConstructorArgValue(element.getAttribute(NAME));
            bundle.setLazyInit(true);
            parseAndApplyDescription(element, bundle);

            //
            // NOTE: Seems we have to use ManagedMap to inject things properly.
            //       But they are not generic, so so limit their usage here.
            //

            ManagedMap commands = new ManagedMap();
            // noinspection unchecked
            commands.putAll(parseCommands(element));
            // noinspection unchecked
            commands.putAll(parseLinks(element));
            bundle.addPropertyValue(COMMANDS, commands);

            ManagedMap aliases = new ManagedMap();
            // noinspection unchecked
            aliases.putAll(parseAliases(element));
            bundle.addPropertyValue(ALIASES, aliases);

            return bundle;
        }

        //
        // <gshell:command>
        //

        private Map<String,BeanDefinition> parseCommands(final Element element) {
            assert element != null;

            log.trace("Parse commands; element; {}", element);

            Map<String,BeanDefinition> commands = new LinkedHashMap<String,BeanDefinition>();

            List<Element> children = getChildElements(element, COMMAND);

            for (Element child : children) {
                String name = child.getAttribute(NAME);
                BeanDefinitionBuilder command = parseCommand(child);
                commands.put(name, command.getBeanDefinition());
            }

            return commands;
        }

        private BeanDefinitionBuilder parseCommand(final Element element) {
            assert element != null;

            log.trace("Parse command; element; {}", element);

            CommandType type = CommandType.parse(element.getAttribute(TYPE));
            BeanDefinitionBuilder command = BeanDefinitionBuilder.childBeanDefinition(type.getTemplateName());
            parseAndApplyDescription(element, command);

            Element child;

            // Required children elements

            child = getChildElement(element, ACTION);
            BeanDefinitionHolder action = parseCommandAction(child);
            type.wire(command, action);

            // Optional children elements

            child = getChildElement(element, DOCUMENTER);
            if (child != null) {
                BeanDefinitionHolder holder = parseBeanDefinitionElement(child);
                command.addPropertyValue(DOCUMENTER, holder.getBeanDefinition());
            }

            child = getChildElement(element, COMPLETER);
            if (child != null) {
                BeanDefinitionHolder holder = parseBeanDefinitionElement(child);
                command.addPropertyValue(COMPLETER, holder.getBeanDefinition());
            }

            child = getChildElement(element, MESSAGE_SOURCE);
            if (child != null) {
                BeanDefinitionHolder holder = parseBeanDefinitionElement(child);
                command.addPropertyValue(MESSAGES, holder.getBeanDefinition());
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
            action.setScope(PROTOTYPE);

            // Generate id and register the bean
            String id = resolveId(element, action);
            return register(action, id);
        }

        //
        // <gshell:link>
        //

        private Map<String,BeanDefinition> parseLinks(final Element element) {
            assert element != null;

            log.trace("Parse links; element; {}", element);

            Map<String,BeanDefinition> links = new LinkedHashMap<String,BeanDefinition>();

            List<Element> children = getChildElements(element, LINK);

            for (Element child : children) {
                BeanDefinitionBuilder link = BeanDefinitionBuilder.rootBeanDefinition(LinkCommand.class);
                link.addConstructorArgValue(child.getAttribute(TARGET));

                String name = child.getAttribute(NAME);
                links.put(name, link.getBeanDefinition());
            }

            return links;
        }

        //
        // <gshell:alias>
        //

        private Map<String,String> parseAliases(final Element element) {
            assert element != null;

            log.trace("Parse aliases; element; {}", element);

            Map<String,String> aliases = new LinkedHashMap<String,String>();

            List<Element> children = getChildElements(element, ALIAS);

            for (Element child : children) {
                String name = child.getAttribute(NAME);
                String alias = child.getAttribute(ALIAS);

                aliases.put(name, alias);
            }

            return aliases;
        }
    }
}