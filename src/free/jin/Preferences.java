/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin;

import java.awt.Color;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import free.util.BeanProperties;
import free.util.EventListenerList;
import free.util.FilteringEnumeration;
import free.util.FormatException;
import free.util.IOUtilities;
import free.util.MappingEnumeration;
import free.util.RectDouble;
import free.util.StringEncoder;
import free.util.StringParser;


/**
 * A class representing the preferences of a user. This is an abstract class,
 * but there are a few static method returning instances of specific
 * implementations. Note that preference names must be unique across preference
 * types, that is, you can't have two preference with the same name but
 * different types (the typing is just done for your convenience).
 */

public abstract class Preferences{



  /**
   * Registers the specified <code>PropertyChangeListener</code> to be notified
   * whenever a preference changes.
   */

  public abstract void addChangeListener(PropertyChangeListener listener);



  /**
   * Unregisters the specified <code>PropertyChangeListener</code>.
   */

  public abstract void removeChangeListener(PropertyChangeListener listener);



  /**
   * Returns an <code>Enumeration</code> of the preference names in this
   * <code>Preference</code> object.
   */

  public abstract Enumeration getPreferenceNames();



  /**
   * Returns whether the preference with the specified name exists.
   */

  public boolean hasPreference(String prefName){
    return get(prefName, null) != null;
  }



  /**
   * Sets the value of the specified preference. The value's type must be one of
   * the supported types, or <code>null</code>, with primitive types wrapped in
   * their usual wrapper classes. Setting the preference value to
   * <code>null</code> is equivalent to removing the preference. If the current
   * value of this preference is already the specified value (compared via
   * <code>equals</code>, for non <code>null</code> values), the call is ignored
   * (no firing property change events for example).
   */

  public abstract void set(String prefName, Object prefValue);



  /**
   * Returns the value of the specified preference. The type of the return value
   * will depend on the type of the preference, with primitive types wrapped in
   * their usual wrapper types. If there is no preference with the specified
   * name, the specified default value is returned.
   */

  public abstract Object get(String prefName, Object defaultValue);



  /**
   * Returns the value of the specified preference. The type of the return value
   * will depend on the type of the preference, with primitive types wrapped in
   * their usual wrapper types. If there is no preference with the specified
   * name, a <code>NoSuchElementException</code> is thrown.
   */

  public Object get(String prefName){
    Object result = get(prefName, null);
    if (result == null)
      throw new NoSuchElementException(prefName);

    return result;
  }

  
  
  /**
   * Removes the preference with the specified name.
   */

  public void remove(String prefName){
    set(prefName, null);
  }



  /**
   * Looks up the specified preference by repeatedly truncating the last token
   * (the delimiter is a period character - <code>'.'</code>) of the specified
   * name until a preference with that exact name is found.
   * For example, if the the specified preference is <code>"foo.bar.moo"</code>,
   * first an attempt to find a preference with the name
   * <code>"foo.bar.moo"</code> is made, then if such a preference does not
   * exist, a preference with the name <code>"foo.bar"</code> is searched and
   * finally if no preference with that name is found either, the preference
   * with the name <code>"foo"</code> is searched. If it exists, its value is
   * returned, otherwise the specified default value is returned.
   */

  public Object lookup(String pref, Object defaultValue){
    if (hasPreference(pref))
      return get(pref);

    int dotIndex = pref.lastIndexOf('.');
    if (dotIndex == -1)
      return defaultValue;
    else
      return lookup(pref.substring(0, dotIndex), defaultValue);
  }



  /**
   * Looks up a preference in the same manner as
   * {@link #lookup(String, char, Object)}, but instead throws a
   * NoSuchElementException if the lookup process yields no results.
   */

  public Object lookup(String pref){
    Object val = lookup(pref, null);
    if (val == null)
      throw new NoSuchElementException(pref);

    return val;
  }



  /**
   * Returns the boolean preference with the specified name or the specified
   * default value if no such preference exists.
   */

