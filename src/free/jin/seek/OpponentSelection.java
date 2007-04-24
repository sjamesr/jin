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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import free.jin.I18n;
import free.jin.ServerUser;
import free.util.TableLayout;
import free.util.swing.ListWrapperComboBoxModel;
import free.util.swing.PreferredSizedPanel;
import free.util.swing.WrapperComponent;



/**
 * A UI element which allows the user to select his opponent in a match offer.
 */

public final class OpponentSelection extends WrapperComponent{
  
  
  
  /**
   * The combo box where the user makes his selection.
   */
  
  private final JComboBox box;
  
  
  
  /**
   * Creates a new <code>OpponentSelection</code> with the specified list of
   * opponents for easy selection and an initial value.
   */
  
  public OpponentSelection(ListModel easyAccessOpponents, ServerUser initialOpponent){
    new DefaultComboBoxModel();
    
    this.box = new JComboBox(new ListWrapperComboBoxModel(easyAccessOpponents));
    box.setEditable(true);
    box.setSelectedItem(initialOpponent);
    
    box.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e){
        fireStateChanged();
      }
    });
    
    // This is a hack, but JComboBox doesn't give us a way to know when the
    // selected item changed simply because the user typed something (until the
    // component loses focus).
    Component editor = box.getEditor().getEditorComponent();
    if (editor instanceof JTextComponent)
      ((JTextComponent)editor).getDocument().addDocumentListener(new DocumentListener(){
        public void changedUpdate(DocumentEvent e){
          fireStateChanged();
        }
        public void insertUpdate(DocumentEvent e){
          fireStateChanged();
        }
        public void removeUpdate(DocumentEvent e){
          fireStateChanged();
        }
      });
    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this component.
   */
  
  private void createUI(){
    I18n i18n = I18n.get(OpponentSelection.class);
    
    JPanel content = new PreferredSizedPanel(new TableLayout(2, 4, 5));
    content.add(i18n.createLabel(""));
    content.add(box);
    
    add(content);
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
    super.addChangeListener(listener);
  }
  
  
  
  /**
   * Removes the specified change listener from receiving notifications when the
   * selected opponent changes.
   */
  
  public void removeChangeListener(ChangeListener listener){
    super.removeChangeListener(listener);
  }
  
  
  
}
