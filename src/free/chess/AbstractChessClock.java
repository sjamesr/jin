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
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


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
   * The code for the mode where the actual display depends on the current time.
   */
   
  public static final int TIME_DEPENDENT_DISPLAY_MODE = 3;

   
   
  /**
   * The time that was last set on this clock. Note that if the clock is
   * running, the actual time is calculated by subtracting from this value
   * the amount of time that has passed since the clock started running.
   */

  private int time;
  
  
  
  /**
   * The threshold of time under which the clock displays second tenths, when in
   * <code>TIME_DEPENDENT_DISPLAY_MODE</code> mode.
   */
   
  private int secondTenthsThreshold = 10*1000;
  
  
  
  /**
   * The threshold of time under which the clock displays minutes and seconds,
   * when in <code>TIME_DEPENDENT_DISPLAY_MODE</code> mode.
   */
   
  private int minutesSecondsThreshold = 20*60*1000;
  
  
  
  /**
   * The repaint timer.
   */
   
  private final Timer repaintTimer = new Timer(100, new ActionListener(){
     public void actionPerformed(ActionEvent evt){
       repaint();
     }
  });
  
  
  
  /**
   * The value of the system clock at the time the clock was set to run.
   * -1 if the clock is not running.
   */
   
  private long runStart = -1;
  
  

  /**
   * The current display mode of this clock. Possible values are
   * {@link #HOUR_MINUTE_DISPLAY_MODE}, {@link #MINUTE_SECOND_DISPLAY_MODE) and
   * {@link #SECOND_TENTHS_DISPLAY_MODE}.
   */

  private int displayMode = TIME_DEPENDENT_DISPLAY_MODE;



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
    if (isRunning())
      runStart = System.currentTimeMillis(); 
    repaint();
  }




  /**
   * Returns the time displayed by this clock, in milliseconds.
   */

  public int getTime(){
    if (isRunning())
      return time - (int)(System.currentTimeMillis() - runStart);
    else
      return time;
  }
  
  
  
  /**
   * Returns whether the clock is running.
   */
   
  public boolean isRunning(){
    return runStart >= 0;
  }
  
  
  
  /**
   * Sets the clock's running status.
   */
   
  public void setRunning(boolean isRunning){
    if (isRunning == isRunning())
      return;
    
    if (isRunning){
      runStart = System.currentTimeMillis();
      repaintTimer.start();
    }
    else{
      time = time - (int)(System.currentTimeMillis() - runStart);
      runStart = -1;
      repaintTimer.stop();
    }
  }
  
  
  
  /**
   * Sets the time thresholds under which the clock switches to display
   * <code>minutes:seconds</code> and <code>minutes:seconds.tenths</code>, when
   * in <code>TIME_DEPENDENT_DISPLAY_MODE</code> mode.
   */
   
  public void setTimeDependentDisplayModeThresholds(int minutesSeconds, int secondTenths){
    this.minutesSecondsThreshold = minutesSeconds;
    this.secondTenthsThreshold = secondTenths;
    
    repaint();
  }
  
  
  
  /**
   * Sets the delay between repaints of the clock when it is running, in
   * milliseconds.
   */
   
  public void setRepaintDelay(int delay){
    repaintTimer.setDelay(delay);
  }
  
  
  
  /**
   * Returns the delay between repaints of the clock when it is running, in
   * milliseconds.
   */
   
  public int getRepaintDelay(){
    return repaintTimer.getDelay();
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
      case TIME_DEPENDENT_DISPLAY_MODE:
        break;
      default:
        throw new IllegalArgumentException("Unrecognized display mode value: " + displayMode);
    }
    
    this.displayMode = displayMode;
    repaint();
  }
  
  

  /**
   * Returns the current display mode of the clock. Possible values are
   * {@link #HOUR_MINUTE_DISPLAY_MODE}, {@link #MINUTE_SECOND_DISPLAY_MODE},
   * {@link #SECOND_TENTHS_DISPLAY_MODE} and
   * {@link #TIME_DEPENDENT_DISPLAY_MODE}.
   */

  public int getDisplayMode(){
    return displayMode;
  }
  
  
  
  /**
   * Returns the actual display mode. If the display mode is
   * <code>TIME_DEPENDENT_DISPLAY_MODE</code>, returns one of the other display
   * modes based on the current time.
   */
   
  protected int getActualDisplayMode(){
    int displayMode = getDisplayMode(); 
    if (displayMode != TIME_DEPENDENT_DISPLAY_MODE)
      return displayMode;
    
    int time = Math.abs(getTime());
    if (time < secondTenthsThreshold)
      return SECOND_TENTHS_DISPLAY_MODE;
    else if (time < minutesSecondsThreshold)
      return MINUTE_SECOND_DISPLAY_MODE;
    else
      return HOUR_MINUTE_DISPLAY_MODE;
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
