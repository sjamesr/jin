/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import javax.swing.JComponent;


/**
 * The abstract superclass of all components who display a chess clock (one part
 * of it).
 */

public abstract class AbstractChessClock extends JComponent{


  /**
   * The code for the mode where only hours and minutes are displayed. 
   */
   
  public static final int HOUR_MINUTE_DISPLAY_MODE = 0;
  
  
  
  /**
   * The code for the mode where minutes and seconds are displayed.
   */
   
  public static final int MINUTE_SECOND_DISPLAY_MODE = 1;
  
  
  
  /**
   * The code for the mode where minutes, seconds and second tenths are
   * displayed.
   */
   
  public static final int SECOND_TENTHS_DISPLAY_MODE = 2;

   
   
  /**
   * The time displayed by this clock, in milliseconds.
   */

  private int time;
  
  

  /**
   * The current display mode of this clock. Possible values are
   * {@link #HOUR_MINUTE_DISPLAY_MODE}, {@link #MINUTE_SECOND_DISPLAY_MODE) and
   * {@link #SECOND_TENTHS_DISPLAY_MODE}.
   */

  private int displayMode = MINUTE_SECOND_DISPLAY_MODE;



  /**
   * Is the clock currently active (the current turn belongs to the owner of
   * this clock).
   */

  private boolean isActive = false;



  /**
   * Creates a new <code>AbstractChessClock</code> with the given initial amount
   * of time (in milliseconds) on it.
   */

  public AbstractChessClock(int time){
    this.time = time;
  }




  /**
   * Sets the time displayed by this clock, in milliseconds.
   */

  public void setTime(int time){
    this.time = time;
    repaint();
  }




  /**
   * Returns the time displayed by this clock, in milliseconds.
   */

  public int getTime(){
    return time;
  }



  /**
   * Sets the display mode of this clock. Possible values are
   * {@link #HOUR_MINUTE_DISPLAY_MODE}, {@link #MINUTE_SECOND_DISPLAY_MODE) and
   * {@link #SECOND_TENTHS_DISPLAY_MODE}. Note that concrete implementations may
   * ignore this setting.
   */

  public void setDisplayMode(int displayMode){
    switch (displayMode){
      case HOUR_MINUTE_DISPLAY_MODE:
      case MINUTE_SECOND_DISPLAY_MODE:
      case SECOND_TENTHS_DISPLAY_MODE:
        break;
      default:
        throw new IllegalArgumentException("Unrecognized display mode value: " + displayMode);
    }
    
    this.displayMode = displayMode;
    repaint();
  }
  
  

  /**
   * Returns the current display mode of the clock. Possible values are
   * {@link #HOUR_MINUTE_DISPLAY_MODE}, {@link #MINUTE_SECOND_DISPLAY_MODE) and
   * {@link #SECOND_TENTHS_DISPLAY_MODE}.
   */

  public int getDisplayMode(){
    return displayMode;
  }

  

  /**
   * Sets the active flag of this clock. The clock should be set to be active
   * when its owner is also the owner of the current turn.
   */

  public void setActive(boolean isActive){
    this.isActive = isActive;
    repaint();
  }



  /**
   * Returns whether this clock is active.
   */

  public boolean isActive(){
    return isActive;
  }

  

}
