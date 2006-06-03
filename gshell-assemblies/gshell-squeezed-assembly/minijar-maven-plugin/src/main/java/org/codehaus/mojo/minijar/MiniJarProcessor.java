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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;

public final class MiniJarProcessor
{

    private final Clazzpath clazzpath = new Clazzpath();

    private List roots = new ArrayList();

    private File dir;

    public MiniJarProcessor()
    {
    }

    public void addRootUnit( final String pLocation )
        throws IOException
    {
        roots.add( new ClazzpathUnit( clazzpath, pLocation ) );
    }

    public void addUnit( final String pLocation )
        throws IOException
    {
        new ClazzpathUnit( clazzpath, pLocation );
    }

    public void setOutputPath( final String pPath )
    {
        dir = new File( pPath );
    }

    private Collection getAllClazzes() {
        return clazzpath.getClazzes();
    }

    private Collection getMissingClazzes() {
        return clazzpath.getMissingClazzes();
    }

    private Collection getJarClazzes() {
        Collection c = new HashSet();
        Iterator iter = roots.iterator();
        while (iter.hasNext()) {
            ClazzpathUnit clazzpath = (ClazzpathUnit)iter.next();
            c.addAll(clazzpath.getClazzes());
        }

        return c;
    }

    private Collection getJarDependencies() {
        Collection c = new HashSet();
        Iterator iter = roots.iterator();
        while (iter.hasNext()) {
            ClazzpathUnit clazzpath = (ClazzpathUnit)iter.next();
            c.addAll(clazzpath.getDependencies());
        }

        return c;
    }

    private Collection getJarTransitiveDependencies() {
        Collection c = new HashSet();
        Iterator iter = roots.iterator();
        while (iter.hasNext()) {
            ClazzpathUnit clazzpath = (ClazzpathUnit)iter.next();
            c.addAll(clazzpath.getTransitiveDependencies());
        }

        return c;
    }

    public void generate( final Console pConsole )
        throws IOException
    {
        final Collection allClazzes = getAllClazzes();
        final Collection missing = getMissingClazzes();
        final Collection jarClazzes = getJarClazzes();
        final Collection jarDependencies = getJarDependencies();
        final Collection jarTransitiveDependencies = getJarTransitiveDependencies();

        final Collection remove = new ArrayList();
        remove.addAll( allClazzes );
        remove.removeAll( jarClazzes );
        remove.removeAll( jarTransitiveDependencies );

        final Matcher matcher = new Matcher()
        {
            final Set removeSet = new HashSet( remove );

            public boolean isMatching( final String pName )
            {
                final String clazzName = pName.substring( 0, pName.length() - 6 ).replace( '/', '.' );
                final Clazz clazz = new Clazz( clazzName );
                final boolean removeIt = remove.contains( clazz );

                /*
                 if (removeIt) {
                 System.out.println(" removing " + clazz);
                 } else {
                 final Clazz c = classpath.getClazz(clazzName);
                 if (c == null) {
                 System.out.println(" hmm... don't know anything about " + clazzName);
                 } else {
                 System.out.println(" keeping " + c + " because of " + c.getReferences());
                 }
                 }
                 */

                return !removeIt;
            }
        };

        final ClazzpathUnit[] units = clazzpath.getUnits();
        for ( int i = 0; i < units.length; i++ )
        {
            final ClazzpathUnit unit = units[i];
            unit.write( dir, matcher, pConsole );
        }
    }

    //        System.out.println("jar classes " + jarClazzes.size());
    //        System.out.println("jar dependencies " + jarDependencies.size());
    //        System.out.println("jar transitive dependencies " + jarTransitiveDependencies.size());
    //
    //        final Collection remove = new ArrayList();
    //        remove.addAll(allClazzes);
    //        remove.removeAll(jarClazzes);
    //        remove.removeAll(jarTransitiveDependencies);
    //        
    //        System.out.println("we can remove " + remove.size() + " of the total " + allClazzes.size());
    //        
    //        System.out.println("--seed deps");
    //
    //        final List seedList = new ArrayList(jarDependencies);
    //        Collections.sort(seedList);
    //
    //        for (final Iterator it = seedList.iterator(); it.hasNext();) {
    //            final Clazz clazz = (Clazz) it.next();
    //            System.out.println("seed: " + clazz);            
    //        }
    //
    //        System.out.println("--removing");
    //
    //        final List removeList = new ArrayList(remove);
    //        Collections.sort(removeList);
    //
    //        for (final Iterator it = removeList.iterator(); it.hasNext();) {
    //            final Clazz clazz = (Clazz) it.next();
    //            if (clazz.getReferences().size() != 0) {
    //                System.out.print("!!");
    //            }
    //            System.out.println("remove: " + clazz + " referenced by " + clazz.getReferences());            
    //        }
    //        
    //        System.out.println("--missing");
    //
    //        final List missingList = new ArrayList(missing);
    //        Collections.sort(missingList);
    //        
    //        for (final Iterator it = missingList.iterator(); it.hasNext();) {
    //            final Clazz clazz = (Clazz) it.next();
    //            System.out.println("missing: " + clazz);
    //            /*
    //            final Set references = clazz.getReferences();
    //            for (final Iterator j = references.iterator(); j.hasNext();) {
    //                final Clazz ref = (Clazz) j.next();
    //                System.out.println(" ref: " + ref);                
    //            }
    //         */
    //        }
}
