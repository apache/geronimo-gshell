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

package org.apache.geronimo.gshell.ivy;

import org.apache.geronimo.gshell.spring.SpringTestSupport;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.ivy.Ivy;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.cache.ResolutionCacheManager;

import java.io.File;
import java.util.List;
import java.net.URL;

/**
 * General Ivy tests.
 * 
 * @version $Rev$ $Date$
 */
public class IvyTest
    extends SpringTestSupport
{
    public void testIvy() throws Exception {
        Message.setDefaultLogger(new DefaultMessageLogger(-1)); // Message.MSG_INFO));

        IvySettings settings = new IvySettings();
        log.debug("Settings: {}", settings);

        URL url = getClass().getResource("ivysettings.xml");
        log.debug("Settings URL: {}", url);
        settings.load(url);

        Ivy ivy = Ivy.newInstance(settings);
        log.debug("Ivy: {}", ivy);

        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org.apache.geronimo.gshell.wisdom", "gshell-wisdom-bootstrap", "1.0-alpha-2-SNAPSHOT");
        log.debug("MRID: {}", mrid);

        DefaultModuleDescriptor md = new DefaultModuleDescriptor(ModuleRevisionId.newInstance("caller", "all-caller", "working"), "integration", null, true);
        md.addConfiguration(new Configuration("default"));
        md.setLastModified(System.currentTimeMillis());
        log.debug("MD: {}", md);

        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, mrid, false, false, true);
        dd.addDependencyConfiguration("default", "default");
        log.debug("DD: {}", dd);

        md.addDependency(dd);

        ResolveOptions options = new ResolveOptions();
        options.setOutputReport(false);
        log.debug("Options: {}", options);

        StopWatch watch = new StopWatch(true);

        ResolveReport report = ivy.resolve(md, options);
        
        log.debug("Resolve completed in: {}", watch);

        log.debug("Report: {}", report);

        if (report.hasError()) {
            log.error("Report has errors");
        }

        ResolutionCacheManager cacheManager = ivy.getResolutionCacheManager();

        File ivyFile = cacheManager.getResolvedIvyFileInCache(md.getModuleRevisionId());
        log.debug("Ivy file: {}", ivyFile);

        File ivyProps = cacheManager.getResolvedIvyPropertiesInCache(md.getModuleRevisionId());
        log.debug("Ivy props: {}", ivyProps);

        log.debug("Artifact download reports");
        for (ArtifactDownloadReport adl : report.getAllArtifactsReports()) {
            Artifact artifact = adl.getArtifact();
            log.debug("    {} -> {}", artifact, adl.getLocalFile());
        }
    }
}