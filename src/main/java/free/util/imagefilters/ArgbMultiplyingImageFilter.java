/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.util.imagefilters;



/**
 * An image filter which multiplies each of the ARGB components by a specified
 * factor.
 */

public class ArgbMultiplyingImageFilter extends ArgbImageFilter{
  
  
  
  /**
   * The factor for the alpha component.
   */
  
  private final float af;
  
  
  
  /**
   * The factor for the red component.
   */
  
  private final float rf;
  
  
  
  /**
   * The factor for the green component.
   */
  
  private final float gf;
  
  
  
  /**
   * The factor for the blue component.
   */
  
  private final float bf;
  
  
  
  /**
   * Creates a new <code>ArgbMultiplyingImageFilter</code> with the specified
   * factors, by which the corresponding components will be multiplied. Positive
   * factors multiply the distance of the component from 0 (the intuitive
   * behaviour). Negative factors, on the other hand, multiply the distance of
   * the component from 255. For example, applying a factor of -0.5 to a
   * component whose value is 128, will result in a value of 192. After applying
   * the factor, the result is constrained to the 0-255 range.
   * 
   * @param af The factor applied to the alpha (opaqueness) component.
   * @param 
   */
  
  public ArgbMultiplyingImageFilter(float af, float rf, float gf, float bf){
    this.af = af;
    this.rf = rf;
    this.gf = gf;
    this.bf = bf;
  }
  
  
  
  /**
   * Multiplies the components by their respective factors.
   */

  @Override
  public int filterArgb(int x, int y, int a, int r, int g, int b){
    return compose(
        apply(a, af),
        apply(r, rf),
        apply(g, gf),
        apply(b, bf));
  }
  
  
  
  /**
   * Applies the specified factor to the specified value.
   */
  
  private static int apply(int value, float factor){
    if (factor < 0)
      value = (int)(255 + factor*(255 - value));
    else
      value = (int)(factor*value);
    
    return Math.min(255, Math.max(0, value));
  }
  
  
  
}
