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

package org.apache.geronimo.gshell.whisper.transport;

/**
 * Exception thrown when the TranportFactoryLocator can not find the given transport
 *
 * @version $Rev: 579828 $ $Date: 2007-09-27 00:15:42 +0200 (Thu, 27 Sep 2007) $
 */
public class LookupException extends TransportException {

    public LookupException(final String name) {
        super("Unabled to lookup: " + name, null);
    }

    public LookupException(final String name, Throwable cause) {
        super("Unabled to lookup: " + name, cause);
    }
}
