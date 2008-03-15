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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;
import free.util.IOUtilities;
import free.util.NamedWrapper;
import free.util.TextUtilities;
import free.util.swing.SwingUtils;



/**
 * Preferences panel for selecting the encoding used by the console.
 */

public class EncodingPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The category key for all the encodings.
   */
  
  private static final String ALL_CATEGORY_KEY = "all";
  
  
  
  /**
   * A map of i18n keys of charset "category" names to the list (expressed as a
   * <code>ListModel</code>) of charsets in that category.
   */
  
  private static final Map CHARSET_CATEGORIES = new TreeMap();
  static{
    try{
      // The "all" category must be first.
      CHARSET_CATEGORIES.put(ALL_CATEGORY_KEY, SwingUtils.collectionListModel(Charset.availableCharsets().values()));
      
      Properties props = IOUtilities.loadProperties(EncodingPrefsPanel.class.getResourceAsStream("charsets.properties"));
      for (Iterator categories = props.entrySet().iterator(); categories.hasNext();){
        Map.Entry entry = (Map.Entry)categories.next();
        String categoryKey = (String)entry.getKey();
        String [] aliases = TextUtilities.parseStringList((String)entry.getValue(), ", ");
        DefaultListModel charsets = new DefaultListModel();
        for (int i = 0; i < aliases.length; i++){
          String alias = aliases[i];
          if (Charset.isSupported(alias))
            charsets.addElement(Charset.forName(aliases[i]));
        }
        
        if (!charsets.isEmpty())
          CHARSET_CATEGORIES.put(categoryKey, charsets);
      }
    } catch (IOException e){
        e.printStackTrace();
    }
  }
  
  
  
  /**
   * The console manager.
   */
  
  private final ConsoleManager consoleManager;
  
  
  
  /**
   * The list of encoding categories.
   */
  
  private final JList encodingCategories;
  
  
  
  /**
   * The list of encodings.
   */
  
  private final JList encodings;
  
  
  
  /**
   * Creates a new <code>EncodingPrefsPanel</code> for the specified console manager.
   */

  public EncodingPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;
    this.encodingCategories = makeEncodingCategoriesList();
    this.encodings = makeEncodingsList();
    
    createUI();
    
    encodingCategories.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        if (encodingCategories.getSelectedIndex() == -1)
          return;
        
        String categoryKey = (String)((NamedWrapper)encodingCategories.getSelectedValue()).getTarget();
        ListModel charsets = (ListModel)CHARSET_CATEGORIES.get(categoryKey);
        encodings.setModel(charsets);
      }
    });
    
    encodingCategories.setSelectedIndex(0);
    if (consoleManager.getEncoding() != null)
      encodings.setSelectedValue(Charset.forName(consoleManager.getEncoding()), true);
    else
      encodings.clearSelection();
  }
  
  
    
  /**
   * Creates the list of encoding categories.
   */
  
  private JList makeEncodingCategoriesList(){
    I18n i18n = I18n.get(EncodingPrefsPanel.class);
    
    DefaultListModel model = new DefaultListModel();
    for (Iterator i = CHARSET_CATEGORIES.keySet().iterator(); i.hasNext();){
      String categoryKey = (String)i.next();
      model.addElement(new NamedWrapper(categoryKey, i18n.getString("encodingCategory." + categoryKey)));
    }
    
    JList list = new JList(model);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    return list;
  }
  
  
  
  /**
   * Creates the list of encodings.
   */
  
  private JList makeEncodingsList(){
    JList list = new JList();
    
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(proxyListSelectionListener);
    
    return list;
  }
  
  
  
  
  /**
   * Creates the user interface of this panel.
   */
  
  private void createUI(){
    I18n i18n = I18n.get(EncodingPrefsPanel.class);
    
    encodingCategories.setVisibleRowCount(10);
    encodings.setVisibleRowCount(10);
    
    JScrollPane encodingsScroller = new JScrollPane(encodings);
    encodingsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    encodingsScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    JScrollPane encodingCategoriesScroller = new JScrollPane(encodingCategories);
    encodingCategoriesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    encodingCategoriesScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    JLabel encodingCategoriesLabel = i18n.createLabel("encodingCategoriesLabel");
    encodingCategoriesLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    encodingCategoriesLabel.setLabelFor(encodingCategories);
    
    JLabel encodingsLabel = i18n.createLabel("encodingsLabel");
    encodingsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    encodingsLabel.setLabelFor(encodings);
    
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    layout.setAutocreateContainerGaps(true);
    
    layout.setHorizontalGroup(layout.createSequentialGroup()
      .add(layout.createParallelGroup(GroupLayout.LEADING)
        .add(encodingCategoriesLabel)
        .add(encodingCategoriesScroller)
      )
      .addPreferredGap(LayoutStyle.UNRELATED)
      .add(layout.createParallelGroup(GroupLayout.LEADING)
        .add(encodingsLabel)
        .add(encodingsScroller)
      )
    );
      
    layout.setVerticalGroup(layout.createParallelGroup()
      .add(layout.createSequentialGroup()
        .add(encodingCategoriesLabel)
        .addPreferredGap(LayoutStyle.RELATED)
        .add(encodingCategoriesScroller)
      )
      .add(layout.createSequentialGroup()
        .add(encodingsLabel)
        .addPreferredGap(LayoutStyle.RELATED)
        .add(encodingsScroller)
      )
    );
  }
  
  

  public void applyChanges() throws BadChangesException{
    Charset selected = (Charset)encodings.getSelectedValue();
    
    try{
      consoleManager.setEncoding(selected.name());
    } catch (UnsupportedEncodingException e){
        e.printStackTrace(); // This shouldn't happen because we let the user select only from supported charsets
      }
  }
  
  
  
}