  public boolean getBool(String prefName, boolean defaultValue){
    return ((Boolean)get(prefName, defaultValue ? Boolean.TRUE : Boolean.FALSE)).booleanValue();
  }



  /**
   * Returns the boolean preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public boolean getBool(String prefName){
    return ((Boolean)get(prefName)).booleanValue();
  }



  /**
   * Returns the integer preference with the specified name or the specified
   * default value if no such preference exists.
   */

  public int getInt(String prefName, int defaultValue){
    return ((Integer)get(prefName, new Integer(defaultValue))).intValue();
  }



  /**
   * Returns the integer preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public int getInt(String prefName){
    return ((Integer)get(prefName)).intValue();
  }



  /**
   * Returns the double preference with the specified name or the specified
   * default value if no such preference exists.
   */

  public double getDouble(String prefName, double defaultValue){
    return ((Double)get(prefName, new Double(defaultValue))).doubleValue();
  }



  /**
   * Returns the double preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public double getDouble(String prefName){
    return ((Double)get(prefName)).doubleValue();
  }



  /**
   * Returns the string preference with the specified name or the specified
   * default value if no such preference exists.
   */

  public String getString(String prefName, String defaultValue){
    return (String)get(prefName, defaultValue);
  }



  /**
   * Returns the string preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public String getString(String prefName){
    return (String)get(prefName);
  }



  /**
   * Returns the integer-list preference with the specified name or the
   * specified default value if no such preference exists.
   */

  public int [] getIntList(String prefName, int [] defaultValue){
    return (int [])get(prefName, defaultValue);
  }



  /**
   * Returns the integer-list preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public int [] getIntList(String prefName){
    return (int [])get(prefName);
  }



  /**
   * Returns the color preference with the specified name or the specified
   * default value if no such preference exists.
   */

  public Color getColor(String prefName, Color defaultValue){
    return (Color)get(prefName, defaultValue);
  }



  /**
   * Returns the color preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public Color getColor(String prefName){
    return (Color)get(prefName);
  }



  /**
   * Returns the rectangle preference with the specified name or the specified
   * default value if no such preference exists.
   */

  public Rectangle getRect(String prefName, Rectangle defaultValue){
    return (Rectangle)get(prefName, defaultValue);
  }



  /**
   * Returns the rectangle preference with the specified name. Throws a
   * <code>NoSuchElementException</code> if there is no preference with the
   * specified name.
   */

  public Rectangle getRect(String prefName){
    return (Rectangle)get(prefName);
  }



  /**
   * Returns the <code>RectDouble</code> preference with the specified name or
   * the specified default value if no such preference exists.
   */

  public RectDouble getRectDouble(String prefName, RectDouble defaultValue){
    return (RectDouble)get(prefName, defaultValue);
  }



  /**
   * Returns the <code>RectDouble</code> preference with the specified name.
   * Throws a <code>NoSuchElementException</code> if there is no preference with
   * the specified name.
   */

  public RectDouble getRectDouble(String prefName){
    return (RectDouble)get(prefName);
  }



  /**
   * Sets the value of the specified boolean preference.
   */

  public void setBool(String prefName, boolean prefValue){
    set(prefName, prefValue ? Boolean.TRUE : Boolean.FALSE);
  }



  /**
   * Sets the value of the specified integer preference.
   */

  public void setInt(String prefName, int prefValue){
    set(prefName, new Integer(prefValue));
  }



  /**
   * Sets the value of the specified double preference.
   */

  public void setDouble(String prefName, double prefValue){
    set(prefName, new Double(prefValue));
  }



  /**
   * Sets the value of the specified string preference.
   */

  public void setString(String prefName, String prefValue){
    set(prefName, prefValue);
  }



  /**
   * Sets the value of the specified integer list preference.
   */

  public void setIntList(String prefName, int [] prefValue){
    set(prefName, prefValue);
  }



  /**
   * Sets the value of the specified color preference.
   */

  public void setColor(String prefName, Color prefValue){
    set(prefName, prefValue);
  }



