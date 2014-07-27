/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2008 Alexander Maryanovsky.
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;



/**
 * Lists and categorizes (some of) the encodings (charsets) available in the
 * JVM. 
 * 
 * @author Maryanovsky Alexander
 */

public class Encodings{
  
  
  
  /**
   * Our localization.
   */
  
  private static final Localization l10n = LocalizationService.getForClass(Encodings.class);
  
  
  
  /**
   * The list of encoding categories.
   */
  
  private static final List CATEGORIES = new LinkedList();
  
  
  
  /**
   * A map from encoding categories to lists of encodings in that category.
   */
  
  private static final Map CATEGORIES_TO_ENCODINGS = new TreeMap();
  
  
  
  /**
   * A map from encoding categories to their names in the current application's
   * locale.
   */
  
  private static final Map CATEGORIES_TO_NAMES = new TreeMap();
  
  
  
  /**
   * A map of i18n
   */
  static{
    try{
      Properties props = IOUtilities.loadProperties(Encodings.class.getResourceAsStream("charsets.properties"));
      for (Iterator categories = props.entrySet().iterator(); categories.hasNext();){
        Map.Entry entry = (Map.Entry)categories.next();
        String categoryKey = (String)entry.getKey();
        String [] aliases = TextUtilities.parseStringList((String)entry.getValue(), ", ");
        List encodings = new LinkedList();
        for (int i = 0; i < aliases.length; i++){
          String alias = aliases[i];
          if (Charset.isSupported(alias))
            encodings.add(Charset.forName(aliases[i]));
        }
        
        CATEGORIES.add(categoryKey);
        CATEGORIES_TO_ENCODINGS.put(categoryKey, encodings);
        CATEGORIES_TO_NAMES.put(categoryKey, l10n.getString(categoryKey + ".name"));
      }
      
      Collections.sort(CATEGORIES, new Comparator(){
        public int compare(Object arg0, Object arg1){
          String c1 = (String)arg0;
          String c2 = (String)arg1;
          
          String n1 = (String)CATEGORIES_TO_NAMES.get(c1);
          String n2 = (String)CATEGORIES_TO_NAMES.get(c2);
          
          return n1.compareToIgnoreCase(n2);
        }
      });

    } catch (IOException e){
        e.printStackTrace();
    }
  }
  
  
  
  /**
   * Returns the list of encoding categories. The list is sorted by the category
   * names in the current application's locale.
   */
  
  public static List categories(){
    return Collections.unmodifiableList(CATEGORIES);
  }
  
  
  
  /**
   * Returns a map from encoding category keys to sets of encodings (and their
   * aliases) in that category. For example, the <code>"western"</code> encoding
   * category could be mapped to the set
   * {"windows-1252", "ISO-8859-1", "US-ASCII"}. 
   */
  
  public static Map categoriesToEncodings(){
    return Collections.unmodifiableMap(CATEGORIES_TO_ENCODINGS);
  }
  
  
  
  /**
   * Returns a map from encoding category keys to their names, in the current
   * application's locale. 
   */
  
  public static Map categoriesToNames(){
    return Collections.unmodifiableMap(CATEGORIES_TO_NAMES);
  }
  
  
  
}
