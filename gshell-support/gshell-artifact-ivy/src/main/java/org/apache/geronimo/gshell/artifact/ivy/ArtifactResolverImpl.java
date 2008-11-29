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

import org.apache.geronimo.gshell.artifact.ArtifactResolver;
import org.apache.geronimo.gshell.artifact.Artifact;
import org.apache.geronimo.gshell.artifact.transfer.TransferListener;
import org.apache.ivy.Ivy;
import org.apache.ivy.util.filter.Filter;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;

/**
 * <a href="http://ant.apache.org/ivy">Apache Ivy</a> based {@link ArtifactResolver}.
 *
 * @version $Rev$ $Date$
 */
public class ArtifactResolverImpl
    implements ArtifactResolver
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Ivy ivy;

    public ArtifactResolverImpl(final Ivy ivy) {
        assert ivy != null;

        this.ivy = ivy;
    }

    public void setTransferListener(final TransferListener listener) {
        // Ignore for now
    }

    public Result resolve(final Request request) throws Failure {
        assert request != null;

        ResolveOptions options = new ResolveOptions();
        options.setOutputReport(true);
        options.setTransitive(true);

        AndArtifactFilter filter = new AndArtifactFilter();
        options.setArtifactFilter(filter);

        // Filter deps needed for use of apache-ivy
        filter.add(new IvyDependenciesFilter());

        if (request.filter != null) {
            log.debug("Filter: {}", request.filter);

            filter.add(new Filter() {
                public boolean accept(final Object obj) {
                    if (!(obj instanceof org.apache.ivy.core.module.descriptor.Artifact)) {
                        return false;
                    }

                    Artifact artifact = createArtifact((org.apache.ivy.core.module.descriptor.Artifact)obj);
                    return request.filter.accept(artifact);
                }
            });
        }

        ModuleDescriptor md = createModuleDescriptor(request);

        Result result = new Result();

        try {
            ResolveReport resolveReport = ivy.resolve(md, options);
            result.artifacts = new LinkedHashSet<Artifact>();

            log.debug("Resolved:");

            for (ArtifactDownloadReport downloadReport : resolveReport.getAllArtifactsReports()) {
                Artifact artifact = createArtifact(downloadReport.getArtifact());
                artifact.setFile(downloadReport.getLocalFile());

                log.debug("    {}", artifact);

                result.artifacts.add(artifact);
            }
        }
        catch (Exception e) {
            throw new Failure(e);
        }

        return result;
    }

    private Artifact createArtifact(final org.apache.ivy.core.module.descriptor.Artifact source) {
        assert source != null;

        ModuleRevisionId sourceId = source.getModuleRevisionId();

        Artifact artifact = new Artifact();
        artifact.setGroup(sourceId.getOrganisation());
        artifact.setName(source.getName());
        artifact.setVersion(sourceId.getRevision());
        artifact.setType(source.getType());

        return artifact;
    }

    private ModuleDescriptor createModuleDescriptor(final Request request) {
        assert request != null;

        log.debug("Artifact: {}", request.artifact);

        ModuleRevisionId id = ModuleRevisionId.newInstance(request.artifact.getGroup(), request.artifact.getName(), request.artifact.getVersion());
        DefaultModuleDescriptor md = new DefaultModuleDescriptor(id, "integration", null, true);
        md.addConfiguration(new Configuration("default"));
        md.setLastModified(System.currentTimeMillis());

        if (request.artifacts != null) {
            log.debug("Dependencies:");

            for (Artifact artifact : request.artifacts) {
                log.debug("    {}", artifact);

                ModuleRevisionId depId = ModuleRevisionId.newInstance(artifact.getGroup(), artifact.getName(), artifact.getVersion());
                DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, depId, false, false, true);
                dd.addDependencyConfiguration("default", "default");
                md.addDependency(dd);
            }
        }

        return md;
    }
}