/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
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

package free.jin.console.prefs;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.swing.*;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;
import free.util.swing.PreferredSizedPanel;



/**
 * Preferences panel for selecting the encoding used by the console.
 */

public class EncodingPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The console manager.
   */
  
  private final ConsoleManager consoleManager;
  
  
  
  /**
   * The list of encodings.
   */
  
  private final JList encodingsList;
  
  
  
  /**
   * Creates a new <code>EncodingPrefsPanel</code> for the specified console manager.
   */

  public EncodingPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;
    
    this.encodingsList = new JList(Charset.availableCharsets().values().toArray());
    
    encodingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    encodingsList.addListSelectionListener(proxyListSelectionListener);
    
    createUI();
  }
  
  
  
  /**
   * Creates the user interface of this panel.
   */
  
  private void createUI(){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    encodingsList.setVisibleRowCount(10);
    
    JPanel panel = new PreferredSizedPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    JLabel encodingsLabel = I18n.get(EncodingPrefsPanel.class).createLabel("encodingsLabel");
    encodingsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    encodingsLabel.setLabelFor(encodingsList);
    
    panel.add(encodingsLabel);
    
    panel.add(Box.createVerticalStrut(10));
    
    JScrollPane scroller = new JScrollPane(encodingsList);
    scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    encodingsList.setSelectedValue(Charset.forName(consoleManager.getEncoding()), true);
    
    panel.add(scroller);
    
    add(panel);
  }
  
  

  public void applyChanges() throws BadChangesException{
    Charset selected = (Charset)encodingsList.getSelectedValue();
    
    try{
      consoleManager.setEncoding(selected.name());
    } catch (UnsupportedEncodingException e){
        e.printStackTrace(); // This shouldn't happen because we let the user select only from supported charsets
      }
  }
  
  
  
}
