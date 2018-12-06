/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
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
package free.util.imagefilters;

/**
 * An image filter which multiplies the alpha (opaqueness) component of each pixel by a specified
 * factor.
 */
public class OpaquenessImageFilter extends ArgbMultiplyingImageFilter {

  /**
   * Creates a new <code>OpaquenessImageFilter</code> which multiplies the alpha component of each
   * pixel by the specified factor. Positive factors multiply the distance from 0 (the intuitive
   * behaviour). Negative factors, on the other hand, multiply the distance from 255. For example,
   * applying a factor of -0.5 to 128, will result in a value of 192. After applying the factor, the
   * result is constrained to the 0-255 range.
   */
  public OpaquenessImageFilter(float factor) {
    super(factor, 1.0f, 1.0f, 1.0f);
  }
}
