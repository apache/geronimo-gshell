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

//
// Based on Andrew Sazonov's (andrew.sazonov@soft-amis.com) ASL-licensed DumpBeanFactoryPostProcessor
//
//  http://www.soft-amis.com/serendipity/index.php?/archives/14-Who-hides-in-your-Spring-factory.html
//

package org.apache.geronimo.gshell.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import static org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.*;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Logs an XML dump of the given bean factory.
 *
 * @version $Rev$ $Date$
 */
public class LoggingProcessor
    implements BeanFactoryPostProcessor, Ordered
{
    public static final int DEFAULT_INDENT_SIZE = 4;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean prettyPrint = true;

    private int indentSize = DEFAULT_INDENT_SIZE;

    private boolean sortBeansByName = false;

    private MetadataElementDescriptionCreator metadataElementDescriptionCreator = MetadataElementDescriptionCreator.DUMMY;

    public LoggingProcessor() {}

    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (log.isTraceEnabled()) {
            try {
                String xml = render(beanFactory);
                log.trace("Bean factory contents: {}", xml);
            }
            catch (Exception e) {
                log.error("Failed to render bean factory contents", e);
            }
        }
    }

    public String render(final ConfigurableListableBeanFactory beanFactory) throws Exception {
        StringWriter writer = new StringWriter();

        Document document = generateBeanFactoryDump(beanFactory);
        if (document != null) {
            writeDocument(document, writer);
        }

        return writer.toString();
    }

    /**
     * Writes document to provided writer.
     */
    private void writeDocument(final Document document, final Writer writer) throws TransformerException, IOException {
        assert document != null;
        assert writer != null;

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        if (prettyPrint) {
            // workaround for bug in JDK 1.5
            transformerFactory.setAttribute("indent-number", indentSize);
        }

        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(writer);

        if (prettyPrint) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            String indentSize = Integer.toString(this.indentSize);
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indentSize);
        }

        transformer.transform(source, result);
    }

    /**
     * Creates DOM document that contains declaration of given bean factory.
     */
    private Document generateBeanFactoryDump(final ConfigurableListableBeanFactory beanFactory) throws ParserConfigurationException {
        assert beanFactory != null;

        Document result = createDocument();
        ElementBuilder builder = new ElementBuilder(result);
        generateRoot(builder);
        generateBeanAliases(beanFactory, builder);
        generateBeanDefinitions(beanFactory, builder);
        result.getDocumentElement().normalize();

        return result;
    }

    /**
     * Generates declaration for given bean definition. Declaration includes generic
     * attributes, constructor argruments, properties and lookup methods.
     */
    private void processBeanDefinition(final String beanName, final BeanDefinition beanDefinition, final ElementBuilder builder, final boolean inHolder) {
        generateBeanDefinitionAttributes(beanDefinition, builder, beanName, inHolder);
        generateConstructorArguments(beanDefinition, builder);
        generateProperties(beanDefinition, builder);
        generateLookupMethods(beanDefinition, builder);
        builder.up();
    }

    /**
     * Generates declarations for properties of given bean definition.
     */
    private void generateProperties(final BeanDefinition beanDefinition, final ElementBuilder builder) {
        MutablePropertyValues properties = beanDefinition.getPropertyValues();

        @SuppressWarnings({"unchecked"})
        List<PropertyValue> list = properties.getPropertyValueList();

        for (PropertyValue property : list) {
            generatePropertyElement(beanDefinition, builder, property);
        }
    }

    /**
     * Generates declaration of single property elemnet.
     */
    private void generatePropertyElement(final BeanDefinition beanDefinition, final ElementBuilder builder, final PropertyValue property) {
        builder.addChild(PROPERTY_ELEMENT);
        String propertyName = property.getName();
        builder.addAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE, propertyName);

        String description = metadataElementDescriptionCreator.getPropertyValueBeanDefinitionDescription(beanDefinition, property);

        if (description != null) {
            builder.addChild(DESCRIPTION_ELEMENT);
            builder.addPCData(description);
            builder.up();
        }

        Object value = property.getValue();
        processValue(value, builder, false, false);

        builder.up();
    }

    /**
     * Generates generic attributes associated with bean definition (like id, clas, scope etc).
     */
    private void generateBeanDefinitionAttributes(final BeanDefinition beanDefinition, final ElementBuilder elementBuilder, final String beanName, final boolean inHolder) {
        String beanClassName = beanDefinition.getBeanClassName();
        boolean isAbstract = beanDefinition.isAbstract();
        boolean isLazyInit = beanDefinition.isLazyInit();

        String scope = beanDefinition.getScope();

        ElementBuilder builder = elementBuilder.addChild(BEAN_ELEMENT);
        if (!inHolder) {
            if (beanName != null) {
                if (!beanName.equals(beanClassName)) {
                    if (isBeanNameApplicableForID(beanName)) {
                        builder.addAttribute(ID_ATTRIBUTE, beanName);
                    }
                    else {
                        builder.addAttribute(NAME_ATTRIBUTE, beanName);
                    }
                }
            }
        }

        if (beanClassName != null) {
            builder.addAttribute(CLASS_ATTRIBUTE, beanClassName);
        }

        if (isAbstract) {
            builder.addAttribute(ABSTRACT_ATTRIBUTE, TRUE_VALUE);
        }

        if (isLazyInit) {
            builder.addAttribute(LAZY_INIT_ATTRIBUTE, TRUE_VALUE);
        }

        if (scope != null) {
            if (!SINGLETON_ATTRIBUTE.equals(scope)) {
                builder.addAttribute(SCOPE_ATTRIBUTE, scope);
            }
        }

        if (beanDefinition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abstractBeanDefinition = (AbstractBeanDefinition) beanDefinition;

            String initMethod = abstractBeanDefinition.getInitMethodName();
            if (initMethod != null) {
                builder.addAttribute(INIT_METHOD_ATTRIBUTE, initMethod);
            }

            String destroyMethod = abstractBeanDefinition.getDestroyMethodName();
            if (destroyMethod != null) {
                builder.addAttribute(DESTROY_METHOD_ATTRIBUTE, destroyMethod);
            }

            String factoryBeanName = abstractBeanDefinition.getFactoryBeanName();
            if (factoryBeanName != null) {
                builder.addAttribute(FACTORY_BEAN_ATTRIBUTE, factoryBeanName);
            }

            String factoryMethodName = abstractBeanDefinition.getFactoryMethodName();

            if (factoryMethodName != null) {
                builder.addAttribute(FACTORY_METHOD_ATTRIBUTE, factoryMethodName);
            }

            int autowireMode = abstractBeanDefinition.getAutowireMode();
            int dependencyCheck = abstractBeanDefinition.getDependencyCheck();

            String[] dependsOn = abstractBeanDefinition.getDependsOn();

            if (dependsOn != null) {
                if (dependsOn.length > 0) {
                    String dependsOnValue = StringUtils.arrayToCommaDelimitedString(dependsOn);
                    builder.addAttribute(DEPENDS_ON_ATTRIBUTE, dependsOnValue);
                }
            }

            String autowire = getAutowireModeString(autowireMode);
            if (autowire != null) {
                if (!"no".equals(autowire)) {
                    builder.addAttribute(AUTOWIRE_ATTRIBUTE, autowire);
                }
            }

            String dependsCheck = getDependencyCheck(dependencyCheck);
            if (dependsCheck != null) {
                if (!"none".equals(dependsCheck)) {
                    builder.addAttribute(DEPENDENCY_CHECK_ATTRIBUTE, dependsCheck);
                }
            }
        }

        if (beanDefinition instanceof ChildBeanDefinition) {
            ChildBeanDefinition childBeanDefinition = (ChildBeanDefinition) beanDefinition;
            String parent = childBeanDefinition.getParentName();

            if (parent != null) {
                builder.addAttribute(PARENT_ATTRIBUTE, parent);
            }
        }

        String description = metadataElementDescriptionCreator.getBeanDefinitionDescription(beanName, beanDefinition);

        if (description != null) {
            elementBuilder.addChild(DESCRIPTION_ELEMENT);
            elementBuilder.addPCData(description);
            elementBuilder.up();
        }
    }

    /**
     * Method investigates given name of bean and determines whether
     * one could be used in "id" attribute (or otherwise "name" attribute
     * is more acceptable).
     */
    private boolean isBeanNameApplicableForID(final String beanName) {
        // simple check for chars not applicable for identifiers
        // potentially, more strict check should be used there
        return (beanName.indexOf('#') != -1) && (beanName.indexOf('.') != -1);
    }


    /**
     * Generates root element of context.
     */
    private void generateRoot(final ElementBuilder builder) {
        assert builder != null;

        builder.createRoot("beans");
    }

    /**
     * Utility that creates DOM document.
     */
    private Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    /**
     * Generates lookup methods for given bean definition.
     */
    private void generateLookupMethods(final BeanDefinition beanDefinition, final ElementBuilder builder) {
        if (beanDefinition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition def = (AbstractBeanDefinition) beanDefinition;
            MethodOverrides overrides = def.getMethodOverrides();

            @SuppressWarnings({"unchecked"})
            Set<MethodOverride> overridesSet = overrides.getOverrides();

            for (MethodOverride override : overridesSet) {
                generateMethodOverride(override, builder);
            }
        }
    }

    /**
     * Generates methods overrides (lookup or replace).
     */
    private void generateMethodOverride(final MethodOverride override, final ElementBuilder builder) {
        if (override instanceof LookupOverride) {
            LookupOverride lookupOverride = (LookupOverride) override;
            builder.addChild(LOOKUP_METHOD_ELEMENT);
            String methodName = lookupOverride.getMethodName();
            builder.addAttribute(NAME_ATTRIBUTE, methodName);
            String targetBeanName = lookupOverride.getBeanName();
            builder.addAttribute(BEAN_REF_ATTRIBUTE, targetBeanName);
            builder.up();
        }
        else if (override instanceof ReplaceOverride) {
            ReplaceOverride replaceOverride = (ReplaceOverride) override;
            builder.addChild(REPLACED_METHOD_ELEMENT);
            String methodName = replaceOverride.getMethodName();
            String beanName = replaceOverride.getMethodReplacerBeanName();
            builder.addAttribute(NAME_ATTRIBUTE, methodName);
            builder.addAttribute(REPLACER_ATTRIBUTE, beanName);

            // TMP - so far it's not possible to obtain information about replacers....
            // probably we need to use reflection based hack to obtain list of arg type
            // identifiers
            builder.up();
        }
    }

    /**
     * Generates constructor arguments for given bean definition.
     */
    private void generateConstructorArguments(final BeanDefinition beanDefinition, final ElementBuilder builder) {
        ConstructorArgumentValues args = beanDefinition.getConstructorArgumentValues();

        if (args != null) {
            @SuppressWarnings({"unchecked"})
            List<ConstructorArgumentValues.ValueHolder> genericArguments = args.getGenericArgumentValues();

            if (genericArguments.size() > 0) {
                for (ConstructorArgumentValues.ValueHolder holder : genericArguments) {
                    generateConstructorArgument(-1, holder, builder);
                }
            }

            @SuppressWarnings({"unchecked"})
            Map<Integer, ConstructorArgumentValues.ValueHolder> indexedArguments = args.getIndexedArgumentValues();

            if (indexedArguments.size() > 0) {
                for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : indexedArguments.entrySet()) {
                    Integer index = entry.getKey();
                    ConstructorArgumentValues.ValueHolder holder = entry.getValue();
                    generateConstructorArgument(index, holder, builder);
                }
            }
        }
    }

    /**
     * Generates declaration of constructor-arg elemnet.
     */
    private void generateConstructorArgument(final int i, final ConstructorArgumentValues.ValueHolder holder, final ElementBuilder builder) {
        builder.addChild(CONSTRUCTOR_ARG_ELEMENT);

        if (i >= 0) {
            builder.addAttribute(INDEX_ATTRIBUTE, Integer.toString(i));
        }

        String type = holder.getType();
        if (type != null) {
            builder.addAttribute(TYPE_ATTRIBUTE, type);
        }

        Object value = holder.getValue();
        processValue(value, builder, false, false);
        builder.up();
    }

    /**
     * Generates declaration for given value (obtained either from particular
     * property, constructor or element of list, map, set or reference.
     */
    private void processValue(final Object value, final ElementBuilder builder, final boolean fullFormForReference, final boolean inMap) {
        // well, I know that's ugly... but didn't find other way to resolve that
        if (value instanceof BeanDefinition) {
            processBeanDefinition(null, (BeanDefinition) value, builder, true);
        }
        else if (value instanceof BeanDefinitionHolder) {
            BeanDefinitionHolder holder = (BeanDefinitionHolder) value;
            processBeanDefinition(holder.getBeanName(), holder.getBeanDefinition(), builder, true);
        }
        else if (value instanceof RuntimeBeanReference) {
            processRuntimeBeanReference(value, builder, fullFormForReference, inMap);
        }
        else if (value instanceof List) {
            processList((List) value, builder);
        }
        else if (value instanceof Set) {
            processSet((Set) value, builder);
        }
        else if (value instanceof Map) {
            processMap((Map) value, builder);
        }
        else if (value instanceof TypedStringValue) {
            TypedStringValue typedStringValue = (TypedStringValue) value;
            String stringValue = typedStringValue.getValue();

            if (fullFormForReference) {
                builder.addChild(VALUE_ELEMENT);
            }

            if (stringValue != null) {
                String targetTypeName = typedStringValue.getTargetTypeName();
                if (!fullFormForReference && (targetTypeName != null)) {
                    builder.addChild(VALUE_ELEMENT);
                    builder.addAttribute(TYPE_ATTRIBUTE, targetTypeName);
                    builder.addPCData(stringValue);
                    builder.up();
                }
                else {
                    if (inMap) {
                        builder.addAttribute(VALUE_ATTRIBUTE, stringValue);
                    }
                    else {
                        builder.addPCData(stringValue);
                    }
                }
            }
            else {
                builder.addChild(NULL_ELEMENT);
                builder.up();
            }

            if (fullFormForReference) {
                builder.up();
            }
        }
        else if (value instanceof String) {
            if (fullFormForReference) {
                builder.addChild(VALUE_ELEMENT);
                builder.addPCData((String) value);
                builder.up();
            }
            else {
                builder.addAttribute(VALUE_ATTRIBUTE, (String) value);
            }
        }
        else {
            if (value == null) {
                builder.addChild(NULL_ELEMENT);
                builder.up();
            }
            else {
                String str = value.toString();

                if (fullFormForReference) {
                    builder.addChild(VALUE_ELEMENT);
                    builder.addPCData(str);
                    builder.up();
                }
                else {
                    builder.addAttribute(VALUE_ATTRIBUTE, str);
                }
            }
        }
    }

    /**
     * Generates declaration for RuntimeBeanReference (one usually occur as
     * result of "ref" element or reference property.
     */
    private void processRuntimeBeanReference(final Object value, final ElementBuilder builder, final boolean fullFormForReference, final boolean inMap) {
        RuntimeBeanReference ref = (RuntimeBeanReference) value;
        String beanName = ref.getBeanName();

        if (fullFormForReference) {
            builder.addChild(REF_ELEMENT);
            builder.addAttribute(BEAN_REF_ATTRIBUTE, beanName);
            builder.up();
        }
        else {
            if (inMap) {
                builder.addAttribute(VALUE_REF_ATTRIBUTE, beanName);
            }
            else {
                builder.addAttribute(REF_ATTRIBUTE, beanName);
            }
        }
    }

    /**
     * Generates declaration for given map.
     */
    private void processMap(final Map map, final ElementBuilder builder) {
        assert map != null;
        assert builder != null;

        builder.addChild(MAP_ELEMENT);

        for (Object key : map.keySet()) {
            Object value = map.get(key);
            builder.addChild(ENTRY_ELEMENT);
            String keyValue;

            if (key instanceof String) {
                keyValue = (String) key;
                builder.addAttribute(KEY_ATTRIBUTE, keyValue);
            }
            else if (key instanceof TypedStringValue) {
                TypedStringValue typedStringValue = (TypedStringValue) key;
                keyValue = typedStringValue.getValue();
                builder.addAttribute(KEY_ATTRIBUTE, keyValue);
            }
            else if (key instanceof RuntimeBeanReference) {
                RuntimeBeanReference reference = (RuntimeBeanReference) key;
                String beanName = reference.getBeanName();
                builder.addAttribute(KEY_REF_ATTRIBUTE, beanName);
            }
            else {
                keyValue = key.toString();
                builder.addAttribute(KEY_ATTRIBUTE, keyValue);
            }

            processValue(value, builder, false, true);
            builder.up();
        }

        builder.up();
    }

    /**
     * Generates declaration for given set.
     */
    private void processSet(final Set set, final ElementBuilder builder) {
        builder.addChild(SET_ELEMENT);

        for (Object value : set) {
            processValue(value, builder, true, false);
        }

        builder.up();
    }

    /**
     * Generates declaration for given list.
     */
    private void processList(final List list, final ElementBuilder builder) {
        builder.addChild(LIST_ELEMENT);

        for (Object value : list) {
            processValue(value, builder, true, false);
        }

        builder.up();
    }

    /**
     * Method generates declarations for top-level bean declaration in given context.
     */
    private void generateBeanDefinitions(final ConfigurableListableBeanFactory beanFactory, final ElementBuilder builder) {
        String[] names = beanFactory.getBeanDefinitionNames();

        if (sortBeansByName) {
            Arrays.sort(names);
        }

        for (String name : names) {
            BeanDefinition definition = beanFactory.getBeanDefinition(name);
            processBeanDefinition(name, definition, builder, false);
        }
    }

    /**
     * Method generates all aliases for given factory.
     */
    private void generateBeanAliases(final ConfigurableListableBeanFactory beanFactory, final ElementBuilder builder) {
        String[] names = beanFactory.getBeanDefinitionNames();

        for (String name : names) {
            String[] aliases = beanFactory.getAliases(name);

            if (aliases.length > 0) {// first element is name of bean
                // here will be also returned aliases from parent context, probably we need to filter them...
                for (String alias : aliases) {
                    generateAlias(builder, name, alias);
                }
            }
        }
    }

    /**
     * Method that generates declaration of bean' attribute.
     */
    private void generateAlias(final ElementBuilder builder, final String beanName, final String alias) {
        builder.addChild(DefaultBeanDefinitionDocumentReader.ALIAS_ELEMENT);
        builder.addAttribute(DefaultBeanDefinitionDocumentReader.NAME_ATTRIBUTE, beanName);
        builder.addAttribute(DefaultBeanDefinitionDocumentReader.ALIAS_ATTRIBUTE, alias);
        builder.up();
    }

    /**
     * Internal utility that converts internal value of dependency check setting
     * to string value of appropriate attribute.
     */
    private String getDependencyCheck(final int dependencyCheck) {
        String result = null;

        switch (dependencyCheck) {
            case AbstractBeanDefinition.DEPENDENCY_CHECK_ALL: {
                result = DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE;
                break;
            }
            case AbstractBeanDefinition.DEPENDENCY_CHECK_NONE: {
                result = "none";
                break;
            }
            case AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE: {
                result = DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE;
                break;
            }
            case AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS: {
                result = DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE;
                break;
            }
        }

        return result;
    }

    /**
     * Internal utility to transfer internal value of autowire mode to
     * string value used in corresponding attribute.
     */
    private String getAutowireModeString(final int value) {
        String result = null;

        switch (value) {
            case AbstractBeanDefinition.AUTOWIRE_NO: {
                result = "no";
                break;
            }
            case AbstractBeanDefinition.AUTOWIRE_BY_NAME: {
                result = AUTOWIRE_BY_NAME_VALUE;
                break;
            }
            case AbstractBeanDefinition.AUTOWIRE_BY_TYPE: {
                result = AUTOWIRE_BY_TYPE_VALUE;
                break;
            }
            case AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR: {
                result = AUTOWIRE_CONSTRUCTOR_VALUE;
                break;
            }
            case AbstractBeanDefinition.AUTOWIRE_AUTODETECT: {
                result = AUTOWIRE_AUTODETECT_VALUE;
                break;
            }
        }

        return result;
    }

    /**
     * Utility class that simplifies creation of DOM tree
     */
    public class ElementBuilder
    {
        // document used by build tree
        private Document document = null;

        // root of the tree
        private Element rootElement = null;

        // stack of elements from root to current element
        private Stack<Element> stack = new Stack<Element>();

        public ElementBuilder(final Document document) {
            this.document = document;
        }

        public ElementBuilder createRoot(final String elementName) {
            doCreateRootElement(elementName);
            return this;
        }

        private Element doCreateRootElement(final String elementName) {
            rootElement = document.createElement(elementName);
            document.appendChild(rootElement);
            stack.push(rootElement);

            return rootElement;
        }

        public ElementBuilder addAttribute(final String attributeName, final String value) {
            Element elm = stack.peek();
            elm.setAttribute(attributeName, value);

            return this;
        }

        public ElementBuilder addPCData(final String pcData) {
            Element elm = stack.peek();
            Node cdata = document.createCDATASection(pcData);
            elm.appendChild(cdata);

            return this;
        }

        public ElementBuilder addChild(final String elementName) {
            doAddChild(elementName);

            return this;
        }

        private Element doAddChild(final String elementName) {
            Element currentElement = stack.peek();
            Element result = document.createElement(elementName);
            currentElement.appendChild(result);
            stack.push(result);

            return result;
        }

        public Element getRoot() {
            return rootElement;
        }

        public ElementBuilder up() {
            stack.pop();

            return this;
        }

        public void clear() {
            rootElement = null;
            stack.clear();
        }

        public Element getCurrent() {
            return stack.peek();
        }
    }

    public int getOrder() {
        // our post-processor should be the end one
        return Integer.MAX_VALUE;
    }

    /**
     * Utility interface that is intended to provide
     * description strings for bean definition and property of bean
     */
    private static interface MetadataElementDescriptionCreator
    {
        /**
         * Returns description for bean name.
         */
        String getBeanDefinitionDescription(String aBeanName, BeanDefinition aBeanDefinition);

        /**
         * Returns description for property.
         */
        String getPropertyValueBeanDefinitionDescription(BeanDefinition beanDefinition, PropertyValue property);

        /**
         * Default do-nothing implementation.
         */
        static MetadataElementDescriptionCreator DUMMY = new MetadataElementDescriptionCreator() {
            public String getBeanDefinitionDescription(final String beanName, final BeanDefinition beanDefinition) {
                return null;
            }

            public String getPropertyValueBeanDefinitionDescription(final BeanDefinition beanDefinition, final PropertyValue property) {
                return null;
            }
        };
    }
}