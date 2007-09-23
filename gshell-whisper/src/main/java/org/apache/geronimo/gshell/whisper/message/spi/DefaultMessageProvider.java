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

package org.apache.geronimo.gshell.whisper.message.spi;

import java.util.Set;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.crypto.CryptoContext;
import org.apache.geronimo.gshell.whisper.request.RequestEncoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class DefaultMessageProvider
    implements MessageProvider
{
    private MessageHeader header;

    private Set<Class<?>> types;

    private MessageFactory factory;

    private MessageIDFactory idFactory;

    private MessageMarshaller marshaller;
    
    private ProtocolCodecFactory codecFactory;

    private CryptoContext cryptoContext;

    public DefaultMessageProvider(final MessageHeader header, final Set<Class<?>> messageTypes) {
        this.header = header;
        
        this.types = messageTypes;

        this.factory = new DefaultMessageFactory();

        this.idFactory = new LongMessageID.Factory();

        this.marshaller = new DefaultMessageMarshaller(this);

        DemuxingProtocolCodecFactory factory = new DemuxingProtocolCodecFactory();

        factory.register(new MessageDecoder.Factory(this));

        factory.register(new MessageEncoder.Factory(this));

        //
        // TODO: Need to find a better way to get this puppy registered na...
        //

        factory.register(new RequestEncoder.Factory(this));

        this.codecFactory = factory;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

    public MessageHeader getMessageHeader() {
        return header;
    }

    public void setMessageHeader(final MessageHeader header) {
        this.header = header;
    }

    public Set<Class<?>> getMessageTypes() {
        return types;
    }

    public void setMessageTypes(final Set<Class<?>> types) {
        this.types = types;
    }

    public MessageFactory getMessageFactory() {
        return factory;
    }

    public void setMessageFactory(final MessageFactory factory) {
        this.factory = factory;
    }

    public MessageIDFactory getMessageIDFactory() {
        return idFactory;
    }

    public void setMessageIDFactory(final MessageIDFactory idFactory) {
        this.idFactory = idFactory;
    }

    public MessageMarshaller getMessageMarshaller() {
        return marshaller;
    }

    public void setMessageMarshaller(final MessageMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public ProtocolCodecFactory getProtocolCodecFactory() {
        return codecFactory;
    }

    public void setProtocolCodecFactory(final ProtocolCodecFactory factory) {
        this.codecFactory = factory;
    }

    public CryptoContext getCryptoContext() {
        return cryptoContext;
    }

    public void setCryptoContext(final CryptoContext context) {
        this.cryptoContext = context;
    }
}