  /**
   * Sets the value of the specified rectangle preference.
   */

  public void setRect(String prefName, Rectangle prefValue){
    set(prefName, prefValue);
  }



  /**
   * Sets the value of the specified <code>RectDouble</code> preference.
   */

  public void setRectDouble(String prefName, RectDouble prefValue){
    set(prefName, prefValue);
  }



  /**
   * Writes this <code>Preferences</code> object into the specified file.
   */

  public void save(File file) throws IOException{
    OutputStream out = new FileOutputStream(file);
    try{
      save(out);
    } finally{
        out.close();
      }
  }



  /**
   * Writes this <code>Preferences</code> object into the specified output
   * stream. The format is as documented in the <code>load(InputStream)</code>
   * method.
   */

  public void save(OutputStream out) throws IOException{
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

    Enumeration prefNames = getPreferenceNames();
    while (prefNames.hasMoreElements()){
      String prefName = (String)prefNames.nextElement();
      Object prefValue = get(prefName);
      
      writer.write(prefName);
      writer.write("=");
      writer.write(encodePreference(prefValue));
      writer.write("\n");
    }

    writer.flush();
  }



  /**
   * Creates a new, initially empty <code>Preferences</code> object.
   */

  public static Preferences createNew(){
    return new BeanPreferences();
  }



  /**
   * Loads preferences from the specified file.
   *
   * @param file The file from which to load the preferences.
   *
   * @see {@link #load(InputStream)}
   */

  public static Preferences load(File file) throws IOException{
    InputStream in = new FileInputStream(file);
    try{
      return Preferences.load(in);
    } finally{
        in.close();
      }
  }



  /**
   * Reads preferences from the specified input stream. The format is:
   * <ul>
   *   <li> Each line is either an empty line, a comment line or a preference
   *        line.
   *   <li> An empty line is a line consisting entirely of whitespace
   *        characters.
   *   <li> A command line is a line whose first character is '#'.
   *   <li> A preference line is a line in the format
   *        <code>[whitespace]preferenceName[whitespace]=[whitespace]preferenceType;preferenceValue[whitespace]</code>.
   *   <li> The allowed preference types and their corresponding allowed values
   *        are:
   *     <ul>
   *       <li> <code>boolean</code> - Either <code>true</code> or
   *            <code>false</code>.
   *       <li> <code>integer</code> - An integer in standard notation, fitting
   *            into a 32-bit signed int.
   *       <li> <code>double</code> - A fractional number, in standard notation.
   *       <li> <code>string</code> - A string, with escaped \n, \t, \r and \\.
   *       <li> <code>intlist</code> - A list of integers in standard notation,
   *            separated by spaces (each must fit into a 32-bit signed int).
   *       <li> <code>color</code> - An integer in hexadecimal notation in the
   *            range [0..0xffffff], specifying the color components in RGB
   *            format.
   *       <li> <code>rect.int</code> - A string in the format
   *            <code>x;y;width;height</code> where the values are specified as
   *            integers in standard notation.
   *       <li> <code>rect.double</code> - A string in the format
   *            <code>x;y;width;height</code> where the values are specified as
   *            fractions and usually mean a part of some bigger rectangle.
   *     </ul>
   *     The preference type, along with the ';' character may be omitted, in
   *     which case, the preference is assumed to be a string, and it is not
   *     unescaped.  
   * </ul>
   * Note: This method does what it does in a somewhat unusual manner for
   *       performance reasons, with the main goal being to avoid allocating
   *       too many <code>String</code> objects.  
   */

