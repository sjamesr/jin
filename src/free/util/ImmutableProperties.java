/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import java.util.Properties;
import java.util.Enumeration;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStream;


/**
 * An extension of java.util.Properties which acts as a wrapper to another
 * Properties object and does not allow modifying any 
 * property. All methods that modify the Properties somehow do nothing except 
 * throw an unchecked exception.
 *
 * @see java.util.Properties
 */

public class ImmutableProperties extends Properties{


  /**
   * The wrapped Properties.
   */

  private final Properties props;




  /**
   * Creates a new ImmutableProperties object which will wrap the given
   * Properties.
   */

  public ImmutableProperties(Properties props){
    this.props = props;
  }



  /**
   * Returns the value of the property with the given name or null if no such
   * property exists.
   */

  public String getProperty(String propertyName){
    return props.getProperty(propertyName);
  }




  /**
   * Returns the value of the property with the given name, or the given default
   * value if no such property exists.
   */

  public String getProperty(String propertyName, String defaultValue){
    return props.getProperty(propertyName, defaultValue);
  }




  /**
   * Prints this property list out to the specified PrintStream.
   * This method is useful for debugging. 
   */

  public void list(PrintStream out){
    props.list(out);
  }




  /**
   * Prints this property list out to the specified PrintWriter.
   * This method is useful for debugging. 
   */

  public void list(PrintWriter out){
    props.list(out);
  }




  /**
   * Returns an enumeration of all the keys in this property list, including 
   * the keys in the default property list.
   */

  public Enumeration propertyNames(){
    return props.propertyNames();
  }





  /**
   * Stores this property list to the specified output stream. The string header
   * is printed as a comment at the beginning of the stream. 
   */

  public void save(OutputStream out, String header){
    props.save(out,header);
  }




  /**
   * Tests if some key maps into the specified value in this hashtable. This 
   * operation is more expensive than the containsKey method. 
   */

  public boolean contains(Object value){
    return props.contains(value);
  }




  /**
   * Tests if the specified object is a key in this hashtable. 
   */

  public boolean containsKey(Object key){
    return props.contains(key);
  }




  /**
   * Returns an enumeration of the values in this hashtable. Use the 
   * Enumeration methods on the returned object to fetch the elements 
   * sequentially. 
   */

  public Enumeration elements(){
    return props.elements();
  }




  /**
   * Returns the value to which the specified key is mapped in this hashtable. 
   */

  public Object get(Object key){
    return props.get(key);
  }




  /**
   * Tests if this hashtable maps no keys to values. 
   */

  public boolean isEmpty(){
    return props.isEmpty();
  }




  /**
   * Returns an enumeration of the keys in this hashtable. 
   */

  public Enumeration keys(){
    return props.keys();
  }





  /**
   * Returns the number of keys in this hashtable. 
   */

  public int size(){
    return props.size();
  }




  /**
   * Returns a rather long string representation of this hashtable. 
   */

  public String toString(){
    return props.toString();
  }




  // ************************************************************************ //
  // ******* Modifying methods - throw UnsupportedOperationException ******** //
  // ************************************************************************ //




  /**
   * Throws an UnsupportedOperationException.
   */

  public void load(InputStream in){
    throw new UnsupportedOperationException("Attempt to modify ImmutableProperties");
  }



  /**
   * Throws an UnsupportedOperationException.
   */

  public Object setProperty(String propertyName, String propertyValue){
    throw new UnsupportedOperationException("Attempt to modify ImmutableProperties");    
  }




  /**
   * Throws an UnsupportedOperationException.
   */

  public Object put(Object key, Object value){
    throw new UnsupportedOperationException("Attempt to modify ImmutableProperties");    
  }




  /**
   * Throws an UnsupportedOperationException.
   */

  public void clear(){
    throw new UnsupportedOperationException("Attempt to modify ImmutableProperties");    
  }



  /**
   * Throws an UnsupportedOperationException.
   */

  public Object remove(Object key){
    throw new UnsupportedOperationException("Attempt to modify ImmutableProperties");    
  }


}
