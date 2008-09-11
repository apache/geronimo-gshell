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

package org.apache.geronimo.gshell.wisdom.shell;

import jline.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.spring.BeanContainerAware;
import org.apache.geronimo.gshell.spring.BeanContainer;
import org.apache.geronimo.gshell.wisdom.application.event.ApplicationConfiguredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationEvent;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.File;

/**
 * Default implementation of the {@link jline.History} component.
 *
 * @version $Rev$ $Date$
 */
public class HistoryImpl
    extends History
    implements BeanContainerAware
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BeanContainer container;

    @Autowired
    private ApplicationManager applicationManager;

    public HistoryImpl() {}

    public void setBeanContainer(BeanContainer container) {
        assert container != null;
        this.container = container;
    }

    @PostConstruct
    public void init() {
        container.addListener(new ApplicationListener()
        {
            public void onApplicationEvent(final ApplicationEvent event) {
                log.debug("Processing application event: {}", event);
                
                if (event instanceof ApplicationConfiguredEvent) {
                    assert applicationManager != null;

                    try {
                        File file = applicationManager.getContext().getApplication().getBranding().getHistoryFile();

                        log.debug("Application configured, setting history file: {}", file);

                        setHistoryFile(file);
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Failed to set history file", e);
                    }
                }
            }
        });
    }
    
    public void setHistoryFile(final File file) throws IOException {
        assert file != null;

        File dir = file.getParentFile();

        if (!dir.exists()) {
            dir.mkdirs();

            log.debug("Created base directory for history file: {}", dir);
        }

        log.debug("Using history file: {}", file);

        super.setHistoryFile(file);
    }
}