  public static Preferences load(InputStream in) throws IOException{
    BeanPreferences prefs = new BeanPreferences();

    String text = IOUtilities.loadText(in); 
    StringTokenizer tokenizer = new StringTokenizer(text, "\n");
    
    // parse and map the preferences
    while (tokenizer.hasMoreTokens()){
      String line = tokenizer.nextToken();
      
      if (line.startsWith("#")) // Comment line
        continue;
      
      int lineLength = line.length();

      boolean isLineEmpty = true;
      for (int i = 0; i < lineLength; i++)
        if (line.charAt(i) > ' '){
          isLineEmpty = false;
        }
      if (isLineEmpty)
        continue;

      int eqIndex = line.indexOf("=");
      if (eqIndex == -1)
        throw new FormatException("No \'=\' found on line: " + line);
      
      int nameEndIndex = eqIndex - 1;
      while ((nameEndIndex >= 0) && (line.charAt(nameEndIndex) <= ' '))
        nameEndIndex--;
      
      int valStartIndex = eqIndex + 1;
      while ((nameEndIndex < lineLength) && (line.charAt(valStartIndex) <= ' '))
        valStartIndex++;

      String prefName = line.substring(0, nameEndIndex + 1).trim();
      String prefValue = line.substring(valStartIndex).trim();
      prefs.set(prefName, parsePreference(prefValue));
    }

    return prefs;
  }
  
  

  /**
   * Returns a wrapper <code>Preferences</code> instance with two delegates -
   * main and default. Whenever a preference value is asked, first
   * the main delegate is consulted; if it doesn't define the specified
   * preference, the preference value of the default delegate is returned. When
   * setting a preference, it is always passed to the main delegate. Saving the
   * preferences object causes only the main delegate to be written.
   */

  public static Preferences createBackedUp(Preferences mainPrefs, Preferences defaultPrefs){
    return new BackedUpPreferences(mainPrefs, defaultPrefs);
  }



  /**
   * Creates a new wrapping Preferences object with the specified
   * <code>delegate</code> and <code>name</code>. All <code>getPreference</code>
   * and <code>setPreference</code> calls will be delegated to
   * <code>delegate</code> with <code>name</code> prepended to the preference
   * name.
   */

  public static Preferences createWrapped(Preferences delegate, String name){
    return new WrapperPreferences(delegate, name);
  }



  /**
   * Parses the specified property value and returns a corresponding preference
   * object.
   * 
   * Note: This method does what it does in a somewhat unusual manner for
   *       performance reasons, with the main goal being to avoid allocating
   *       too many <code>String</code> objects.  
   *
   * @see #encodePreference(Object)
   * @see #loadPreferences(InputStream, Preferences)
   */

  private static Object parsePreference(String s){
    if (s.startsWith("boolean;"))
      return Boolean.valueOf(s.substring("boolean;".length()));
    else if (s.startsWith("integer;"))
      return new Integer(s.substring("integer;".length()));
    else if (s.startsWith("double;"))
      return new Double(s.substring("double;".length()));
    else if (s.startsWith("string;"))
      return StringParser.parseString(s.substring("string;".length()));
    else if (s.startsWith("intlist;"))
      return StringParser.parseIntList(s.substring("intlist;".length()));
    else if (s.startsWith("color;"))
      return StringParser.parseColor(s.substring("color;".length()));
    else if (s.startsWith("rect.int;"))
      return StringParser.parseRectangle(s.substring("rect.int;".length()));
    else if (s.startsWith("rect.double;"))
      return StringParser.parseRectDouble(s.substring("rect.double;".length()));
    else
      return s;
  }



  /**
   * Returns a String encoding the specified preference value.
   *
   * @see #parsePreference(String)
   * @see #savePreferences(Preferences, OutputStream)
   */

  private static String encodePreference(Object prefValue){
    if (prefValue instanceof Boolean)
      return "boolean;" + prefValue;
    else if (prefValue instanceof Integer)
      return "integer;" + prefValue;
    else if (prefValue instanceof Double)
      return "double;" + prefValue;
    else if (prefValue instanceof String)
      return "string;" + StringEncoder.encodeString((String)prefValue);
    else if (prefValue instanceof int [])
      return "intlist;" + StringEncoder.encodeIntList((int [])prefValue);
    else if (prefValue instanceof Color)
      return "color;" + StringEncoder.encodeColor((Color)prefValue);
    else if (prefValue instanceof Rectangle)
      return "rect.int;" + StringEncoder.encodeRectangle((Rectangle)prefValue);
    else if (prefValue instanceof RectDouble)
      return "rect.double;" + StringEncoder.encodeRectDouble((RectDouble)prefValue);
    else
      throw new IllegalArgumentException("Unsupported preference type: " +
        prefValue.getClass().getName());
  }



