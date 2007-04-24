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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import free.jin.I18n;
import free.util.swing.WrapperComponent;



/**
 * A UI element which allows the user to show/hide a part of the UI. 
 */

public final class MoreLessButton extends WrapperComponent{
  
  
  
  /**
   * The actual button.
   */
  
  private final JButton button;
  
  
  
  /**
   * The component we show/hide.
   */
  
  private final Component target;
  
  
  
  /**
   * Creates a new <code>MoreLessButton</code> with the specified target
   * component and initial state.
   */
  
  public MoreLessButton(Component target, boolean isMore){
    this.target = target;
    this.button = new JButton();
    button.setDefaultCapable(false);
    
    setMore(isMore);
    
    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        setMore(!isMore());
      }
    });
    
    add(button);
  }
  
  
  
  /**
   * Sets the state to the specified one.
   */
  
  private void setMore(boolean isMore){
    I18n i18n = I18n.get(MoreLessButton.class);
    
    button.setActionCommand(isMore ? "less" : "more");
    i18n.initAbstractButton(button, isMore ? "less" : "more");
    target.setVisible(isMore);
  }
  
  
  
  /**
   * Returns the current state.
   */
  
  public boolean isMore(){
    return "less".equals(button.getActionCommand());
  }
  
  
  
}
