/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;


/**
 * A class loader with a parent, to which loading is delegated if the class or
 * resource can't be found. This class isn't needed with JDK1.2 which already
 * has this functionality. 
 */

public abstract class ChildClassLoader extends ClassLoader{



  /**
   * The parent classloader. May be null.
   */

  private final ChildClassLoader parent;



  /**
   * Creates a new <code>ChildClassLoader</code> with the specified parent. The
   * parent may be <code>null</code>.
   */

  public ChildClassLoader(ChildClassLoader parent){
    this.parent = parent;
  }



  /**
   * Creates a new <code>ChildClassLoader</code> with no parent.
   */

  public ChildClassLoader(){
    this(null);
  }



  /**
   * Returns the parent classloader.
   */

  public ChildClassLoader getParentClassLoader(){
    return parent;
  }



  /**
   * Loads and optionally resolves the class with the specified name. If this
   * classloader can't find the specified class, the parent is asked to load it.
   */

  public final synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException{
    
    // Already loaded? 
    Class c = findLoadedClass(name);

    // System class?
    if (c == null){    
      try{
        c = findSystemClass(name);
      } catch (ClassNotFoundException e){}
    }

    // Our class?
    if (c == null){
      try{
        byte [] classData = loadClassData(name);
        if (classData != null)
          c = defineClass(name, classData, 0, classData.length);
      } catch (IOException e){}
    }

    // Our class indeed - resolve if necessary and return 
    if (c != null){
      if (resolve)
        resolveClass(c);
      return c;
    }

    // Delegate to parent, if there is one.
    if (parent == null)
      throw new ClassNotFoundException(name);
    else
      c = parent.loadClass(name, resolve);
    
    return c;
  }



  /**
   * Returns an <code>InputStream</code> for reading the resource with the
   * specified name. If this classloader can't find the resource, the parent
   * is asked to find it.
   */

  public final InputStream getResourceAsStream(String name){
    InputStream in = getResourceAsStreamImpl(name);
    return in != null ? in : (parent == null ? null : parent.getResourceAsStream(name));
  }



  /**
   * Returns a <code>URL</code> pointing to the resource with the specified
   * name. If this classloader can't find the resource, the parent is asked to
   * find it.
   */

  public final URL getResource(String name){
    URL url = getResourceImpl(name);
    return url != null ? url : (parent == null ? null : parent.getResource(name));
  }



  /**
   * Returns (usually by loading from the appropriate location) the byte array
   * defining the class with the specific name. The default implementation uses
   * the getResourceAsStreamImpl method to load that data.
   */

  protected byte [] loadClassData(String name) throws IOException{
    String resourceName = name.replace('.', '/') + ".class";
    InputStream in = getResourceAsStreamImpl(resourceName);
    if (in == null)
      throw new IOException(name + " not found");
    return IOUtilities.readToEnd(in);
  }




  /**
   * Returns an <code>InputStream</code> for reading the resource with the
   * specified name.
   */

  protected abstract InputStream getResourceAsStreamImpl(String name);



  /**
   * Returns a <code>URL</code> pointing to the resource with the specified
   * name.
   */

  protected abstract URL getResourceImpl(String name);



}