  /**
   * A straightforward implementation of <code>Preferences</code> via a
   * <code>BeanProperties</code> object.
   */

  private static class BeanPreferences extends Preferences{



    /**
     * The BeanProperties object that keeps our preferences.
     */

    private final BeanProperties props;



    /**
     * Creates a new, initially empty, <code>Preferences</code> object with the
     * specified default preferences.
     */

    public BeanPreferences(){
      this.props = new BeanProperties(this);
    }



    // Preferences implementation follows

    public void set(String prefName, Object prefValue){
      props.setProperty(prefName, prefValue);
    }



    public Object get(String prefName, Object defaultValue){
      return props.getProperty(prefName, defaultValue);
    }



    public void addChangeListener(PropertyChangeListener listener){
      props.addPropertyChangeListener(listener);
    }



    public void removeChangeListener(PropertyChangeListener listener){
      props.removePropertyChangeListener(listener);
    }



    public Enumeration getPreferenceNames(){
      return props.getPropertyNames();
    }



  }



  /**
   * An implementation of a wrapper <code>Preferences</code> class. The actual
   * preferences keeping is delegated to another <code>Preferences</code> object
   * with a specified string prepended to the the preference names.
   */

  private static class WrapperPreferences extends Preferences implements PropertyChangeListener{



    /**
     * The delegate Preferences object.
     */

    private final Preferences delegate;



    /**
     * The prefix we prepend to all property names before passing it to the
     * delegate preferences.
     */

    private final String prefix;



    /**
     * The registered PropertyChangeListeners.
     */

    private final EventListenerList listenerList = new EventListenerList();



    /**
     * Creates a new WrapperPreferences object with the specified delegate
     * Preferences and prefix. 
     */

    public WrapperPreferences(Preferences delegate, String prefix){
      if (delegate == null)
        throw new IllegalArgumentException("The delegate may not be null");
      if (prefix == null)
        throw new IllegalArgumentException("The prefix may not be null - use an empty string instead");

      this.delegate = delegate;
      this.prefix = prefix;
    }



    /**
     * Returns the specified property name with this wrapper preference's prefix
     * string.
     */

    private String prefix(String preferenceName){
      return prefix + preferenceName;
    }



    /**
     * Forwards the event to our own listeners, if necessary.
     */

    public void propertyChange(PropertyChangeEvent evt){
      String propertyName = evt.getPropertyName();
      if (!propertyName.startsWith(prefix)) // Not one of ours
        return;

      propertyName = propertyName.substring(prefix.length());
      PropertyChangeEvent newEvt = new PropertyChangeEvent(this, propertyName, evt.getOldValue(), evt.getNewValue());
      newEvt.setPropagationId(evt.getPropagationId());

      // Fire the event
      Object [] listeners = listenerList.getListenerList();
      for (int i = 0; i < listeners.length; i += 2){
        if (listeners[i] == PropertyChangeListener.class){
          PropertyChangeListener listener = (PropertyChangeListener)listeners[i+1];
          listener.propertyChange(newEvt);
        }
      }
    }



     // Preferences implementation follows

    public void set(String prefName, Object prefValue){
      delegate.set(prefix(prefName), prefValue);
    }



    public Object get(String prefName, Object defaultValue){
      return delegate.get(prefix(prefName), defaultValue);
    }



    public void addChangeListener(PropertyChangeListener listener){
      if (listenerList.getListenerCount(PropertyChangeListener.class) == 0)
        delegate.addChangeListener(this);

      listenerList.add(PropertyChangeListener.class, listener);
    }



