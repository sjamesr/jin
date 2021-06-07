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
package free.util.imagefilters;

/**
 * An image filter which simply multiplies all the RGB components of each pixel by a specified
 * factor (cropping to the 0-255 range, of course).
 */
public class BrightnessImageFilter extends ArgbMultiplyingImageFilter {

  /**
   * Creates a new <code>BrightnessImageFilter</code> with the specified factor to multiply the RGB
   * components of an image by. Positive factors multiply the distance of the component from 0 (the
   * intuitive behaviour). Negative factors, on the other hand, multiply the distance of the
   * component from 255. For example, applying a factor of -0.5 to a component whose value is 128,
   * will result in a value of 192. After applying the factor, the result is constrained to the
   * 0-255 range.
   */
  public BrightnessImageFilter(float factor) {
    super(1.0f, factor, factor, factor);
  }
}
