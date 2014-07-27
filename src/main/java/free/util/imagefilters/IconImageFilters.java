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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;




/**
 * Provides a set of filters used to obtain variations of icon images for
 * buttons, such as disabled, rollover, selected etc.
 */

public class IconImageFilters{
  
  
  
  /**
   * The filter that creates a disabled version of an image.
   */
  
  public static final ImageFilter DISABLED = new OpaquenessImageFilter(0.5f);
  
  
  
  /**
   * The filter that creates a rollover version of an image.
   */
  
  public static final ImageFilter ROLLOVER = new BrightnessImageFilter(0.9f);
  
  
  
  /**
   * The filter that creates a pressed version of an image.
   */
  
  public static final ImageFilter PRESSED = new BrightnessImageFilter(0.8f);
  
  
  
  /**
   * Returns a disabled version of the specified image.
   */
  
  public static Image getDisabled(Image image){
    return filter(image, DISABLED);
  }
  
  
  
  /**
   * Returns a rollover version of the specified image.
   */
  
  public static Image getRollover(Image image){
    return filter(image, ROLLOVER);
  }
  
  
  
  /**
   * Returns a pressed version of the specified image.
   */
  
  public static Image getPressed(Image image){
    return filter(image, PRESSED);
  }
  
  
  
  /**
   * Returns an image filtered using the specified <code>ImageFilter</code>.
   */
  
  private static Image filter(Image image, ImageFilter filter){
    ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(producer);
  }
  
  
  
}
