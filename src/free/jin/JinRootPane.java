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

package free.jin;

import javax.swing.JRootPane;
import java.awt.Container;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import free.util.swing.AdvancedJDesktopPane;
import free.util.StringParser;


/**
 * Jin's root pane.
 */

public class JinRootPane extends JRootPane{


  /**
   * The JinFrame assosiated with this JinRootPane.
   */

  private final JinFrame jinFrame;


  /**
   * Creates a new JinRootPane which will the the JRootPane for the given JinFrame.
   */

  public JinRootPane(JinFrame jinFrame){
    this.jinFrame = jinFrame;
  }

  
  /**
   * Creates the content pane - a JinContentPane.
   */

  public Container createContentPane(){
    String bgColorString = Jin.getProperty("desktop.background.color");
    Color bgColor = bgColorString==null ? null : StringParser.parseColor(bgColorString);

    String wallpaperFilename = Jin.getProperty("desktop.wallpaper");
    Image wallpaper = wallpaperFilename==null ? null : Toolkit.getDefaultToolkit().getImage(wallpaperFilename);
    if (wallpaper!=null){
      MediaTracker tracker = new MediaTracker(this);
      tracker.addImage(wallpaper,0);
      try{
        tracker.waitForAll();
      } catch (InterruptedException e){
          e.printStackTrace();
        }
    }
    String wallpaperLayoutString = Jin.getProperty("desktop.wallpaper.layout","center");
    int wallpaperLayout = AdvancedJDesktopPane.CENTER;
    if (wallpaperLayoutString.equalsIgnoreCase("stretch"))
      wallpaperLayout = AdvancedJDesktopPane.STRETCH;
    else if (wallpaperLayoutString.equalsIgnoreCase("tile"))
      wallpaperLayout = AdvancedJDesktopPane.TILE;
    else if (wallpaperLayoutString.equalsIgnoreCase("center"))
      wallpaperLayout = AdvancedJDesktopPane.CENTER;


    AdvancedJDesktopPane desktop = new AdvancedJDesktopPane();
    desktop.setBackground(bgColor);
    desktop.setWallpaper(wallpaper);
    desktop.setWallpaperLayoutStyle(wallpaperLayout);

    String dragMode = Jin.getProperty("desktop.dragMode","outline");
    desktop.putClientProperty("JDesktopPane.dragMode", dragMode);

    return desktop;
  }

}
