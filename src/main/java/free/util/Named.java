/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util;

import java.util.Comparator;

/** An interface for classes whose objects have a name. */
public interface Named {

  /** A {@link java.util.Comparator} which sorts <code>Named</code> objects alphabetically. */
  public static final Comparator ALPHABETIC_NAME_COMPARATOR =
      new Comparator() {
        @Override
        public int compare(Object arg0, Object arg1) {
          Named named1 = (Named) arg0;
          Named named2 = (Named) arg1;

          return named1.getName().compareToIgnoreCase(named2.getName());
        }
      };

  /** Returns the name of the object. */
  String getName();
}
