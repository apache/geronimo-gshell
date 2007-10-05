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

package org.apache.geronimo.gshell.descriptor;

import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;

/**
 * Describes arbitrary configuration for a command.
 *
 * @version $Rev$ $Date$
 */
@XStreamAlias("configuration")
public class CommandConfiguration
{
    private String name;

    private String value;

    private Map<String,String> attributes;

    private Map<String,CommandConfiguration> children;

    public CommandConfiguration(final String name) {
        this.name = name;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() throws CommandConfigurationException {
        return value;
    }

    public String getValue(final String defaultValue) {
        try {
            return getValue();
        }
        catch (CommandConfigurationException e) {
            return defaultValue;
        }
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    /*
    String[] getAttributeNames();

    String getAttribute(String paramName) throws CommandConfigurationException;

    String getAttribute(String name, String defaultValue);

    CommandConfiguration getChild(String child);

    CommandConfiguration getChild(int i);

    CommandConfiguration getChild(String child, boolean createChild);

    CommandConfiguration[] getChildren();

    CommandConfiguration[] getChildren(String name);


    int getChildCount();
    */

    public void addChild(final CommandConfiguration configuration) {
        assert configuration != null;

        //
        // TODO:
        //
    }

    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    // TODO: getAttribute()

    public Map<String,CommandConfiguration> getChildren() {
        return children;
    }

    public void setChildren(final Map<String,CommandConfiguration> children) {
        this.children = children;
    }

    // TODO: getChild()

    // TODO: addChild()
}