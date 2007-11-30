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

package org.apache.geronimo.gshell.maven.gshell;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Parameter;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.apache.geronimo.gshell.descriptor.CommandParameter;
import org.apache.geronimo.gshell.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.descriptor.CommandRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ????
 *
 * @version $Id$
 */
public class CommandDescriptorGleaner
{
    private static final String EMPTY_STRING = "";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String filterEmptyAsNull(final String value) {
        if (value == null) {
            return null;
        }
        else if (EMPTY_STRING.equals(value.trim())) {
            return null;
        }
        else {
            return value;
        }
    }

    protected boolean isRequirementListType(final Class type) {
        assert type != null;

        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    protected String deHump(final String string) {
        assert string != null;

        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && Character.isUpperCase(string.charAt(i))) {
                buff.append('-');
            }

            buff.append(string.charAt(i));
        }

        return buff.toString().trim().toLowerCase();
    }

    public CommandDescriptor glean(final Class clazz) throws Exception {
        assert clazz != null;

        // Cast to <?> so that we don't have to cast below
        Class<?> type = (Class<?>)clazz;

        // Skip abstract classes
        if (Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        CommandComponent anno = type.getAnnotation(CommandComponent.class);

        if (anno == null) {
            return null;
        }

        log.debug("Creating descriptor for: {}", type);

        CommandDescriptor command = new CommandDescriptor();

        //
        // TODO: Set the source...
        //

        command.setId(anno.id());

        command.setDescription(filterEmptyAsNull(anno.description()));

        command.setImplementation(type.getName());

        command.setVersion(filterEmptyAsNull(anno.version()));

        for (Class t : getClasses(type)) {
            for (Field field : t.getDeclaredFields()) {
                CommandRequirement requirement = findRequirement(field);

                if (requirement != null) {
                    command.addRequirement(requirement);
                }

                CommandParameter parameter = findParameter(field);

                if (parameter != null) {
                    command.addParameter(parameter);
                }
            }

            //
            // TODO: Inspect methods?
            //
        }

        return command;
    }

    /**
     * Returns a list of all of the classes which the given type inherits from.
     */
    private List<Class> getClasses(Class<?> type) {
        assert type != null;

        List<Class> classes = new ArrayList<Class>();

        while (type != null) {
            classes.add(type);
            type = type.getSuperclass();

            //
            // TODO: See if we need to include interfaces here too?
            //
        }

        return classes;
    }

    private CommandRequirement findRequirement(final Field field) {
        assert field != null;

        Requirement anno = field.getAnnotation(Requirement.class);

        if (anno == null) {
            return null;
        }

        Class<?> type = field.getType();

        CommandRequirement requirement = new CommandRequirement();

        if (isRequirementListType(type)) {
            requirement.setCollection(true);
        }

        requirement.setId(filterEmptyAsNull(anno.id()));

        if (anno.type().isAssignableFrom(Void.class)) {
            requirement.setType(type.getName());
        }
        else {
            requirement.setType(anno.type().getName());
        }

        String name = filterEmptyAsNull(anno.name());

        if (name == null) {
            name = field.getName();
        }

        requirement.setName(name);

        return requirement;
    }

    private CommandParameter findParameter(final Field field) {
        assert field != null;

        Parameter anno = field.getAnnotation(Parameter.class);

        if (anno == null) {
            return null;
        }

        String name = filterEmptyAsNull(anno.name());

        if (name == null) {
            name = field.getName();
        }
        
        name = deHump(name);

        String value = filterEmptyAsNull(anno.value());

        return new CommandParameter(name, value);
    }
}