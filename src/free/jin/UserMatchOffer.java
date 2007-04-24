/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

import free.chess.Player;
import free.chess.WildVariant;
import free.util.Struct;



/**
 * Encapsulates the details of a match offer made by the user.
 */

public class UserMatchOffer extends Struct{
  
  
  
  /**
   * Creates a new <code>UserMatchOffer</code> with the specified arguments.
   * 
   * @param opponent The matched player.
   * @param time The time of the proposed game, in minutes.
   * @param increment The increment of the proposed game, in seconds.
   * @param isRated Whether the proposed game is rated.
   * @param variant The wild variant of the proposed game.
   * @param color The color the user wants to play with, or <code>null</code> 
   * for automatic color assignment.
   */
  
  public UserMatchOffer(ServerUser opponent, int time, int increment, boolean isRated, WildVariant variant, Player color){
    if (opponent == null)
      throw new IllegalArgumentException("opponent may not be null");
    if (time < 0)
      throw new IllegalArgumentException("time may not be negative");
    if (increment < 0)
      throw new IllegalArgumentException("increment may not be negative");
    if (variant == null)
      throw new IllegalArgumentException("variant may not be null");
    
    setProperty("Opponent", opponent);
    setIntegerProperty("Time", time);
    setIntegerProperty("Increment", increment);
    setBooleanProperty("IsRated", isRated);
    setProperty("Variant", variant);
    setProperty("Color", color);
  }
  
  
  
  /**
   * Returns the matched player.
   */
  
  public ServerUser getOpponent(){
    return (ServerUser)getProperty("Opponent");
  }
  
  
  
  /**
   * Returns the time of the proposed game, in minutes.
   */
  
  public int getTime(){
    return getIntegerProperty("Time");
  }
  
  
  
  /**
   * Returns the increment of the proposed game, in seconds.
   */
  
  public int getIncrement(){
    return getIntegerProperty("Increment");
  }
  
  
  
  /**
   * Returns whether the proposed game is rated.
   */
  
  public boolean isRated(){
    return getBooleanProperty("IsRated");
  }
  
  
  
  /**
   * Returns the variant of the proposed game.
   */
  
  public WildVariant getVariant(){
    return (WildVariant)getProperty("Variant");
  }
  
  
  
  /**
   * Returns the color with which the user wishes to play, or <code>null</code>
   * for automatic color assignment.
   */
  
  public Player getColor(){
    return (Player)getProperty("Color");
  }
  
  
  
}
