/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA
 */

package free.util;

import java.util.Hashtable;


/**
 * A collection of general utility methods.
 */

public class Utilities{



  /**
   * Returns <code>true</code> if the two specified objects are the same.
   * Returns <code>false</code> otherwise. To be considered the same, the two
   * references must either both be null or invoking <code>equals</code> on one
   * of them with the other must return <code>true</code>.
   */

  public static boolean areEqual(Object obj1, Object obj2){
    return obj1 == null ? obj2 == null : obj1.equals(obj2);
  }




  /**
   * Maps the specified key to the specified value in the specified
   * <code>Hashtable</code>. If the specified value is <code>null</code> any
   * existing mapping of the specified key is removed from the
   * <code>Hashtable</code>. The old value mapped to the specified key
   * is returned, or <code>null</code> if no value was mapped to the key.
   */

  public static Object put(Hashtable table, Object key, Object value){
    return value == null ? table.remove(key) : table.put(key, value);
  }



}