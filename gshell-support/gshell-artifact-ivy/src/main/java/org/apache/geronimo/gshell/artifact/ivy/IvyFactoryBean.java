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

package org.apache.geronimo.gshell.artifact.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.net.URL;

/**
 * {@link Ivy} factory bean.
 *
 * @version $Rev$ $Date$
 */
public class IvyFactoryBean
    implements FactoryBean
{
    static {
        Message.setDefaultLogger(new Slf4jMessageLogger());
    }
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private URL settingsUrl;

    public void setSettingsUrl(final URL url) {
        this.settingsUrl = url;
    }

    public URL getSettingsUrl() {
        if (settingsUrl == null) {
            throw new IllegalStateException("Missing property: settingsUrl");
        }

        return settingsUrl;
    }

    public Object getObject() throws Exception {
        IvySettings settings = new IvySettings();
        URL url = getSettingsUrl();
        log.debug("Settings URL: {}", url);
        settings.load(url);

        //
        // TODO: Need to hook up a TransferListener to show progress, not really sure how to do that with Ivy...
        //
        
        settings.setVariable("ivy.default.configuration.m2compatible", "true");
        
        Ivy ivy = Ivy.newInstance(settings);

        log.debug("Ivy: {}", ivy);
        
        return ivy;
    }

    public Class getObjectType() {
        return Ivy.class;
    }

    public boolean isSingleton() {
        return true;
    }
}