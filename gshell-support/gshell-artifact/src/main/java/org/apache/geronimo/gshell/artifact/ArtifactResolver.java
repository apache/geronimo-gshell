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

package org.apache.geronimo.gshell.artifact;

import org.apache.geronimo.gshell.artifact.transfer.TransferListener;

import java.util.Collection;

/**
 * Provides resolution of artifacts.
 *
 * @version $Rev$ $Date$
 */
public interface ArtifactResolver
{
    /**
     * Install a transfer listener.
     */
    void setTransferListener(TransferListener listener);

    /**
     * Resolve a request.
     */
    Result resolve(Request request) throws Failure;

    /**
     * Artifact resolution request.
     */
    class Request
    {
        /**
         * Artifact filter.
         */
        public ArtifactFilter filter;

        /**
         * Originating artifact.
         */
        public Artifact artifact;

        /**
         * Dependency artifacts.
         */
        public Collection<Artifact> artifacts;
    }

    /**
     * Artifact resolution result.
     */
    class Result
    {
        /**
         * Resolved artifacts.
         */
        public Collection<Artifact> artifacts;
    }

    /**
     * Artifact resolution failure.
     */
    class Failure
        extends Exception
    {
        private static final long serialVersionUID = 1;

        public Failure(final String msg) {
            super(msg);
        }

        public Failure(final String msg, final Throwable cause) {
            super(msg, cause);
        }

        public Failure(final Throwable cause) {
            super(cause);
        }

        public Failure() {
            super();
        }
    }
}