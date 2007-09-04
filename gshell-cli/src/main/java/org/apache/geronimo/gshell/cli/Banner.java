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

package org.apache.geronimo.gshell.cli;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Display helper for the sexy GShell banner.
 *
 * @version $Rev$ $Date$
 */
public class Banner
{
    ///CLOVER:OFF
    
    public static String getBanner() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        
        out.println("   ____ ____  _          _ _ ");
        out.println("  / ___/ ___|| |__   ___| | |");
        out.println(" | |  _\\___ \\| '_ \\ / _ \\ | |");
        out.println(" | |_| |___) | | | |  __/ | |");
        out.println("  \\____|____/|_| |_|\\___|_|_|");
        out.println();
        out.println(" GShell -- Geronimo command-line shell");
        out.flush();
        
        return writer.toString();
    }
}
