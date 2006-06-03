/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.mojo.minijar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Clazzpath
{

    final Collection units = new ArrayList();

    final Map missing = new HashMap();

    final Map clazzes = new HashMap();

    public Clazzpath()
    {
    }

    public Collection getClazzes()
    {
        return clazzes.values();
    }

    public Collection getMissingClazzes()
    {
        return missing.values();
    }

    public Clazz getClazz( final String pClazzName )
    {
        return (Clazz) clazzes.get( pClazzName );
    }

    public ClazzpathUnit[] getUnits()
    {
        return (ClazzpathUnit[]) units.toArray( new ClazzpathUnit[units.size()] );
    }

}
