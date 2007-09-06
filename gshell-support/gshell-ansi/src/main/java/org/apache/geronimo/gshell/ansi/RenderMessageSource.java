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

package org.apache.geronimo.gshell.ansi;

import org.apache.geronimo.gshell.i18n.MessageSource;

/**
 * Message source adapter which supports automatic ANSI color rendering.
 *
 * @version $Rev$ $Date$
 */
public class RenderMessageSource
    implements MessageSource
{
    private final Renderer renderer = new Renderer();

    private final MessageSource source;

    public RenderMessageSource(final MessageSource source) {
        assert source != null;

        this.source = source;
    }
    
    public String getMessage(final String code) {
        String msg = source.getMessage(code);

        if (Renderer.test(msg)) {
            return renderer.render(msg);
        }

        return msg;
    }

    public String format(String code, Object... args) {
        String msg = source.format(code, args);

        if (Renderer.test(msg)) {
            return renderer.render(msg);
        }

        return msg;
    }
}