    public void removeChangeListener(PropertyChangeListener listener){
      listenerList.remove(PropertyChangeListener.class, listener);

      if (listenerList.getListenerCount(PropertyChangeListener.class) == 0)
        delegate.removeChangeListener(this);
    }



    public Enumeration getPreferenceNames(){
      Enumeration filtering = new FilteringEnumeration(delegate.getPreferenceNames()){
        public boolean accept(Object preferenceName){
          return ((String)preferenceName).startsWith(prefix);
        }
      };

      return new MappingEnumeration(filtering){
        public Object map(Object o){
          String s = (String)o;
          return s.substring(prefix.length());
        }
      };
    }



  }



  /**
   * An implementation of a wrapper <code>Preferences</code> class with two
   * delegates - main and default. See
   * {@link #createBackedUp(Preferences, Preferences)} for more information.
   */

  private static class BackedUpPreferences extends Preferences implements PropertyChangeListener{



    /**
     * The main delegate.
     */

    private final Preferences mainDelegate;



    /**
     * The default delegate.
     */

    private final Preferences defaultDelegate;



    /**
     * The registered PropertyChangeListeners.
     */

    private final EventListenerList listenerList = new EventListenerList();



    /**
     * Creates a new BackedUpPreferences object with the specified delegates.
     */

    public BackedUpPreferences(Preferences mainDelegate, Preferences defaultDelegate){
      if ((mainDelegate == null) || (defaultDelegate == null))
        throw new IllegalArgumentException("Neither of the delegates may be null");

      this.mainDelegate = mainDelegate;
      this.defaultDelegate = defaultDelegate;
    }



    /**
     * Forwards the event to our own listeners, if necessary.
     */

    public void propertyChange(PropertyChangeEvent evt){
      Object source = evt.getSource();
      String propertyName = evt.getPropertyName();
      Object oldValue = evt.getOldValue();
      Object newValue = evt.getNewValue();

      // Check whether the preference changed in the default but the main also
      // has it, so as far as the user is concerned, no change occured.
      if ((source == defaultDelegate) && mainDelegate.hasPreference(propertyName))
        return;

      PropertyChangeEvent newEvt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
      newEvt.setPropagationId(evt.getPropagationId());

      // Fire the event
      Object [] listeners = listenerList.getListenerList();
      for (int i = 0; i < listeners.length; i += 2){
        if (listeners[i] == PropertyChangeListener.class){
          PropertyChangeListener listener = (PropertyChangeListener)listeners[i+1];
          listener.propertyChange(newEvt);
        }
      }
    }



     // Preferences implementation follows

    public void set(String prefName, Object prefValue){
      if (prefValue.equals(get(prefName, null)))
        return;

      mainDelegate.set(prefName, prefValue);
    }



    public Object get(String prefName, Object defaultValue){
      return mainDelegate.get(prefName, defaultDelegate.get(prefName, defaultValue));
    }



    public void addChangeListener(PropertyChangeListener listener){
      if (listenerList.getListenerCount(PropertyChangeListener.class) == 0){
        mainDelegate.addChangeListener(this);
        defaultDelegate.addChangeListener(this);
      }

      listenerList.add(PropertyChangeListener.class, listener);
    }



    public void removeChangeListener(PropertyChangeListener listener){
      listenerList.remove(PropertyChangeListener.class, listener);

      if (listenerList.getListenerCount(PropertyChangeListener.class) == 0){
        mainDelegate.removeChangeListener(this);
        defaultDelegate.removeChangeListener(this);
      }
    }



    public Enumeration getPreferenceNames(){
      Hashtable prefNamesSet = new Hashtable();

      Enumeration prefNames = mainDelegate.getPreferenceNames();
      while (prefNames.hasMoreElements()){
        Object prefName = prefNames.nextElement();
        prefNamesSet.put(prefName, prefName);
      }

      prefNames = defaultDelegate.getPreferenceNames();
      while (prefNames.hasMoreElements()){
        Object prefName = prefNames.nextElement();
        prefNamesSet.put(prefName, prefName);
      }

      return prefNamesSet.keys();
    }



  }



}