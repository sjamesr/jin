/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.util.swing;

import javax.swing.JDesktopPane;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Dimension;


/**
 * Implements some more features on top of JDesktopPane:
 * <UL>
 *   <LI> You can set a wallpaper image which will be either centered, tiled or
 *        stretched.
 * </UL>
 */

public class AdvancedJDesktopPane extends JDesktopPane{



  /**
   * The code for centering the wallpaper image.
   */

  public static final int CENTER = 1;



  /**
   * The code for tiling the wallpaper image.
   */

  public static final int TILE = 2;



  /**
   * The code for stretching the wallpaper image.
   */

  public static final int STRETCH = 3;


  
  /**
   * The wallpaper image.
   */

  private Image wallpaper;



  /**
   * The layout style of the wallpaper image.
   */

  private int wallpaperLayoutStyle = CENTER;



  /**
   * Sets the wallpaper.
   */

  public void setWallpaper(Image wallpaper){
    this.wallpaper = wallpaper;
    repaint();
  }



  /**
   * Returns the current wallpaper image.
   */

  public Image getWallpaper(){
    return wallpaper;
  }



  /**
   * Sets the wallpaper layout style. Possible values are {@link #CENTER}, 
   * {@link #STRETCH} and {@link #TILE}.
   */

  public void setWallpaperLayoutStyle(int wallpaperLayoutStyle){
    switch(wallpaperLayoutStyle){
      case CENTER:
      case TILE:
      case STRETCH:
        break;
      default:
       throw new IllegalArgumentException("Illegal wallpaper layout style: "+wallpaperLayoutStyle);
    }
    
    this.wallpaperLayoutStyle = wallpaperLayoutStyle;
    repaint();
  }



  /**
   * Returns the current wallpaper layout style.
   */

  public int getWallpaperLayoutStyle(){
    return wallpaperLayoutStyle;
  }



  public void paintComponent(Graphics g){
    Dimension size = this.getSize();

    if (wallpaper!=null){
      if (wallpaperLayoutStyle==STRETCH)
        g.drawImage(wallpaper,0,0,size.width,size.height,this);
      else if (wallpaperLayoutStyle==CENTER){
        int imageWidth = wallpaper.getWidth(this);
        int imageHeight = wallpaper.getHeight(this);
        int x = (size.width-imageWidth)/2;
        int y = (size.height-imageHeight)/2;
        g.drawImage(wallpaper,x,y,this);
      }
      else if (wallpaperLayoutStyle==TILE){
        int imageWidth = wallpaper.getWidth(this);
        int imageHeight = wallpaper.getHeight(this);
        for (int x=0;x<size.width;x+=imageWidth)
          for (int y=0;y<size.height;y+=imageHeight)
            g.drawImage(wallpaper,x,y,this);
      }
    }
  }


}
