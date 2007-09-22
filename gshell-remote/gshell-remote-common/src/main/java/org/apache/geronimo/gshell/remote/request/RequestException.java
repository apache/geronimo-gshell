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

package org.apache.geronimo.gshell.remote.request;

import org.apache.geronimo.gshell.remote.message.Message;

/**
 * Thrown to indicate a request operation failed.
 *
 * @version $Rev: 577545 $ $Date: 2007-09-19 21:55:19 -0700 (Wed, 19 Sep 2007) $
 */
public class RequestException
    extends RuntimeException
{
    private static final long serialVersionUID = 1;

    public RequestException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public RequestException(final String msg) {
        super(msg);
    }

    public RequestException(final Throwable cause) {
        super(cause);
    }

    public RequestException(final Message.ID id) {
        super(String.valueOf(id));
    }

    public RequestException() {
        super();
    }
}