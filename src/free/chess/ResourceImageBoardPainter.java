/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

import java.awt.*;
import java.io.IOException;
import java.util.Properties;
import free.util.TextUtilities;
import free.util.ImageUtilities;
import free.util.IOUtilities;


/**
 * An extension of ImageBoardPainter which loads the images by using the
 * Class.getResource() method, allowing you to board patterns along with your
 * application's jar file.
 */

public class ResourceImageBoardPainter extends ImageBoardPainter{
  
  

  /**
   * Simply invokes the superclass's constructor.
   */
  
  private ResourceImageBoardPainter(Image boardImage){
    super(boardImage);
  }


  /**
   * Simply invokes the superclass's constructor.
   */

  private ResourceImageBoardPainter(Image lightImage, Image darkImage, boolean isScaled){
    super(lightImage, darkImage, isScaled);
  }
  
  
  
  /**
   * <P>Creates a new <code>ResourceBoardPiecePainter</code> with images loaded
   * by the given class' class loader from the given relative path (relative to
   * the package name of that class). The <code>ResourceBoardPiecePainter</code>
   * will try to read a properties file named "definition" from the relative
   * path. That file should contain the following two properties:
   * <ul>
   *   <li><code>type</code>: The value is either "single" or "light-dark". This
   *       specifies whether there is a single image of the entire board or two
   *       pattern images for the light and dark squares. If this is omitted,
   *       "light-dark" is assumed.
   *   <li><code>image.type</code>: Specifies the extension (type) of the
   *       image(s) - gif, png, etc. If this is omitted, "gif" is assumed.
   * </ul>
   * If the value of <code>type</code> is "single", the image will be loaded
   * from a file named "board.gif" (assuming <code>image.type</code> is "gif").
   * If the value of <code>type</code> is "light-dark", the images will be
   * loaded from "light.gif" and "dark.gif". In this case, the definition file
   * must contain the boolean property <code>scaleSquares</code>. This property
   * specifies whether the square images specify an exact image of each square
   * or a general pattern to be used for the squares. In effect, if the value
   * of <code>scaleSquares</code> is "true", the light and dark square images
   * will be stretched to fill each square on the board. If it is "false", the
   * images will be sliced and tiled to fill each square. The default value is
   * "false". In the first case all the squares will look the same - in the 2nd
   * they may not look the same if the patterns are bigger than the squares. If
   * all your desired values are the default ones, you may omit the definition
   * file altogether.
   */
   
  public static ImageBoardPainter getInstance(Class c, String relPath) throws IOException{
    return getInstance(c.getClassLoader(), TextUtilities.translateResource(c, relPath));
  }
  
  
  
  /**
   * Creates a new <code>ResourceImageBoardPainter</code> by loading the
   * definition file and images with the specified classloader's
   * <code>getResource</code> method from the specified path. For the layout of
   * the files, see the <code>getInstance(Class, String)</code> method.
   */
   
  public static ImageBoardPainter getInstance(ClassLoader cl, String path) throws IOException{
    if (!(path.endsWith("/") || "".equals(path)))
      path = path + "/";
    String defFile = path + "definition";

    Properties def = IOUtilities.loadProperties(cl.getResourceAsStream(defFile));
    if (def == null)
      def = new Properties();

    String type = def.getProperty("type", "light-dark");     
    String ext = def.getProperty("ext", "gif");
    
    Toolkit toolkit = Toolkit.getDefaultToolkit();    
    
    if ("single".equals(type)){
      Image boardImage = toolkit.getImage(cl.getResource(path + "board." + ext));
      
      ImageUtilities.preload(boardImage);
      
      return new ResourceImageBoardPainter(boardImage);
    }
    else if ("light-dark".equals(type)){
      Image lightImage = toolkit.getImage(cl.getResource(path + "light." + ext));
      Image darkImage = toolkit.getImage(cl.getResource(path + "dark." + ext));

      ImageUtilities.preload(new Image[]{lightImage, darkImage}, null);
      
      boolean isScaled = def.getProperty("scaleSquares", "false").equalsIgnoreCase("true");
      return new ResourceImageBoardPainter(lightImage, darkImage, isScaled);
    }
    else{
      throw new IOException("Unrecognized type value: " + type); 
    }
  }
  
  

}