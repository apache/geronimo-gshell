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

package org.apache.geronimo.gshell.remote.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feeds data from an input stream to an output stream.
 *
 * @version $Rev$ $Date$
 */
public class StreamFeeder
    implements Runnable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final InputStream input;

    private final OutputStream output;

    private volatile boolean running;

    public StreamFeeder(final InputStream input, final OutputStream output) {
        assert input != null;
        assert output != null;
        
        this.input = input;
        this.output = output;
    }

    public void run() {
        running = true;

        log.debug("Running");

        //
        // TODO: Look into using a byte[] buffer here to read larger chunks at the same time?
        //
        
        try {
            int b;

            while (running && ((b = input.read()) != -1)) {
                output.write(b);
            }
        }
        catch (Throwable e) {
            log.error("Feed failure: " + e, e);
        }
        finally {
            close();
        }

        log.debug("Stopped");
    }

    public boolean isRunning() {
        return running;
    }
    
    public void close() {
        running = false;
        
        log.debug("Closed");
    }

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);

    public Thread createThread() {
        Thread t = new Thread(this, getClass().getSimpleName() + "-" + THREAD_COUNTER.getAndIncrement());

        t.setDaemon(true);
        
        return t;
    }
}