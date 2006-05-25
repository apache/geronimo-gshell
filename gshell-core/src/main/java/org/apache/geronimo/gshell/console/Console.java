/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.console;

import java.io.IOException;

/**
 * Abstraction of a console.
 *
 * <p>Allows pluggable implemenations (like to enable readline, etc.)
 *
 * @version $Id: IO.java 399599 2006-05-04 08:13:57Z jdillon $
 */
public interface Console
{
    String readLine(final String prompt) throws IOException;
}