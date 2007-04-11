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

package free.util;

import java.awt.image.RGBImageFilter;



/**
 * An image filter which simply multiplies each RGB component by a specified
 * factor (cropping to the 0-255 range, of course).
 */

public class BrightnessImageFilter extends RGBImageFilter{
  
  
  
  /**
   * The factor by which we multiply the components.
   */
  
  private final float factor;
  
  
  
  /**
   * Creates a new <code>BrightnessImageFilter</code> with the specified factor
   * to multiply the RGB components of an image by.
   */
  
  public BrightnessImageFilter(float factor){
    if (factor < 0)
      throw new IllegalArgumentException("factor may not be negative");
    
    this.factor = factor;
  }
  
  
  
  /**
   * Multiplies the RGB values by the factor and returns the result.
   */
  
  public int filterRGB(int x, int y, int rgb){
    int a = (rgb >> 24) & 0xff;
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = rgb & 0xff;
    
    r = Math.min(255, Math.max(0, (int)(r*factor)));
    g = Math.min(255, Math.max(0, (int)(g*factor)));
    b = Math.min(255, Math.max(0, (int)(b*factor)));
    
    return (a << 24) | (r << 16) | (g << 8) | b;
  }
  
  
  
}
