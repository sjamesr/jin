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

import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;
import free.util.Encodings;
import free.util.NamedWrapper;
import free.util.TextUtilities;
import free.util.Utilities;
import free.util.swing.SwingUtils;



/**
 * Preferences panel for selecting the default encoding used by the consoles.
 */

public class EncodingPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The name for the fake "all" encodings category.
   */
  
  private static final String ALL_ENCODINGS_CATEGORY = "all";
  
  
  
  /**
   * Maps encoding categories to <code>ListModel</code>s of encodings in that
   * category.
   */
  
  private static final Map CATEGORIES_TO_ENCODING_LIST_MODELS = new TreeMap();
  static{
    Collection allEncodings = new LinkedList();
    allEncodings.add(null); // No encoding
    allEncodings.addAll(Charset.availableCharsets().values());
    CATEGORIES_TO_ENCODING_LIST_MODELS.put(ALL_ENCODINGS_CATEGORY,
        SwingUtils.collectionListModel(allEncodings));
    
    Map categoriesToEncodings = Encodings.categoriesToEncodings();
    for (Iterator i = Encodings.categories().iterator(); i.hasNext();){
      String category = (String)i.next();
      CATEGORIES_TO_ENCODING_LIST_MODELS.put(category, 
          SwingUtils.collectionListModel((Collection)categoriesToEncodings.get(category)));
    }
  }
  
  
  
  /**
   * The displayed name for the "system default" encoding. 
   */
  
  private static final String DEFAULT_ENCODING_NAME = 
    I18n.get(EncodingPrefsPanel.class).getFormattedString("defaultEncoding.name", 
        new Object[]{TextUtilities.getDefaultCharsetName()});
  
  
  
  /**
   * The console manager.
   */
  
  private final ConsoleManager consoleManager;
  
  
  
  /**
   * The list of encoding categories.
   */
  
  private final JList encodingCategories;
  
  
  
  /**
   * The list of encodings in the currently selected category.
   */
  
  private final JList encodings;
  
  
  
  /**
   * The currently selected encoding.
   */
  
  private Charset selectedEncoding;
  
  
  
  /**
   * A flag we set when changing the encoding selection programmatically such
   * that the selectedEncoding shouldn't change. 
   */
  
  private boolean ignoreEncodingSelectionChanges = false;
  
  
  
  /**
   * Creates a new <code>EncodingPrefsPanel</code> for the specified console manager.
   */

  public EncodingPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;
    
    I18n i18n = I18n.get(EncodingPrefsPanel.class);
    
    final Map categoryNames = Encodings.categoriesToNames();
    DefaultListModel encodingCategoriesModel = new DefaultListModel();
    encodingCategoriesModel.addElement(
        new NamedWrapper(ALL_ENCODINGS_CATEGORY, i18n.getString("allEncodings.name")));
    
    for (Iterator i = Encodings.categories().iterator(); i.hasNext();){
      String category = (String)i.next();
      encodingCategoriesModel.addElement(new NamedWrapper(category, (String)categoryNames.get(category)));
    }
    
    this.encodingCategories = new JList(encodingCategoriesModel);
    this.encodings = new JList();
    
    final ListCellRenderer encodingsCellRenderer = encodings.getCellRenderer();
    encodings.setCellRenderer(new ListCellRenderer(){
      public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus){
        if (value == null)
          value = DEFAULT_ENCODING_NAME;
        return encodingsCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
    
    encodingCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    encodings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    encodingCategories.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        try{
          ignoreEncodingSelectionChanges = true;
          
          NamedWrapper selection = (NamedWrapper)encodingCategories.getSelectedValue();
          
          ListModel model;
          if (selection == null)
            model = new DefaultListModel();
          else{
            String categoryKey = (String)(selection.getTarget());
            model = (ListModel)CATEGORIES_TO_ENCODING_LIST_MODELS.get(categoryKey);
          }
          
          encodings.setModel(model);
          
          boolean isSelectedDefault = TextUtilities.getDefaultCharset().equals(selectedEncoding);
          
          int selectedIndex = -1;
          for (int i = 0; i < model.getSize(); i++){
            Charset encoding = (Charset)model.getElementAt(i);
            if (Utilities.areEqual(encoding, selectedEncoding) || (isSelectedDefault && (encoding == null))){
              selectedIndex = i;
              break;
            }
          }
          encodings.setSelectedIndex(selectedIndex);
        } finally {
          ignoreEncodingSelectionChanges = false;
        }
      }
    });
    
    encodings.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        if (ignoreEncodingSelectionChanges)
          return;
        
        selectedEncoding = (Charset)encodings.getSelectedValue();
        fireStateChanged();
      }
    });
    
    selectedEncoding = Charset.forName(consoleManager.getEncoding());
    encodingCategories.setSelectedIndex(0);
    
    createUI();
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
      if (selected == null)
        consoleManager.setEncoding(null);
      else
        consoleManager.setEncoding(selected.name());
    } catch (UnsupportedEncodingException e){
        e.printStackTrace(); // This shouldn't happen because we let the user select only from supported charsets
      }
  }
  
  
  
}
