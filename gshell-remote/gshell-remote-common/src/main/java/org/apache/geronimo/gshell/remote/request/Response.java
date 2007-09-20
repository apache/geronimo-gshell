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

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.message.Message;

//
// NOTE: Snatched and massaged from Apache Mina
//

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class Response
{
    private final Request request;

    private final Type type;

    private final Message message;

    public Response(final Request request, final Message message, final Type type) {
        assert request != null;
        assert message != null;
        assert type != null;

        this.request = request;
        this.type = type;
        this.message = message;
    }

    public Request getRequest() {
        return request;
    }

    public Type getType() {
        return type;
    }

    public Message getMessage() {
        return message;
    }

    public int hashCode() {
        return getRequest().getId().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (!(obj instanceof Response)) {
            return false;
        }

        Response resp = (Response) obj;

        return getRequest().equals(resp.getRequest()) && getType().equals(resp.getType());

    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //
    // Response Type
    //

    public static enum Type
    {
        WHOLE,
        PARTIAL,
        PARTIAL_LAST
    }
}
