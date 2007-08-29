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

package org.apache.geronimo.gshell.util;

import java.util.List;

/**
 * Utils for command-line arguments.
 *
 * @version $Rev$ $Date$
 */
public class Arguments
{
    public static Object[] shift(final Object[] args) {
        return shift(args, 1);
    }

    public static Object[] shift(final Object[] args, int pos) {
        assert args != null;
        assert args.length >= pos;

        String[] _args = new String[args.length - pos];
        System.arraycopy(args, pos, _args, 0, _args.length);
        return _args;
    }

    public static String asString(final Object[] args) {
        assert args != null;

        StringBuffer buff = new StringBuffer();

        for (int i=0; i<args.length; i++ ) {
            buff.append(args[i]);
            if (i + 1 < args.length) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }

    public static String asString(final List args) {
        assert args != null;

        StringBuffer buff = new StringBuffer();

        for (int i=0; i<args.size(); i++ ) {
            buff.append(args.get(i));
            if (i + 1 < args.size()) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }

    public static String[] toStringArray(final Object[] args) {
        assert args != null;

        String[] strings = new String[args.length];

        for (int i=0; i<args.length; i++ ) {
            strings[i] = String.valueOf(args[i]);
        }

        return strings;
    }
}
