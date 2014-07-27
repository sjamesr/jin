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

import java.awt.image.RGBImageFilter;



/**
 * An <code>RGBImageFilter</code> which, for convenience, decomposes the
 * ARGB int into its components, before passing it to the user.
 */

public abstract class ArgbImageFilter extends RGBImageFilter{
  
  
  
  /**
   * Decomposes the <code>rgb</code> argument into its components and delegates
   * to <code>filterARGB</code>.
   */

  @Override
  public int filterRGB(int x, int y, int rgb){
    int a = (rgb >> 24) & 0xff;
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = rgb & 0xff;
    
    return filterArgb(x, y, a, r, g, b);
  }
  
  
  
  /**
   * Returns the ARGB value to replace the specified pixel.
   * Each of the ARGB components is in the 0-255 range.
   */
  
  public abstract int filterArgb(int x, int y, int a, int r, int g, int b);
  
  
  
  /**
   * Utility method which composes the specified ARGB components into an int.
   * Each of the ARGB components must be in the 0-255 range. The method does not
   * validate this.
   */
  
  public static int compose(int a, int r, int g, int b){
    return (a << 24) | (r << 16) | (g << 8) | b;
  }
  
  
  
}
