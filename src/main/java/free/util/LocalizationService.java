/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2008 Alexander Maryanovsky. All rights reserved.
 *
 * The utillib library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util;

/**
 * Provides localization services to the <code>free.util</code> package.
 */
class LocalizationService {

  /**
   * The template <code>Localization</code> for the entire package.
   */
  private static Localization templateLocalization = null;

  /**
   * Returns the <code>free.util.Localization</code> localization for the specified class.
   */
  public static synchronized Localization getForClass(Class c) {
    // Lazily initialize
    if (templateLocalization == null)
      templateLocalization = Localization.load(LocalizationService.class);

    return templateLocalization.getForClass(c);
  }
}
