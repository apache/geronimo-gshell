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

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Messages for the {@link org.apache.geronimo.gshell.clp.handler} package.
 *
 * @version $Rev$ $Date$
 */
enum Messages
{
    ILLEGAL_OPERAND,
    ILLEGAL_BOOLEAN,
    ;

    private static ResourceBundle bundle;

    String format(Object... args) {
        assert args != null;
        
        synchronized (Messages.class) {
            if (bundle == null) {
                bundle = ResourceBundle.getBundle(Messages.class.getName());
            }
            
            return MessageFormat.format(bundle.getString(name()), args);
        }
    }
}
