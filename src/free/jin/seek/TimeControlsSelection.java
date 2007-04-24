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

package free.jin.seek;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import free.jin.I18n;
import free.util.TableLayout;
import free.util.swing.PreferredSizedPanel;
import free.util.swing.WrapperComponent;



/**
 * A UI element which allows the user to specify the time controls of a game.
 */

public final class TimeControlsSelection extends WrapperComponent{
  
  
  
  /**
   * The time control.
   */
  
  private final JSpinner time;
  
  
  
  /**
   * The increment control.
   */
  
  private final JSpinner increment;
  
  
  
  /**
   * Creates a new <code>TimeControlsSelection</code> with the specified initial
   * values for time and increment.
   */
  
  public TimeControlsSelection(int initialTime, int initialIncrement){
    this.time = new JSpinner(new SpinnerNumberModel(initialTime, 0, 600, 1));
    this.increment = new JSpinner(new SpinnerNumberModel(initialIncrement, 0, 300, 1));
    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this component.
   */
  
  private final void createUI(){
    I18n i18n = I18n.get(TimeControlsSelection.class);
    
    JPanel content = new PreferredSizedPanel(new TableLayout(3, 4, 6));
    
    JLabel timeLabel = i18n.createLabel("timeLabel");
    timeLabel.setLabelFor(time);
    timeLabel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    JLabel incLabel = i18n.createLabel("incrementLabel");
    incLabel.setLabelFor(increment);
    incLabel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    JLabel secondsLabel = i18n.createLabel("secondsLabel");
    secondsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    JLabel minutesLabel = i18n.createLabel("minutesLabel");
    minutesLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    time.setMaximumSize(time.getPreferredSize());
    increment.setMaximumSize(increment.getPreferredSize());
    
    // First row
    content.add(timeLabel);
    content.add(time);
    content.add(minutesLabel);
    
    // Second row
    content.add(incLabel);
    content.add(increment);
    content.add(secondsLabel);
    
    add(content);
  }
  
  
  
  /**
   * Returns the time selected by the user, in minutes.
   */
  
  public int getTime(){
    return ((Integer)time.getValue()).intValue();
  }
  
  
  
  /**
   * Returns the increment selected by the user, in seconds.
   */
  
  public int getIncrement(){
    return ((Integer)increment.getValue()).intValue();
  }
  
  
  
}
