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

import free.chess.BoardPainter;
import free.chess.DefaultBoardPainter;
import free.chess.ResourceBoardPainter;
import free.jin.Resource;
import free.jin.Server;
import free.jin.plugin.Plugin;
import free.util.IOUtilities;
import free.util.PlatformUtils;
import free.util.TextUtilities;
import free.util.URLClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;


/**
 * A board pattern resource.
 */
 
public class BoardPattern implements Resource{
  
  
  
  /**
   * The default board pattern.
   */
   
  public static final BoardPattern DEFAULT_BOARD_PATTERN;
  
  
  
  /**
   * Creates the default board pattern.
   */
  
  static{
    DEFAULT_BOARD_PATTERN = new BoardPattern();
    DEFAULT_BOARD_PATTERN.name = "Default";
    DEFAULT_BOARD_PATTERN.id = "default";
    DEFAULT_BOARD_PATTERN.boardPainter = new DefaultBoardPainter();
  }
  
  
  
  /**
   * The name of the board pattern.
   */
   
  private String name;
  
  
  
  /**
   * The id of the board pattern.
   */
   
  private String id;
  
  
  
  /**
   * The list of server id's with which this <code>BoardPattern</code> is
   * compatible with, or <code>null</code> if it works everywhere. 
   */
   
  private String [] serverIds;



  /**
   * The board painter.
   */
   
  private BoardPainter boardPainter;
  
  
  
  /**
   * Loads this <code>BoardPattern</code> from the specified URL and for the
   * specified plugin. Returns <code>false</code> if the Java version required
   * by the board pattern is higher than the one we're running in.
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
    
    
    String classname = definition.getProperty("boardPainter.classname");
    
    // Hack to support the old board pack format 
    if (classname == null){
      String resClassname = definition.getProperty("classname");
      if ("ImageBoardLoader".equals(resClassname))
        classname = free.chess.ImageBoardPainter.class.getName();
    }
    
    Class boardPainterClass = null;
    
    try{
      // First try loading it from within the url. 
      ClassLoader classLoader = new URLClassLoader(url);
      boardPainterClass = classLoader.loadClass(classname);
    } catch (SecurityException e){}
      catch (ClassNotFoundException e){}
    
    try{
      if (boardPainterClass == null)
        boardPainterClass = plugin.getClass().getClassLoader().loadClass(classname);
    } catch (ClassNotFoundException e){e.printStackTrace();}
    
    if (boardPainterClass == null)
      throw new IOException("Unable to load board painter class: " + classname);
      
    try{
      boardPainter = (BoardPainter)boardPainterClass.newInstance();
    } catch (InstantiationException e){e.printStackTrace();}
      catch (IllegalAccessException e){e.printStackTrace();}
      
    if (boardPainter == null)
      throw new IOException("Unable to instantiate class " + boardPainterClass);
      
    if (boardPainter instanceof ResourceBoardPainter)
      ((ResourceBoardPainter)boardPainter).load(url);
    
    return true;
  }
  
  
  
  /**
   * Returns the string <code>boards</code>.
   */
   
  public String getType(){
    return "boards";
  }
  
  
  
  /**
   * Returns the name of this board pattern.
   */
   
  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns the id of this board pattern.
   */
   
  public String getId(){
    return id;
  }
  
  
  
  /**
   * Returns whether this <code>BoardPattern</code> is compatible with the
   * specified server.
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
   * Returns a board painter of this piece set.
   */
  
  public BoardPainter getBoardPainter(){
    return boardPainter.freshInstance();
  }

  
  
  /**
   * Returns the name of the board pattern.
   */
  
  public String toString(){
    return getName();
  }
  
  
  
  /**
   * Two <code>BoardPattern</code>s are equal if they have the same id and type.
   */
   
  public boolean equals(Object o){
    if (!(o instanceof BoardPattern))
      return false;
    
    BoardPattern boardPattern = (BoardPattern)o;
    
    return getType().equals(boardPattern.getType()) && getId().equals(boardPattern.getId());
  }
  
  
  
  /**
   * Returns the hashcode of this board pattern.
   */
   
  public int hashCode(){
    return getId().hashCode() ^ getType().hashCode();
  }
  
  

}