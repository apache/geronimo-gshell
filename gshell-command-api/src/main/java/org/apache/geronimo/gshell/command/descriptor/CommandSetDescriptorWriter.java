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

package org.apache.geronimo.gshell.command.descriptor;

import java.io.Writer;
import java.util.List;

import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * Writes out a {@link CommandSetDescriptor} as XML.
 *
 * @version $Rev$ $Date$
 */
public class CommandSetDescriptorWriter
{
    private static final String LS = System.getProperty("line.separator");

    public void write(final Writer writer, final CommandSetDescriptor desc) throws Exception {
        try {
            XMLWriter w = new PrettyPrintXMLWriter(writer);

            w.startElement("command-set");
            
            writeCommands(w, desc.getCommandDescriptors());

            w.endElement();

            writer.write(LS);

            writer.close();
        }
        catch (PlexusConfigurationException e) {
            throw new Exception("Internal error while writing out the configuration", e);
        }
    }

    private void writeCommands(final XMLWriter w, final List<CommandDescriptor> descs) throws Exception {
        if (descs == null) {
            return;
        }

        w.startElement("commands");

        for (CommandDescriptor descriptor : descs) {
            w.startElement("command");

            element(w, "id", descriptor.getId());
            element(w, "implementation", descriptor.getImplementation());
            element(w, "version", descriptor.getVersion());
            element(w, "description", descriptor.getDescription());

            writeRequirements(w, descriptor.getRequirements());

            writeConfiguration(w, descriptor.getConfiguration());

            w.endElement();
        }

        w.endElement();
    }

    private void writeRequirements(final XMLWriter w, final List<ComponentRequirement> requirements) {
        if (requirements == null || requirements.size() == 0) {
            return;
        }

        w.startElement("requirements");

        for (ComponentRequirement requirement : requirements) {
            w.startElement("requirement");

            element(w, "role", requirement.getRole());
            element(w, "role-hint", requirement.getRoleHint());
            element(w, "field-name", requirement.getFieldName());

            w.endElement();
        }

        w.endElement();
    }

    private void writeConfiguration(final XMLWriter w, final PlexusConfiguration configuration) throws Exception {
        if (configuration == null || configuration.getChildCount() == 0) {
            return;
        }

        if (!configuration.getName().equals("configuration")) {
            throw new Exception("The root node of the configuration must be 'configuration'.");
        }

        writePlexusConfiguration(w, configuration);
    }

    private void writePlexusConfiguration(final XMLWriter xmlWriter, final PlexusConfiguration c) throws Exception {
        if (c.getAttributeNames().length == 0 && c.getChildCount() == 0 && c.getValue() == null) {
            return;
        }

        xmlWriter.startElement(c.getName());

        // Attributes
        for (String name : c.getAttributeNames()) {
            xmlWriter.addAttribute(name, c.getAttribute(name));
        }

        // Children
        PlexusConfiguration[] children = c.getChildren();

        if (children.length > 0) {
            for (PlexusConfiguration child : children) {
                writePlexusConfiguration(xmlWriter, child);
            }
        }
        else {
            String value = c.getValue();

            if (value != null) {
                xmlWriter.writeText(value);
            }
        }

        xmlWriter.endElement();
    }

    private void element(final XMLWriter w, final String name, final String value) {
        if (value == null) {
            return;
        }

        w.startElement(name);

        w.writeText(value);

        w.endElement();
    }
}