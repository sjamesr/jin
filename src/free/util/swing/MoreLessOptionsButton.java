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

package free.util.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.ChangeListener;

import free.util.ChangeSupport;
import free.util.Localization;



/**
 * A UI element which allows the user to show/hide a part of the UI. 
 */

public final class MoreLessOptionsButton{
  
  
  
  /**
   * The localization for this class, loaded lazily.
   */
  
  private static Localization l10n;

  
  
  /**
   * The actual button.
   */
  
  private final JButton button;
  
  
  
  /**
   * The list of components we show/hide.
   */
  
  private final Component [] components;
  
  
  
  /**
   * Our change support.
   */
  
  private final ChangeSupport changeSupport = new ChangeSupport(this);
  
  
  
  /**
   * Creates a new <code>MoreLessOptionsButton</code> with the specified components to
   * show/hide, initial state and the plugin ui container the components are all
   * inside of.
   */
  
  public MoreLessOptionsButton(boolean isMore, Component [] components){
    this.components = components;
    
    this.button = new JButton();
    button.setDefaultCapable(false);
    
    setMore(isMore);
    
    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        setMore(!isMore());
      }
    });
  }
  
  
  
  /**
   * Returns the localization for this class.
   */
  
  private static synchronized Localization getL10n(){
    if (l10n == null)
      l10n = LocalizationService.getForClass(MoreLessOptionsButton.class);
    return l10n;
  }
  
  
  
  /**
   * Returns the button.
   */
  
  public JButton getButton(){
    return button;
  }
  
  
  
  /**
   * Adds a change listener.
   */
  
  public void addChangeListener(ChangeListener listener){
    changeSupport.addChangeListener(listener);
  }
  
  
  
  /**
   * Removes a change listener.
   */
  
  public void removeChangeListener(ChangeListener listener){
    changeSupport.removeChangeListener(listener);
  }
  
  
  
  /**
   * Sets the state to the specified one.
   */
  
  private void setMore(boolean isMore){
    button.setActionCommand(isMore ? "less" : "more");
    button.setText(getL10n().getString(isMore ? "less.text" : "more.text"));
    
    for (int i = 0; i < components.length; i++)
      components[i].setVisible(isMore);
    
    changeSupport.fireStateChanged();
  }
  
  
  
  /**
   * Returns the current state.
   */
  
  public boolean isMore(){
    return "less".equals(button.getActionCommand());
  }
  
  
  
}
