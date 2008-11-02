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

import java.util.List;

/**
 * Client to server message asking for possible completions
 *
 * @version $Rev: 697487 $ $Date: 2008-09-21 14:37:42 +0200 (Sun, 21 Sep 2008) $
 */
public class CompleteMessage 
    extends RshMessage
{
    private final String buffer;

    private final int cursor;

    public CompleteMessage(final String buffer, final int cursor) {
        this.buffer = buffer;
        this.cursor = cursor;
    }

    public String getBuffer() {
        return buffer;
    }

    public int getCursor() {
        return cursor;
    }

    /**
     * Server to client message to return the candidates.
     */
    public static class Result
        extends RshMessage
    {
        private final List<String> candidates;
        
        private final int position;

        public Result(final List<String> candidates, final int position) {
            this.candidates = candidates;
            this.position = position;
        }

        public List<String> getCandidates() {
            return candidates;
        }

        public int getPosition() {
            return position;
        }
    }


}
