/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.util;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.beans.PropertyChangeListener;


/**
 * An advanced implementation of java.util.Properties. It basically adds 2
 * functionalities to it - Merging 2 Properties objects into 1 and property
 * change modification.
 *
 * @see java.util.Properties
 */

public class MergedProperties extends Properties{

  
  /**
   * The default Properties.
   */

  private final Properties defaultProps;



  /**
   * The Properties.
   */

  private final Properties props;




  /**
   * Creates a new Properties with the given Properties and the given default
   * Properties. When a property is looked up, first the regular Properties is
   * searched, and if the property isn't defined there, the default Properties
   * is searched. This constructor does not copy the properties, it uses them
   * directly, so if they change, the state of this object will also change.
   */

  public MergedProperties(Properties props, Properties defaultProps){
    this.props = props;
    this.defaultProps = defaultProps;
  }




  /**
   * Returns the value of the property with the given name.
   */

  public String getProperty(String propertyName){
    String value = props.getProperty(propertyName);
    return value == null ? defaultProps.getProperty(propertyName) : value;
  }




  /**
   * Returns the value of the property with the given name. If no property with
   * the given name exists, the given default value is returned.
   */

  public String getProperty(String propertyName, String defaultValue){
    String value = getProperty(propertyName);
    return value == null ? defaultValue : value;
  }




  /**
   * Sets the given property to have the given value. This modifies the regular
   * Properties, not the default one. Returns the previous value of the property
   * or null if no such property was defined.
   */

  public Object setProperty(String propertyName, String propertyValue){
    return props.put(propertyName, propertyValue);
  }





  /**
   * Overrides Hashtable.get(Object) to make sure that applications that try to access the
   * Hashtable's methods still work. The given key must be a String.
   */

  public Object get(Object key){
    return getProperty((String)key);
  }



  /**
   * Overrides Hashtable.put(Object) to make sure that applications that try to access the
   * Hashtable's methods still work. Both the given key and the given value must
   * be Strings.
   */

  public Object put(Object key, Object value){
    return setProperty((String)key, (String)value);
  }





  /**
   * Returns an enumeration of all the keys in this property list, including the
   * keys in the default property list. 
   */

  public Enumeration propertyNames(){
    Hashtable tempHash = new Hashtable();

    Enumeration defaultEnum = defaultProps.keys();
    while (defaultEnum.hasMoreElements()){
      String key = (String)defaultEnum.nextElement();
      tempHash.put(key,defaultProps.getProperty(key));
    } 

    Enumeration enum = props.keys();
    while (enum.hasMoreElements()){
      String key = (String)enum.nextElement();
      tempHash.put(key,props.getProperty(key));
    } 

    return tempHash.keys();
  }



  /**
   * This method is not currently implemented.
   */

  public void list(java.io.PrintStream out){
    throw new UnsupportedOperationException();
  }




  /**
   * This method is not currently implemented.
   */

  public void list(java.io.PrintWriter out){
    throw new UnsupportedOperationException();
  }



  /**
   * This method is not currently implemented.
   */

  public void load(java.io.InputStream in){
    throw new UnsupportedOperationException();
  }

  


  /**
   * This method is not currently implemented.
   */

  public void save(java.io.OutputStream out){
    throw new UnsupportedOperationException();
  }


}
