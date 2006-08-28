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

package org.apache.geronimo.gshell;

import jline.CompletionHandler;
import jline.ConsoleReader;
import jline.CursorBuffer;

import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.MessageSourceImpl;

//
// NOTE: Based on jline.CandidateListCompletionHandler.  CLCH that comes with
//       jLine 0.9.5 has a bug that never displays the completion list.  When that
//       is fixed, then this class can be removed.
//

/**
 * A candindate list completion handler (that actually works).
 *
 * <p>
 * Follows the style of Bash.
 *
 * @version $Rev$ $Date$
 */
public class CompletionHandlerImpl
    implements CompletionHandler
{
    private static final Log log = LogFactory.getLog(CompletionHandlerImpl.class);

    private static MessageSource messages = new MessageSourceImpl(CompletionHandlerImpl.class.getName());

    public boolean complete(final ConsoleReader reader, final List candidates, final int pos)
        throws IOException
    {
        if (log.isDebugEnabled()) {
            log.debug("Complete; candicates=" + candidates + "; pos=" + pos);
        }

        CursorBuffer buf = reader.getCursorBuffer();

        // if there is only one completion, then fill in the buffer
        if (candidates.size() == 1) {
            String value = candidates.get(0).toString();

            // fail if the only candidate is the same as the current buffer
            if (value.equals(buf.toString())) {
                return false;
            }

            setBuffer(reader, value, pos);
            return true;
        }
        else if (candidates.size() > 1) {
            String value = getUnambiguousCompletions(candidates);
            setBuffer(reader, value, pos);
        }

        reader.printNewline();
        printCandidates(reader, candidates);

        // redraw the current console buffer
        reader.drawLine();

        return true;
    }


    private static void setBuffer(ConsoleReader reader, String value, int offset) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Setting buffer: value=" + value + "; offset=" + offset);
        }

        while (reader.getCursorBuffer().cursor >= offset && reader.backspace()) {
            // empty
        }

        reader.putString(value);
        reader.setCursorPosition(offset + value.length());
    }

    private void printCandidates(ConsoleReader reader, Collection<String> candidates) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Printing candidates: " + candidates);
        }

        Set<String> distinct = new HashSet<String>(candidates);

        if (distinct.size() > reader.getAutoprintThreshhold()) {
            reader.printString(messages.getMessage("display-candidates", candidates.size()) + " ");
            reader.flushConsole();

            int c;

            String no = messages.getMessage("display-candidates-no");
            String yes = messages.getMessage("display-candidates-yes");

            while ((c = reader.readCharacter(new char[]{ yes.charAt(0), no.charAt(0) })) != -1) {
                if (no.startsWith(String.valueOf(c))) {
                    reader.printNewline();
                    return;
                }
                else if (yes.startsWith(String.valueOf(c))) {
                    break;
                }
                else {
                    reader.beep();
                }
            }
        }

        // copy the values and make them distinct, without otherwise
        // affecting the ordering. Only do it if the sizes differ.
        if (distinct.size() != candidates.size()) {
            Collection<String> copy = new ArrayList<String>(candidates.size());

            for (String candidate : candidates) {
                copy.add(candidate);
            }

            candidates = copy;
        }

        reader.printColumns(candidates);
    }

    private String getUnambiguousCompletions(final List<String> candidates) {
        if (log.isDebugEnabled()) {
            log.debug("Get unambiguous completions: " + candidates);
        }

        if (candidates == null || candidates.size() == 0) {
            return null;
        }

        // convert to an array for speed
        String [] strings = candidates.toArray(new String[candidates.size()]);

        String first = strings[0];
        StringBuffer candidate = new StringBuffer();

        for (int i = 0; i < first.length(); i++) {
            if (startsWith(first.substring(0, i + 1), strings)) {
                candidate.append(first.charAt(i));
            }
            else {
                break;
            }
        }

        return candidate.toString();
    }

    private boolean startsWith(final String starts, final String[] candidates) {
        for (String candidate : candidates) {
            if (!candidate.startsWith(starts)) {
                return false;
            }
        }

        return true;
    }
}
