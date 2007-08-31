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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for ANSI color codes.
 *
 * @version $Rev$ $Date$
 */
public class Code
{
    public static final int OFF = 0;
    public static final int BOLD = 1;
    public static final int UNDERSCORE = 4;
    public static final int BLINK = 5;
    public static final int REVERSE = 7;
    public static final int CONCEALED = 8;

    public static final int FG_BLACK = 30;
    public static final int FG_RED = 31;
    public static final int FG_GREEN = 32;
    public static final int FG_YELLOW = 33;
    public static final int FG_BLUE = 34;
    public static final int FG_MAGENTA = 35;
    public static final int FG_CYAN = 36;
    public static final int FG_WHITE = 37;

    public static final int BLACK = FG_BLACK;
    public static final int RED = FG_RED;
    public static final int GREEN = FG_GREEN;
    public static final int YELLOW = FG_YELLOW;
    public static final int BLUE = FG_BLUE;
    public static final int MAGENTA = FG_MAGENTA;
    public static final int CYAN = FG_CYAN;
    public static final int WHITE = FG_WHITE;

    public static final int BG_BLACK = 40;
    public static final int BG_RED = 41;
    public static final int BG_GREEN = 42;
    public static final int BG_YELLOW = 43;
    public static final int BG_BLUE = 44;
    public static final int BG_MAGENTA = 45;
    public static final int BG_CYAN = 46;
    public static final int BG_WHITE = 47;

    /** The ANSI escape char which is used to start sequences. */
    private static final char ESC = 27;

    /** A map of code names to values. */
    private static final Map<String,Integer> NAMES_TO_CODES;

    /** A map of codes to name. */
    private static final Map<Integer,String> CODES_TO_NAMES;

    static {
        Field[] fields = Code.class.getDeclaredFields();
        Map<String,Integer> names = new HashMap<String,Integer>(fields.length);
        Map<Integer,String> codes = new HashMap<Integer,String>(fields.length);

        try {
            for (Field field : fields) {
                // Skip anything non-public, all public fields are codes
                int mods = field.getModifiers();
                if (!Modifier.isPublic(mods)) {
                    continue;
                }

                String name = field.getName();
                Integer code = (Integer) field.get(Code.class);

                names.put(name, code);
                codes.put(code, name);
            }
        }
        catch (IllegalAccessException e) {
            // This should never happen
            throw new Error(e);
        }

        NAMES_TO_CODES = names;
        CODES_TO_NAMES = codes;
    }

    /**
     * Returns the ANSI code for the given symbolic name.  Supported symbolic names are all defined as
     * fields in {@link Code} where the case is not significant.
     */
    public static int forName(final String name) throws IllegalArgumentException {
        assert name != null;

        // All names in the map are upper-case
        String tmp = name.toUpperCase();
        Integer code = NAMES_TO_CODES.get(tmp);

        if (code == null) {
            throw new IllegalArgumentException("Invalid ANSI code name: " + name);
        }

        return code;
    }

    /**
     * Returns the symbolic name for the given ANSI code.
     */
    public static String name(final int code) throws IllegalArgumentException {
        assert code >= 0;
        
        String name = CODES_TO_NAMES.get(code);

        if (name == null) {
            throw new IllegalArgumentException("Invalid ANSI code: " + code);
        }

        return name;
    }

    public static String attrib(final int attr) {
        return ESC + "[" + attr + "m";
    }
}
