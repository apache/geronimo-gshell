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

package org.apache.geronimo.gshell.remote.message;

import org.apache.geronimo.gshell.remote.crypto.CryptoContext;
import org.apache.geronimo.gshell.remote.request.RequestEncoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Provides encoding and decoding support for {@link Message} instances.
 *
 * @version $Rev$ $Date$
 */
@Component(role=MessageCodecFactory.class)
public class MessageCodecFactory
    extends DemuxingProtocolCodecFactory
    implements Initializable
{
    @Requirement
    private CryptoContext crypto;

    public MessageCodecFactory() {}

    public void initialize() throws InitializationException {
        register(new MessageEncoder.Factory(crypto));

        register(new MessageDecoder.Factory(crypto));

        //
        // TODO: For now hook up the reqeust encoding here, would like to find a better way though...
        //

        register(new RequestEncoder.Factory(crypto));
    }
}