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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.ListModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import free.jin.I18n;
import free.jin.ServerUser;
import free.util.ChangeSupport;
import free.util.swing.ListWrapperComboBoxModel;
import free.workarounds.FixedJComboBox;



/**
 * A UI element which allows the user to select his opponent in a match offer.
 */

public final class OpponentSelection{
  
  
  
  /**
   * The label.
   */
  
  private final JLabel label;
  
  
  
  /**
   * The combo box where the user makes his selection.
   */
  
  private final JComboBox box;
  
  
  
  /**
   * Our support for state change notifications.
   */
  
  private final ChangeSupport changeSupport = new ChangeSupport(this);
  
  
  
  /**
   * Creates a new <code>OpponentSelection</code> with the specified list of
   * opponents for easy selection and an initial value.
   */
  
  public OpponentSelection(ListModel easyAccessOpponents, ServerUser initialOpponent){
    I18n i18n = I18n.get(OpponentSelection.class);
    
    this.box = new FixedJComboBox(new ListWrapperComboBoxModel(easyAccessOpponents));
    box.setEditable(true);
    box.setSelectedItem(initialOpponent);
    
    box.addItemListener(new ItemListener(){
      @Override
      public void itemStateChanged(ItemEvent e){
        changeSupport.fireStateChanged();
      }
    });
    
    // This is a hack, but JComboBox doesn't give us a way to know when the
    // selected item changed simply because the user typed something (until the
    // component loses focus).
    Component editor = box.getEditor().getEditorComponent();
    if (editor instanceof JTextComponent)
      ((JTextComponent)editor).getDocument().addDocumentListener(new DocumentListener(){
        @Override
        public void changedUpdate(DocumentEvent e){
          changeSupport.fireStateChanged();
        }
        @Override
        public void insertUpdate(DocumentEvent e){
          changeSupport.fireStateChanged();
        }
        @Override
        public void removeUpdate(DocumentEvent e){
          changeSupport.fireStateChanged();
        }
      });
    
    this.label = i18n.createLabel("");
    label.setLabelFor(box);
  }
  
  
  
  /**
   * Returns the label.
   */
  
  public JLabel getLabel(){
    return label;
  }
  
  
  
  /**
   * Returns the opponent selection combo box.
   */
  
  public JComboBox getBox(){
    return box;
  }
  
  
  
  /**
   * Sets the currently selected opponent. May be <code>null</code> to indicate
   * a blank value.
   */
  
  public void setOpponent(ServerUser opponent){
    box.setSelectedItem(opponent);
  }
  
  
  
  /**
   * Returns the selected opponent.
   */
  
  public String getOpponentName(){
    // This is a hack, but JComboBox doesn't tell us the typed value until the
    // component loses focus.
    Component editor = box.getEditor().getEditorComponent();
    if (editor instanceof JTextComponent)
      return ((JTextComponent)editor).getText();
    else{
      Object selectedItem = box.getSelectedItem();
      return selectedItem == null ? "" : selectedItem.toString();
    }
  }
  
  
  
  /**
   * Adds the specified change listener to receive notifications when the
   * selected opponent changes. 
   */
  
  public void addChangeListener(ChangeListener listener){
    changeSupport.addChangeListener(listener);
  }
  
  
  
  /**
   * Removes the specified change listener from receiving notifications when the
   * selected opponent changes.
   */
  
  public void removeChangeListener(ChangeListener listener){
    changeSupport.removeChangeListener(listener);
  }
  
  
  
}
