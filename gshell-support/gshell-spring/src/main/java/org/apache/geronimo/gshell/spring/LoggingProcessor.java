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
import org.springframework.beans.factory.support.ManagedList;
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
import java.util.Iterator;
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

    protected boolean prettyPrint = true;

    protected int indentSize = DEFAULT_INDENT_SIZE;

    protected boolean generateSchemaBasedContext = false;

    protected boolean sortBeansByName = false;

    protected MetadataElementDescriptionCreator metadataElementDescriptionCreator = MetadataElementDescriptionCreator.DUMMY;

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

    public String render(final ConfigurableListableBeanFactory beanFactory) throws IOException, TransformerException {
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
    protected void writeDocument(final Document document, final Writer writer) throws TransformerException, IOException {
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
        if (generateSchemaBasedContext) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "beans");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                    "http://www.springframework.org/dtd/spring-beans-2.0.dtd");
        }

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
    protected Document generateBeanFactoryDump(ConfigurableListableBeanFactory beanFactory) {
        Document result = createDocument();
        if (result != null) {
            ElementBuilder builder = new ElementBuilder(result);
            generateRoot(beanFactory, builder);
            generateBeanAliases(beanFactory, builder);
            generateBeanDefinitions(beanFactory, builder);
        }
        result.getDocumentElement().normalize();
        return result;
    }

    /**
     * Generates declaration for given bean definition. Declaration includes generic
     * attributes, constructor argruments, properties and lookup methods.
     */
    protected void processBeanDefinition(String beanName, BeanDefinition beanDefinition, ElementBuilder builder, boolean inHolder) {
        generateBeanDefinitionAttributes(beanDefinition, builder, beanName, inHolder);
        generateConstructorArguments(beanDefinition, builder);
        generateProperties(beanDefinition, builder);
        generateLookupMethods(beanDefinition, builder);
        builder.up();
    }

    /**
     * Generates declarations for properties of given bean definition.
     */
    protected void generateProperties(BeanDefinition beanDefinition, ElementBuilder builder) {
        MutablePropertyValues properties = beanDefinition.getPropertyValues();
        List<PropertyValue> list = properties.getPropertyValueList();

        if (generateSchemaBasedContext) {
            for (PropertyValue property : list) {
                PropertyType type = getPropertyType(property);

                switch (type) {
                    case STRING: {
                        generatePropertyAsAttribute(beanDefinition, builder, property, false);
                        break;
                    }
                    case ORDINARY: {
                        generatePropertyAsAttribute(beanDefinition, builder, property, true);
                        break;
                    }
                    case REFERENCE: {
                        generateRefAsAttribute(beanDefinition, builder, property);
                        break;
                    }
                    default: {
                        generatePropertyElement(beanDefinition, builder, property);
                    }
                }
            }
        }
        else {
            for (PropertyValue property : list) {
                generatePropertyElement(beanDefinition, builder, property);
            }
        }
    }

    /**
     * Utility enum that simplifies generation of attributes using p: namespace
     */
    private enum PropertyType {
        STRING,
        ORDINARY,
        REFERENCE,
        OTHER
    }

    /**
     * Generates property value declaration using p: namespace declaration.
     */
    protected void generatePropertyAsAttribute(BeanDefinition beanDefinition, ElementBuilder builder, PropertyValue propertyValue, boolean typedString) {
        String propertyName = propertyValue.getName();
        String value;

        if (typedString) {
            TypedStringValue typedStringValue = (TypedStringValue) propertyValue.getValue();
            value = typedStringValue.getValue();
        }
        else {
            value = (String) propertyValue.getValue();
        }

        StringBuilder tmp = new StringBuilder();
        tmp.append("p:").append(propertyName);
        String attributeName = tmp.toString();
        builder.addAttribute(attributeName, value);
    }

    /**
     * Generates reference to bean via attribute in p: namespace.
     */
    protected void generateRefAsAttribute(BeanDefinition beanDefinition, ElementBuilder builder, PropertyValue property) {
        String propertyName = property.getName();
        RuntimeBeanReference runtimeBeanReference = (RuntimeBeanReference) property.getValue();
        StringBuilder tmp = new StringBuilder();
        tmp.append("p:").append(propertyName).append("-ref");
        String attributeName = tmp.toString();
        String beanName = runtimeBeanReference.getBeanName();
        builder.addAttribute(attributeName, beanName);
    }

    /**
     * Inspects value of attribute to determine whether one could be declared
     * via p: namespace.
     */
    protected PropertyType getPropertyType(PropertyValue property) {
        PropertyType result = PropertyType.OTHER;
        Object value = property.getValue();

        if (value instanceof String) {
            result = PropertyType.STRING;
        }
        else if (value instanceof RuntimeBeanReference) {
            result = PropertyType.REFERENCE;
        }
        else if (value instanceof TypedStringValue) {
            TypedStringValue typedStringValue = (TypedStringValue) value;
            String type = typedStringValue.getTargetTypeName();

            if (type == null) {
                String typedValue = typedStringValue.getValue();

                if (typedValue != null) {
                    result = PropertyType.ORDINARY;
                }
            }
        }
        return result;

    }

    /**
     * Generates declaration of single property elemnet.
     */
    protected void generatePropertyElement(BeanDefinition beanDefinition, ElementBuilder builder, PropertyValue property) {
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
    protected void generateBeanDefinitionAttributes(BeanDefinition beanDefinition, ElementBuilder elementBuilder, String beanName, boolean inHolder) {
        String beanClassName = beanDefinition.getBeanClassName();
        boolean isSingleton = beanDefinition.isSingleton();
        boolean isAbstract = beanDefinition.isAbstract();
        boolean isLazyInit = beanDefinition.isLazyInit();

        String scope = beanDefinition.getScope();

        ElementBuilder builder = elementBuilder.addChild(BEAN_ELEMENT);
        if (!inHolder) {
            if (beanName != null) {
                if (!beanName.equals(beanClassName)) {
                    if (isBeanNameApplicableForID(beanName, beanDefinition)) {
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
    protected boolean isBeanNameApplicableForID(String beanName, BeanDefinition beanDefinition) {
        // simple check for chars not applicable for identifiers
        // potentially, more strict check should be used there
        return (beanName.indexOf('#') != -1) && (beanName.indexOf('.') != -1);
    }


    /**
     * Generates root element of context.
     */
    protected void generateRoot(ConfigurableListableBeanFactory beanFactory, ElementBuilder builder) {
        builder.createRoot("beans");

        if (generateSchemaBasedContext) {
            builder.addAttribute("xmlns", "http://www.springframework.org/schema/beans");
            Element root = builder.getCurrent();
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xmlns:p", "http://www.springframework.org/schema/p");
            root.setAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd");
        }

        // TODO - check whether params of context there should be generated
        // it seems that we can't restore defaults for context... Anyway,
        // they will be specified on particular beans
    }

    /**
     * Utility that creates DOM document.
     */
    protected Document createDocument() {
        Document document = null;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
        }
        catch (ParserConfigurationException e) {
            log.error("Unable to create document", e);
        }

        return document;
    }

    /**
     * Generates lookup methods for given bean definition.
     */
    protected void generateLookupMethods(BeanDefinition beanDefinition, ElementBuilder builder) {
        if (beanDefinition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition def = (AbstractBeanDefinition) beanDefinition;
            MethodOverrides overrides = def.getMethodOverrides();
            Set<MethodOverride> overridesSet = overrides.getOverrides();

            for (MethodOverride override : overridesSet) {
                generateMethodOverride(override, builder);
            }
        }
    }

    /**
     * Generates methods overrides (lookup or replace).
     */
    protected void generateMethodOverride(MethodOverride override, ElementBuilder builder) {
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
    protected void generateConstructorArguments(BeanDefinition beanDefinition, ElementBuilder builder) {
        ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();

        if (constructorArguments != null) {
            List<ConstructorArgumentValues.ValueHolder> genericArguments = constructorArguments.getGenericArgumentValues();

            if (genericArguments.size() > 0) {
                for (ConstructorArgumentValues.ValueHolder holder : genericArguments) {
                    generateConstructorArgument(-1, holder, builder);
                }
            }

            Map<Integer, ConstructorArgumentValues.ValueHolder> indexedArguments = constructorArguments.getIndexedArgumentValues();

            if (indexedArguments.size() > 0) {
                for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : indexedArguments.entrySet()) {
                    Integer index = entry.getKey();
                    ConstructorArgumentValues.ValueHolder holder = entry.getValue();
                    generateConstructorArgument(index.intValue(), holder, builder);
                }
            }
        }
    }

    /**
     * Generates declaration of constructor-arg elemnet.
     */
    protected void generateConstructorArgument(int i, ConstructorArgumentValues.ValueHolder holder, ElementBuilder builder) {
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
    protected void processValue(Object value, ElementBuilder builder, boolean fullFormForReference, boolean inMap) {
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
    protected void processRuntimeBeanReference(Object value, ElementBuilder builder, boolean fullFormForReference, boolean inMap) {
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
    protected void processMap(Map sourceMap, ElementBuilder builder) {
        builder.addChild(MAP_ELEMENT);
        Map map = sourceMap;
        Set keySet = map.keySet();
        Iterator iter = keySet.iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            Object value = map.get(key);
            builder.addChild(ENTRY_ELEMENT);
            String keyValue = null;

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
    protected void processSet(Set set, ElementBuilder builder) {
        builder.addChild(SET_ELEMENT);

        for (Object value : set) {
            processValue(value, builder, true, false);
        }

        builder.up();
    }

    /**
     * Generates declaration for given list.
     */
    protected void processList(List sourceList, ElementBuilder builder) {
        ManagedList list = (ManagedList) sourceList;
        builder.addChild(LIST_ELEMENT);

        for (Object value : list) {
            processValue(value, builder, true, false);
        }

        builder.up();
    }

    /**
     * Method generates declarations for top-level bean declaration in given context.
     */
    protected void generateBeanDefinitions(ConfigurableListableBeanFactory beanFactory, ElementBuilder builder) {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();

        if (sortBeansByName) {
            Arrays.sort(beanDefinitionNames);
        }

        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            processBeanDefinition(beanDefinitionName, beanDefinition, builder, false);
        }
    }

    /**
     * Method generates all aliases for given factory.
     */
    protected void generateBeanAliases(ConfigurableListableBeanFactory beanFactory, ElementBuilder builder) {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanDefinitionNames) {
            String[] aliases = beanFactory.getAliases(beanName);
            if (aliases.length > 0) {// first element is name of bean
                // here will be also returned aliases from parent context, probably we need to filter them...
                for (String alias : aliases) {
                    generateAlias(builder, beanName, alias);
                }
            }
        }
    }

    /**
     * Method that generates declaration of bean' attribute.
     */
    protected void generateAlias(ElementBuilder builder, String beanName, String alias) {
        builder.addChild(DefaultBeanDefinitionDocumentReader.ALIAS_ELEMENT);
        builder.addAttribute(DefaultBeanDefinitionDocumentReader.NAME_ATTRIBUTE, beanName);
        builder.addAttribute(DefaultBeanDefinitionDocumentReader.ALIAS_ATTRIBUTE, alias);
        builder.up();
    }

    /**
     * Internal utility that converts internal value of dependency check setting
     * to string value of appropriate attribute.
     */
    protected String getDependencyCheck(int dependencyCheck) {
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
    protected String getAutowireModeString(int value) {
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

    public MetadataElementDescriptionCreator getMetadataElementDescriptionCreator() {
        return metadataElementDescriptionCreator;
    }

    /**
     * Utility class that simplifies creation of DOM tree
     */
    public class ElementBuilder
    {
        // document used by build tree
        protected Document document = null;

        // root of the tree
        protected Element rootElement = null;

        // stack of elements from root to current element
        protected Stack<Element> stack = new Stack<Element>();

        public ElementBuilder(Document aDocument) {
            document = aDocument;
        }

        public ElementBuilder createRoot(String elementName) {
            doCreateRootElement( elementName);
            return this;
        }

        protected Element doCreateRootElement(String elementName) {
            rootElement = document.createElement(elementName);
            document.appendChild(rootElement);
            stack.push(rootElement);
            return rootElement;
        }

        public ElementBuilder addAttribute(String attributeName, String value) {
            Element elm = stack.peek();
            elm.setAttribute(attributeName, value);
            return this;
        }

        public ElementBuilder addPCData(String pcData) {
            Element elm = stack.peek();
            Node cdata = document.createCDATASection(pcData);
            elm.appendChild(cdata);
            return this;
        }

        public ElementBuilder addChild(String elementName) {
            doAddChild(elementName);
            return this;
        }

        protected Element doAddChild(String elementName) {
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
            public String getBeanDefinitionDescription(String beanName, BeanDefinition beanDefinition) {
                return null;
            }

            public String getPropertyValueBeanDefinitionDescription(BeanDefinition beanDefinition, PropertyValue property) {
                return null;
            }
        };
    }
}