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

package free.jin;

import free.util.Struct;
import free.chess.WildVariant;
import free.chess.Player;


/**
 * Encapsulates information about a seek sent by the client to the server.
 */

public class UserSeek extends Struct{
  
  
  
  /**
   * Creates a new UserSeek with the specified parameters.
   *
   * @param time The time for the game, in minutes.
   * @param inc The increment, in seconds.
   * @param isRated Is the sought game rated.
   * @param variant The wild variant of the sought game.
   * @param color The color the seeking player wants to play with or
   * <code>null</code> if he has no preference.
   * @param minRating The minimum desired rating for the opponent. Use
   * <code>Integer.MIN_VALUE</code> if you don't want to limit it.
   * @param maxRating The maximum desired rating for the opponent. Use
   * <code>Integer.MAX_VALUE</code> if you don't want to limit it.
   * @param isManualAccept Whether the seeking player wishes to manually confirm
   * that he wants to play against whoever accepts his seek.
   * @param isFormula Whether the seeking player wants accepting players to be
   * filtered through his formula.
   */
  
  public UserSeek(int time, int inc, boolean isRated, WildVariant variant,
      Player color, int minRating, int maxRating, boolean isManualAccept, boolean isFormula){
    
    setIntegerProperty("Time", time);
    setIntegerProperty("Inc", inc);
    setBooleanProperty("IsRated", isRated);
    setProperty("Variant", variant);
    setProperty("Color", color);
    setIntegerProperty("MinRating", minRating);
    setIntegerProperty("MaxRating", maxRating);
    setBooleanProperty("IsManualAccept", isManualAccept);
    setBooleanProperty("IsFormula", isFormula);
  }
  
  
  
  /**
   * Returns the time for the sought game, in minutes.
   */
   
  public int getTime(){
    return getIntegerProperty("Time");
  }
  
  
  
  /**
   * Returns the increment for the sought game, in seconds.
   */
   
  public int getInc(){
    return getIntegerProperty("Inc");
  }
  
  
  
  /**
   * Returns whether the sought game is rated. 
   */
   
  public boolean isRated(){
    return getBooleanProperty("IsRated");
  }
  
  
  
  /**
   * Returns the wild variant of the sought game.
   */
   
  public WildVariant getVariant(){
    return (WildVariant)getProperty("Variant");
  }
  
  
  
  /**
   * Returns the color the seeking player wishes to play with, or
   * <code>null</code> if he has no preference.
   */
   
  public Player getColor(){
    return (Player)getProperty("Color");
  }
  
  
  
  /**
   * Returns the minimum desired rating of the opponent, or
   * <code>Integer.MIN_VALUE</code> if the rating is not limited from below.
   */
   
  public int getMinRating(){
    return getIntegerProperty("MinRating");
  }
   
   

  /**
   * Returns the maximum desired rating of the opponent, or
   * <code>Integer.MAX_VALUE</code> if the rating is not limited from above.
   */
   
  public int getMaxRating(){
    return getIntegerProperty("MaxRating");
  }
  
  
  
  /**
   * Returns whether the seeking player wishes to manually confirm that he wants
   * to play against whoever accepts his seek. 
   */
   
  public boolean isManualAccept(){
    return getBooleanProperty("IsManualAccept");
  }
  
  
  
  /**
   * Returns whether the seeking player wants accepting players to be filtered
   * through his formula. 
   */
   
  public boolean isFormula(){
    return getBooleanProperty("IsFormula");
  }


  
}
