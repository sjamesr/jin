/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.board;

import java.net.URL;
import java.io.IOException;
import java.util.Properties;
import free.jin.Resource;
import free.jin.Server;
import free.jin.plugin.Plugin;
import free.chess.PiecePainter;
import free.chess.ResourcePiecePainter;
import free.chess.DefaultPiecePainter;
import free.util.IOUtilities;
import free.util.PlatformUtils;
import free.util.TextUtilities;
import free.util.URLClassLoader;


/**
 * A piece set resource.
 */

public class PieceSet implements Resource{
  
  
  
  /**
   * The default piece set.
   */
   
  public static final PieceSet DEFAULT_PIECE_SET;
  
  
  
  /**
   * Creates the default piece set.
   */
  
  static{
    DEFAULT_PIECE_SET = new PieceSet();
    DEFAULT_PIECE_SET.name = "Default";
    DEFAULT_PIECE_SET.id = "default";
    DEFAULT_PIECE_SET.piecePainter = new DefaultPiecePainter();
  }
  
  
  
  /**
   * The name of the piece set.
   */
   
  private String name;
  
  
  
  /**
   * The id of the piece set.
   */
   
  private String id;
  
  
  
  /**
   * The list of server id's with which this <code>PieceSet</code> is
   * compatible with, or <code>null</code> if it works everywhere. 
   */
   
  private String [] serverIds;



  /**
   * The piece painter.
   */
   
  private PiecePainter piecePainter;
  
  
  
  /**
   * Loads this <code>PieceSet</code> from the specified URL and for the
   * specified plugin. Returns whether loading finished successfully and this
   * <code>PieceSet</code> can be used for drawing piece sets.
   */
   
  public boolean load(URL url, Plugin plugin) throws IOException{
    Properties definition = IOUtilities.loadProperties(new URL(url, "definition"), true);
    
    String minJavaVer = definition.getProperty("minJavaVersion");
    if ((minJavaVer != null) && !PlatformUtils.isJavaBetterThan(minJavaVer))
      return false;
    
    this.name = definition.getProperty("name");
    this.id = definition.getProperty("id");
    
    String servers = definition.getProperty("servers");
    if (servers != null)
      serverIds = TextUtilities.getTokens(servers, " ");
    
    
    String classname = definition.getProperty("piecePainter.classname");
    
    // Hack to support the old piece pack format 
    if (classname == null){
      String resClassname = definition.getProperty("classname");
      if ("ImagePieceSetLoader".equals(resClassname))
        classname = free.chess.ImagePiecePainter.class.getName();
    }
    
    Class piecePainterClass = null;
    
    try{
      // First try loading it from within the url. 
      ClassLoader classLoader = new URLClassLoader(url);
      piecePainterClass = classLoader.loadClass(classname);
    } catch (SecurityException e){}
      catch (ClassNotFoundException e){}
    
    try{
      if (piecePainterClass == null)
        piecePainterClass = plugin.getClass().getClassLoader().loadClass(classname);
    } catch (ClassNotFoundException e){e.printStackTrace();}
    
    if (piecePainterClass == null)
      throw new IOException("Unable to load piece painter class: " + classname);
      
    try{
      piecePainter = (PiecePainter)piecePainterClass.newInstance();
    } catch (InstantiationException e){e.printStackTrace();}
      catch (IllegalAccessException e){e.printStackTrace();}
      
    if (piecePainter == null)
      throw new IOException("Unable to instantiate class " + piecePainterClass);
      
    if (piecePainter instanceof ResourcePiecePainter)
      ((ResourcePiecePainter)piecePainter).load(url);
    
    return true;
  }
  
  
  
  /**
   * Returns the string <code>pieces</code>.
   */
   
  public String getType(){
    return "pieces";
  }
  
  
  
  
  /**
   * Returns the name of this piece set.
   */
   
  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns the id of this piece set.
   */
   
  public String getId(){
    return id;
  }
  
  
  
  /**
   * Returns whether this <code>PieceSet</code> is compatible with the specified
   * server.
   */
   
  public boolean isCompatibleWith(Server server){
    if (serverIds == null)
      return true;
    
    for (int i = 0; i < serverIds.length; i++)
      if (serverIds[i].equals(server.getId()))
        return true;
    
    return false;
  }
  
  
  
  /**
   * Returns a piece painter of this piece set.
   */
  
  public PiecePainter getPiecePainter(){
    return piecePainter.freshInstance();
  }
  
  
  
  /**
   * Returns the name of the piece set.
   */
  
  public String toString(){
    return getName();
  }


  
  /**
   * Two <code>PieceSet</code>s are equal if they have the same id and type.
   */
   
  public boolean equals(Object o){
    if (!(o instanceof PieceSet))
      return false;
    
    PieceSet pieceSet = (PieceSet)o;
    
    return getType().equals(pieceSet.getType()) && getId().equals(pieceSet.getId());
  }
  
  
  
  /**
   * Returns the hashcode of this piece set.
   */
   
  public int hashCode(){
    return getId().hashCode() ^ getType().hashCode();
  }

  
  
}