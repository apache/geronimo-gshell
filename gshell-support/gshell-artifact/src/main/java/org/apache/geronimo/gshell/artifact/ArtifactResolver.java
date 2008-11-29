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
    void setTransferListener(TransferListener listener);
    
    Result resolve(Request request) throws Failure;

    //
    // Request
    //

    class Request
    {
        public ArtifactFilter filter;

        public Artifact artifact;

        public Collection<Artifact> artifacts;
    }

    //
    // Result
    //

    class Result
    {
        public Collection<Artifact> artifacts;
    }

    //
    // Failure
    //

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