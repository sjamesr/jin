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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import free.jin.I18n;
import free.jin.plugin.PluginUIContainer;
import free.util.AWTUtilities;



/**
 * A UI element which allows the user to show/hide a part of the UI. 
 */

public final class MoreLessButton{
  
  
  
  /**
   * The actual button.
   */
  
  private final JButton button;
  
  
  
  /**
   * The list of components we show/hide.
   */
  
  private final Component [] components;
  
  
  
  /**
   * The plugin ui container we're being put into.
   */
  
  private final PluginUIContainer container;
  
  
  
  /**
   * Creates a new <code>MoreLessButton</code> with the specified components to
   * show/hide, initial state and the plugin ui container the components are all
   * inside of.
   */
  
  public MoreLessButton(boolean isMore, PluginUIContainer container, Component [] components){
    this.components = components;
    this.container = container;
    
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
   * Returns the button.
   */
  
  public JButton getButton(){
    return button;
  }
  
  
  
  /**
   * Sets the state to the specified one.
   */
  
  private void setMore(boolean isMore){
    I18n i18n = I18n.get(MoreLessButton.class);
    
    button.setActionCommand(isMore ? "less" : "more");
    i18n.initAbstractButton(button, isMore ? "less" : "more");
    
    for (int i = 0; i < components.length; i++)
      components[i].setVisible(isMore);
    
    if (isMore && container.isVisible()){
      // Need to wait for all delayed layout to finish
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          Container contentPane = container.getContentPane();
          if (!AWTUtilities.fitsInto(contentPane.getMinimumSize(), contentPane.getSize()))
            container.pack();
        }
      });
    }
  }
  
  
  
  /**
   * Returns the current state.
   */
  
  public boolean isMore(){
    return "less".equals(button.getActionCommand());
  }
  
  
  
}
