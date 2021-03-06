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

package org.apache.geronimo.gshell.clp.handler;

import org.apache.geronimo.gshell.clp.Descriptor;
import org.apache.geronimo.gshell.clp.ProcessingException;
import org.apache.geronimo.gshell.clp.StopProcessingOptionsNotification;
import org.apache.geronimo.gshell.clp.setter.Setter;

/**
 * Handler to abort further option processing (and soak up remaining tokens as arguments).
 *
 * @version $Rev$ $Date$
 */
public class StopHandler
    extends Handler<String>
{
    public StopHandler(Descriptor desc, Setter<? super String> setter) {
        super(desc, setter);
    }

    @Override
    public int handle(final Parameters params) throws ProcessingException {
        throw new StopProcessingOptionsNotification();
    }

    @Override
    public String getDefaultToken() {
        return "ARGUMENTS";
    }
}
