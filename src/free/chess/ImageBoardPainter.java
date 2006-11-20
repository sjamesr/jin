/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import free.util.IOUtilities;


/**
 * This class is provided for backwards compatibility with uses as a
 * <code>ResourceBoardPainter</code>. The actual drawing is now done by either
 * {@link BoardImageBoardPainter} or {@link SquareImagesBoardPainter}.
 */

public final class ImageBoardPainter implements ResourceBoardPainter{
  
  
  
  /**
   * The delegate that does the actual drawing for us.
   */
  
  private ResourceBoardPainter delegate;
  
  
  
  /**
   * Since <code>ImageBoardPainter</code>s are immutable, simply returns
   * <code>this</code>.
   */
  
  public BoardPainter freshInstance(){
    return this;
  }




  /**
   * Loads the board images from the specified URL. The structure at the
   * specified url is described below.
   * A properties file named "definition" must be located at the base URL.
   * That file should contain the two property:
   * <ul>
   *   <li><code>type</code>: The value is either "single" or "light-dark". This
   *       specifies whether there is a single image of the entire board or two
   *       pattern images for the light and dark squares. If this is omitted,
   *       "light-dark" is assumed.
   * </ul>
   * Based on the value of this property, the actual drawing is done by either
   * a {@link BoardImageBoardPainter} or a {@link SquareImagesBoardPainter}.
   */
   
  public void load(URL url) throws IOException{
    if (delegate != null)
      throw new IllegalStateException("This ImageBoardPainter has already been loaded");
    
    URL defURL = new URL(url, "definition");

    Properties def = IOUtilities.loadProperties(defURL, true);
    if (def == null)
      def = new Properties();

    String type = def.getProperty("type", "light-dark");
    if ("single".equals(type))
      delegate = new BoardImageBoardPainter();
    else if ("light-dark".equals(type))
      delegate = new SquareImagesBoardPainter();
    else
      throw new IOException("Unrecognized type value: " + type);
    delegate.load(url);
  }



  /**
   * Merely delegates the drawing to the actual board painter.
   */

  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height){
    delegate.paintBoard(g, component, x, y, width, height);
  } 

  

}
