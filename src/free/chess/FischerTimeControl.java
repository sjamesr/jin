/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

import free.util.Localization;



/**
 * Implements Fischer time control, where a fixed amount of time is added to a
 * player's clock after each move he makes.
 */

public final class FischerTimeControl extends TimeControl{
  
  
  
  /**
   * The initial time on the clock, in milliseconds.
   */
  
  private final int initial;
  
  
  
  /**
   * The amount of time added to the clock after each move, in milliseconds.
   */
  
  private final int increment;
  
  
  
  /**
   * Creates a new <code>FischerTimeControl</code> with the specified initial
   * time and increment.
   * 
   * @param initial The initial amount of time on the player's clock, in
   * milliseconds.
   * @param increment The amount of time added to the player's clock after each
   * move, in milliseconds.
   */
  
  public FischerTimeControl(int initial, int increment){
    if (initial < 0)
      throw new IllegalArgumentException("Initial time may not be negative");
    if (increment < 0)
      throw new IllegalArgumentException("Increment may not be negative");
    
    this.initial = initial;
    this.increment = increment;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public int getInitialTime(Player player){
    return getInitial();
  }
  
  
  
  /**
   * Returns the initial amount of time on the player's clock, in milliseconds.
   */
  
  public int getInitial(){
    return initial;
  }
  
  
  
  /**
   * Returns the amount of time added to the player's clock after each move.
   */
  
  public int getIncrement(){
    return increment;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public String getLocalizedShortDescription(){
    return getDescription("shortDescription");
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public String getLocalizedMediumDescription(){
    return getDescription("mediumDescription");
  }
  
  
  
  /**
   * {@inheritDoc} 
   */
  
  public String getLocalizedLongDescription(){
    return getDescription("longDescription");
  }
  
  
  
  /**
   * Returns a description of the time control obtained using the specified
   * localization key.
   */
  
  private String getDescription(String key){
    Localization l10n = LocalizationService.getForClass(FischerTimeControl.class);
    int initialMinutes = getInitial()/(1000*60);
    int incrementSeconds = getIncrement()/1000;
    Object [] args = new Object[]{String.valueOf(initialMinutes), String.valueOf(incrementSeconds)};
    
    return l10n.getFormattedString(key, args);
  }
  
  
  
  /**
   * Returns whether the specified object equals to this one.
   */
  
  public boolean equals(Object o){
    if (!(o instanceof FischerTimeControl) || (o == null))
      return false;
    
    FischerTimeControl tc = (FischerTimeControl)o;
    return (tc.initial == initial) && (tc.increment == increment);
  }
  
  
  
  /**
   * Returns the hash code for this object.
   */
  
  public int hashCode(){
    return initial^increment;
  }
  
  
  
}
