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

package org.apache.geronimo.gshell.wisdom.branding;

import org.apache.geronimo.gshell.ansi.Buffer;
import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.RenderWriter;
import org.apache.geronimo.gshell.model.Branding;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A custom branding for the default GShell application.
 *
 * @version $Rev$ $Date$
 */
public class GShellBranding
    extends Branding
{
    public GShellBranding() {}

    @Override
    public String getName() {
        return "gshell";
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        return "GShell";
    }

    @Override
    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProgramName() {
        return "gsh";
    }
    
    @Override
    public void setProgramName(String programName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAboutMessage() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);

        out.println("For information about @|cyan GShell|, visit:");
        out.println("    @|bold http://gshell.org| ");
        out.flush();

        return writer.toString();
    }

    @Override
    public void setAboutMessage(final String aboutMessage) {
        throw new UnsupportedOperationException();
    }

    /*
    // Figlet font name: ???
    private static final String[] BANNER = {
        "   ____ ____  _          _ _ ",
        "  / ___/ ___|| |__   ___| | |",
        " | |  _\\___ \\| '_ \\ / _ \\ | |",
        " | |_| |___) | | | |  __/ | |",
        "  \\____|____/|_| |_|\\___|_|_|",
    };
    */

    // Figlet font name: Georgia11
    private static final String[] BANNER = {
        "                          ,,                 ,,    ,,",
        "   .g8\"\"\"bgd   .M\"\"\"bgd `7MM               `7MM  `7MM",
        " .dP'     `M  ,MI    \"Y   MM                 MM    MM",
        " dM'       `  `MMb.       MMpMMMb.  .gP\"Ya   MM    MM",
        " MM             `YMMNq.   MM    MM ,M'   Yb  MM    MM",
        " MM.    `7MMF'.     `MM   MM    MM 8M\"\"\"\"\"\"  MM    MM",
        " `Mb.     MM  Mb     dM   MM    MM YM.    ,  MM    MM",
        "   `\"bmmmdPY  P\"Ybmmd\"  .JMML  JMML.`Mbmmd'.JMML..JMML."
    };

    /*
    // Figlet font name: Georgia11
    private static final String[] BANNER = {
        "                          ,,                 ,,    ,,             ..",
        "   .g8\"\"\"bgd   .M\"\"\"bgd `7MM               `7MM  `7MM              `bq",
        " .dP'     `M  ,MI    \"Y   MM                 MM    MM                YA",
        " dM'       `  `MMb.       MMpMMMb.  .gP\"Ya   MM    MM                `Mb",
        " MM             `YMMNq.   MM    MM ,M'   Yb  MM    MM      mmmmmmmmm  8M",
        " MM.    `7MMF'.     `MM   MM    MM 8M\"\"\"\"\"\"  MM    MM                 8M",
        " `Mb.     MM  Mb     dM   MM    MM YM.    ,  MM    MM      mmmmmmmmm ,M9",
        "   `\"bmmmdPY  P\"Ybmmd\"  .JMML  JMML.`Mbmmd'.JMML..JMML.              dM",
        "                                                                   .pY"
    };
    */

    @Override
    public String getWelcomeMessage() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);
        Buffer buff = new Buffer();

        for (String line : BANNER) {
            buff.attrib(line, Code.CYAN);
            out.println(buff);
        }

        out.println();
        out.print(" @|bold GShell| (");
        out.print(getParent().getVersion());
        out.println(")");
        out.println();
        out.println("Type '@|bold help|' for more information.");

        out.flush();

        return writer.toString();
    }

    @Override
    public void setWelcomeMessage(String welcomeMessage) {
        throw new UnsupportedOperationException();
    }
}