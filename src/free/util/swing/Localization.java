/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
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

package free.util.swing;

import java.util.Locale;
import java.util.ResourceBundle;



/**
 * Provides localization services to the <code>free.util.swing</code> package.
 */

public class Localization{
  
  
  
  /**
   * The name of the resource bundle.
   */
  
  private static final String BUNDLE_NAME = "free.util.swing.localization"; //$NON-NLS-1$
  
  
  
  /**
   * The resource bundle.
   */

  private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
  
  
  
  /**
   * Sets the locale to be used by the <code>free.util.swing</code> package.
   * If this method is not called, the default locale is used. 
   */
  
  public static void setLocale(Locale locale){
    resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }
  
  
  
  /**
   * Returns the localization for the following key.
   */

  public static String getString(String key){
    return resourceBundle.getString(key);
  }
  
  
  
}
