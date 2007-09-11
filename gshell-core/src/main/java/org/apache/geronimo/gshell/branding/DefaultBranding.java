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

package org.apache.geronimo.gshell.branding;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.geronimo.gshell.ansi.Buffer;
import org.apache.geronimo.gshell.ansi.Code;
import org.apache.geronimo.gshell.ansi.RenderWriter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import jline.Terminal;

//
// FIXME: Move this to a seperate module so that folks can omit this and use their own easily
//

/**
 * Provides the default branding for GShell.
 *
 * @version $Rev$ $Date$
 */
@Component(role=Branding.class, hint="default")
public class DefaultBranding
    implements Branding
{
    @Requirement
    private VersionLoader versionLoader;

    @Requirement
    private Terminal terminal;

    public String getName() {
        return "gshell";
    }

    public String getDisplayName() {
        return "GShell";
    }

    public String getProgramName() {
        return System.getProperty("program.name", "gsh");
    }

    public File getUserDirectory() {
        File userHome = new File(System.getProperty("user.home"));

        File dir = new File(userHome, "." + getName());

        return dir.getAbsoluteFile();
    }

    public File getSharedDirectory() {
        //
        // FIXME: This is not very portable :-(
        //
        
        File dir = new File("/etc", getName());

        return dir.getAbsoluteFile();
    }

    public String getAbout() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);

        out.println("For information about @|cyan GShell|, visit:");
        out.println("    @|bold http://geronimo.apache.org/gshell.html| ");
        out.flush();

        return writer.toString();
    }

    public String getVersion() {
        return versionLoader.getVersion();
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
    
    public String getWelcomeBanner() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);
        Buffer buff = new Buffer();

        for (String line : BANNER) {
            buff.attrib(line, Code.CYAN);
            out.println(buff);
        }
        
        out.println();
        out.println(" @|bold GShell| (" + getVersion() + ")");
        out.println();
        out.println("Type '@|bold help|' for more information.");

        // If we can't tell, or have something bogus then use a reasonable default
        int width = terminal.getTerminalWidth();
        if (width < 1) {
            width = 80;
        }
        
        out.print(StringUtils.repeat("-", width - 1));

        out.flush();

        return writer.toString();
    }

    public String getProfileScriptName() {
        return getName() + ".profile";
    }

    public String getInteractiveScriptName() {
        return getName() + ".rc";
    }

    public String getHistoryFileName() {
        return getName() + ".history";
    }

    public String getPropertyName(final String name) {
        assert name != null;
        
        return getName() + "." + name;
    }

    public String getProperty(final String name) {
        return System.getProperty(getPropertyName(name));
    }

    public String getProperty(final String name, final String defaultValue) {
        String value = getProperty(name);
        
        if (value == null) {
            return defaultValue;
        }

        return value;
    }
}