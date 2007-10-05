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

package org.apache.geronimo.gshell.whisper.rfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URL connection <tt>rfile</tt> (remote-file) protocol.
 *
 * @version $Rev$ $Date$
 */
public class RemoteFileURLConnection
    extends URLConnection
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    RemoteFileURLConnection(final URL url /* TODO: We need a session and some details */) {
        super(url);

        //
        // TODO: Probably need to have the session passed in?  And some initial file state details er something?
        //
    }

    @Override
    public void connect() throws IOException {
        //
        // TODO:
        //
    }

    //
    // TODO: May want to lean on the Session<Input|Output>Stream impl here...
    //

    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }

        //
        // TODO:
        //

        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            connect();
        }

        //
        // TODO:
        //

        return null;
    }

    @Override
    public Permission getPermission() throws IOException {
        //
        // TODO:
        //
        
        /*
        // Detect if we have read/write perms
        String perms = null;

        if (file.canRead()) {
            perms = "read";
        }

        if (file.canWrite()) {
            if (perms != null) {
                perms += ",write";
            }
            else {
                perms = "write";
            }
        }

        // File perms need filename to be in system format
        String filename = ParseUtil.decode(url.getPath());
        if (File.separatorChar != '/') {
            filename.replace('/', File.separatorChar);
        }

        return new FilePermission(filename, perms);
        */

        return null;
    }

    @Override
    public long getLastModified() {
        //
        // TODO:
        //

        return -1;
    }

    @Override
    public long getDate() {
        //
        // TODO:
        //

        return -1;
    }

    @Override
    public int getContentLength() {
        //
        // TODO:
        //

        return -1;

        /*
        final long value = file.length();
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalStateException("Can not safly convert to int: " + value);
        }

        return (int) value;
        */
    }

    @Override
    public String getContentType() {
        //return getFileNameMap().getContentTypeFor(file.getName());

        //
        // TODO:
        //

        return null;
    }

    @Override
    public String getHeaderField(final String name) {
        assert name != null;

        String headerName = name.toLowerCase();

        if (headerName.equals("last-modified")) {
            return String.valueOf(getLastModified());
        }
        else if (headerName.equals("content-length")) {
            return String.valueOf(getContentLength());
        }
        else if (headerName.equals("content-type")) {
            return getContentType();
        }
        else if (headerName.equals("date")) {
            return String.valueOf(getDate());
        }

        return super.getHeaderField(name);
    }

    @Override
    public Map<String,List<String>> getHeaderFields() {
        Map<String,List<String>> headers = new HashMap<String,List<String>>();

        String[] headerNames = {
            "last-modified",
            "content-length",
            "content-type",
            "date"
        };

        for (String name : headerNames) {
            List<String> list = new ArrayList<String>(1);

            list.add(getHeaderField(name));

            headers.put(name, Collections.unmodifiableList(list));
        }

        return Collections.unmodifiableMap(headers);
    }
}