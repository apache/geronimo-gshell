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

package org.apache.geronimo.gshell.clp;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.geronimo.gshell.clp.handler.Handler;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceNotFoundException;

/**
 * Helper to print formatted help and usage text.
 *
 * @version $Rev$ $Date$
 */
public class Printer
{
    private CommandLineProcessor processor;

    private MessageSource messages;

    public Printer(final CommandLineProcessor processor) {
        assert processor != null;
        
        this.processor = processor;
    }

    public void setMessageSource(final MessageSource messages) {
        assert messages != null;

        this.messages = messages;
    }

    private String getMetaVariable(final Handler handler, final ResourceBundle bundle) {
        assert handler != null;

        String token = handler.descriptor.metaVar();
        if (token.length() == 0) {
            token = handler.getDefaultMetaVariable();
        }

        if (token == null) {
            return null;
        }

        if (bundle != null) {
            String localized = bundle.getString(token);

            if (localized != null) {
                token = localized;
            }
        }

        return token;
    }

    private String getNameAndMeta(final Handler handler, final ResourceBundle bundle) {
        assert handler != null;

        String str = (handler.descriptor instanceof ArgumentDescriptor) ? "" : handler.descriptor.toString();
    	String meta = getMetaVariable(handler, bundle);

        if (meta != null) {
            if (str.length() > 0) {
                str += " ";
            }
            str += meta;
    	}
        
        return str;
    }

    private int getPrefixLen(final Handler handler, final ResourceBundle bundle) {
        assert handler != null;

        if (handler.descriptor.description().length() == 0) {
            return 0;
        }

        return getNameAndMeta(handler, bundle).length();
    }

    public void printUsage(final Writer writer, final ResourceBundle bundle, final String name) {
        assert writer != null;

        PrintWriter out = new PrintWriter(writer);

        List<Handler> argumentHandlers = new ArrayList<Handler>();
        argumentHandlers.addAll(processor.getArgumentHandlers());

        List<Handler> optionHandlers = new ArrayList<Handler>();
        optionHandlers.addAll(processor.getOptionHandlers());

        // For display purposes, we like the argument handlers in argument order, but the option handlers in alphabetical order
        Collections.sort(optionHandlers, new Comparator<Handler>() {
            public int compare(Handler a, Handler b) {
                return a.descriptor.toString().compareTo(b.descriptor.toString());
            }
        });

        //
        // TODO: i18n, pull for standard messages, not from command's messages
        //
        
        if (name != null) {
        	String syntax = "syntax: " + name;
        	if (!optionHandlers.isEmpty()) {
        		syntax += " [options]";
        	}
        	if (!argumentHandlers.isEmpty()) {
        		syntax += " [arguments]";
        	}
        	out.println(syntax);
        	out.println();
        }

        // Compute the maximum length of the syntax column
        int len = 0;
        

        for (Handler handler : optionHandlers) {
            int curLen = getPrefixLen(handler, bundle);
            len = Math.max(len, curLen);
        }

        for (Handler handler : argumentHandlers) {
            int curLen = getPrefixLen(handler, bundle);
            len = Math.max(len, curLen);
        }

        // And then render the handler usage
        if (!argumentHandlers.isEmpty()) {
        	out.println("arguments:");
        }
        for (Handler handler : argumentHandlers) {
            printHandler(out, handler, len, bundle);
        }

        if (!optionHandlers.isEmpty()) {
        	out.println();
        	out.println("options:");
        }
        for (Handler handler : optionHandlers) {
            printHandler(out, handler, len, bundle);
        }

        out.flush();
    }

    public void printUsage(final Writer writer) {
        printUsage(writer, null, null);
    }
    
    public void printUsage(final Writer writer, final String name) {
    	printUsage(writer, null, name);
    }

    /**
     * Get the description for the given descriptor, using any configured messages for i18n support.
     */
    private String getDescription(final Descriptor descriptor) {
        assert descriptor != null;

        String message = descriptor.description();

        if (message != null && messages != null) {
            try {
                message = messages.getMessage(message);
            }
            catch (ResourceNotFoundException e) {
                // Just use the code
            }
        }

        return message;
    }

    private void printHandler(final PrintWriter out, final Handler handler, final int len, final ResourceBundle bundle) {
        assert out != null;
        assert handler != null;

        //
        // TODO: Expose these as configurables
        //
        
        int terminalWidth = 80;
        String prefix = "  ";
        String separator = "    ";
        int prefixSeperatorWidth = prefix.length() + separator.length();
        int descriptionWidth = terminalWidth - len - prefixSeperatorWidth;

        // Only render if their is a discription, else its hidden
        String desc = getDescription(handler.descriptor);
        if (desc.length() == 0) {
            return;
        }

        // Render the prefix and syntax
        String nameAndMeta = getNameAndMeta(handler, bundle);
        out.print(prefix);
        out.print(nameAndMeta);

        // Render the seperator
        for (int i = nameAndMeta.length(); i < len; ++i) {
            out.print(' ');
       	}
        out.print(separator);

        // Localize the description if we can
        if (bundle != null) {
            desc = bundle.getString(desc);
        }

        // Render the description splitting it over multipule lines if its longer than column size
        while (desc != null && desc.length() > 0) {
            //
            // FIXME: Only split on words
            //

            int i = desc.indexOf('\n');

            if (i >= 0 && i <= descriptionWidth) {
                out.println(desc.substring(0, i));
                desc = desc.substring(i + 1);

                if (desc.length() > 0) {
                    indent(out, len + prefixSeperatorWidth);
                }

                continue;
            }

            if (desc.length() <= descriptionWidth) {
                out.println(desc);
                break;
            }

            out.println(desc.substring(0, descriptionWidth));
            desc = desc.substring(descriptionWidth);
            indent(out, len + prefixSeperatorWidth);
        }
    }

    private void indent(final PrintWriter out, int i) {
        assert out != null;

        for (; i>0; i--) {
            out.print(' ');
        }
    }
}
