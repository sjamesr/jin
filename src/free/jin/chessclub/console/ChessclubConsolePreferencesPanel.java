/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.chessclub.console;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import free.util.swing.*;
import free.util.StringParser;
import free.util.StringEncoder;
import free.jin.plugin.PreferencesPanel;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * The preferences panel for the chessclub console manager.
 */

public class ChessclubConsolePreferencesPanel extends PreferencesPanel{



  /**
   * The ChessclubConsoleManager this panel shows preferences for.
   */

  private final ChessclubConsoleManager consoleManager;




  /**
   * Our own copy of the user properties merged with plugin properties.
   */

  private final Properties props;



  
  /**
   * Maps (short) category names to the corresponding TextStyleChooserPanels.
   * Note that since not all categories must contain a TextStyleChooserPanels, 
   * some categories may be missing from this hashtable.
   */

  private final Hashtable categoriesToTextStyleChoosers = new Hashtable();




  /**
   * Maps TextStyleChooserPanels to (short) category names. A reverse hashtable
   * to the <code>categoriesToTextStyleChoosers</code> one.
   */

  private final Hashtable textStyleChoosersToCategories = new Hashtable();




  /**
   * Maps category names of categories that appear in the
   * <code>categoriesToTextStyleChoosers</code> to arrays of categories that
   * need to be updated when that category changes.
   */

  private final Hashtable visibleCategoriesToTotal = new Hashtable();

  



  /**
   * Maps category names as they appear in the list to panels that should be 
   * displayed when that category is selected.
   */

  private final Hashtable categoriesToPanels = new Hashtable();




  /**
   * The selection color choosing button.
   */

  private final ColorChooserButton selectionChooserButton;




  /**
   * The selected text color choosing button.
   */

  private final ColorChooserButton selectedChooserButton;




  /**
   * The holder panel for the various category specific panels.
   */

  private final JPanel categoryHolderPanel = new JPanel(new BorderLayout());




  /**
   * Creates a new ChessclubConsolePreferencesPanel for the given
   * ChessclubConsoleManager.
   */

  public ChessclubConsolePreferencesPanel(ChessclubConsoleManager consoleManager){
    this.consoleManager = consoleManager;

    props = new Properties(consoleManager.getPluginContext().getProperties());
    Properties userProps = consoleManager.getUser().getProperties();
    Enumeration userPropsEnum = userProps.keys();
    String pluginID = consoleManager.getID();
    while (userPropsEnum.hasMoreElements()){
      String key = (String)userPropsEnum.nextElement();
      if (key.startsWith(pluginID)){
        Object value = userProps.get(key);
        key = key.substring(key.indexOf(".")+1);
        props.put(key, value);
      }
    }

    Color bgColor = StringParser.parseColor(props.getProperty("output-background"));
    Color selectionColor = StringParser.parseColor(props.getProperty("output-selection"));
    Color selectedColor = StringParser.parseColor(props.getProperty("output-selected"));

    selectionChooserButton = new ColorChooserButton("Selection", selectionColor);
    selectedChooserButton = new ColorChooserButton("Selected text", selectedColor);

    addCategory("", new String[]{""}, bgColor);
    addCategory("tell", new String[]{"tell"}, bgColor);
    addCategory("say", new String[]{"say"}, bgColor);
    addCategory("qtell", new String[]{"qtell", "channel-qtell"}, bgColor);
    addCategory("shout", new String[]{"shout", "ishout"}, bgColor);
    addCategory("sshout", new String[]{"sshout"}, bgColor);
    addCategory("announcement", new String[]{"announcement"}, bgColor);
    addCategory("kibitz", new String[]{"kibitz"}, bgColor);
    addCategory("whisper", new String[]{"whisper"}, bgColor);
    addCategory("link", new String[]{"link"}, bgColor);
    addCategory("user", new String[]{"user"}, bgColor);
    addCategory("info", new String[]{"info"}, bgColor);

    createUI();
  }



  
  /**
   * Looks up the given property value.
   */

  private String lookupProperty(String propertyName){
    String propertyValue = props.getProperty(propertyName);
    if (propertyValue == null){
      int dotIndex = propertyName.lastIndexOf(".");
      if (dotIndex == -1)
        return null;
      return lookupProperty(propertyName.substring(0,dotIndex));
    }
    else
      return propertyValue;
  }



  /**
   * A boolean we set to true so we know not to handle echo change events.
   */

  private boolean handlingChangeEvent = false;



  /**
   * The listener that listens to text style changes in the various panels
   * and updates the properties accordingly.
   */

  ChangeListener propsChangeListener = new ChangeListener(){
    public void stateChanged(ChangeEvent evt){
      if (handlingChangeEvent)
        return;

      handlingChangeEvent = true;
      TextStyleChooserPanel source = (TextStyleChooserPanel)evt.getSource();
      String categoryName = (String)textStyleChoosersToCategories.get(source);
      String [] categoriesToUpdate = (String [])visibleCategoriesToTotal.get(categoryName);

      Font selectedFont = source.getSelectedFont();
      Color selectedBG = source.getSelectedBackground();
      Color selectedFG = source.getSelectedForeground();

      for (int i = 0; i < categoriesToUpdate.length; i++){
        String category = categoriesToUpdate[i];
        props.put(propertyName(category, "font-family"), selectedFont.getFamily());
        props.put(propertyName(category, "font-size"), String.valueOf(selectedFont.getSize()));
        props.put(propertyName(category, "font-bold"), String.valueOf(selectedFont.isBold()));
        props.put(propertyName(category, "font-italic"), String.valueOf(selectedFont.isItalic()));
        props.put(propertyName(category, "foreground"), StringEncoder.encodeColor(selectedFG));
      }

      if (categoryName.equals(""))
        props.put("output-background", StringEncoder.encodeColor(selectedBG));

      updatePanels();
      handlingChangeEvent = false;
    }
  };





  /**
   * Refreshes the settings on all the panels from the properties.
   */

  private void updatePanels(){
    Color background = StringParser.parseColor(props.getProperty("output-background"));

    Enumeration categoriesEnum = categoriesToTextStyleChoosers.keys();
    while (categoriesEnum.hasMoreElements()){
      String categoryName = (String)categoriesEnum.nextElement();
      TextStyleChooserPanel chooserPanel = (TextStyleChooserPanel)categoriesToTextStyleChoosers.get(categoryName);

      String fontFamily = lookupProperty("font-family."+categoryName);
      int fontSize = Integer.parseInt(lookupProperty("font-size."+categoryName));
      int fontStyle = 0;
      if (new Boolean(lookupProperty("font-bold."+categoryName)).booleanValue())
        fontStyle |= Font.BOLD;
      if (new Boolean(lookupProperty("font-italic."+categoryName)).booleanValue())
        fontStyle |= Font.ITALIC;
      Color foreground = StringParser.parseColor(lookupProperty("foreground."+categoryName));

      Font font = new Font(fontFamily, fontStyle, fontSize);

      chooserPanel.setSelectedFont(font);
      chooserPanel.setSelectedBackground(background);
      chooserPanel.setSelectedForeground(foreground);
    }
  }




  /**
   * Adds a category with a corresponding TextStyleChooserPanel to the
   * <code>categoriesToTextStyleChoosers</code> (and reverse) hashtable.
   */

  private void addCategory(String categoryName, String [] categoriesToUpdate, Color background){
    String fontFamily = lookupProperty("font-family."+categoryName);
    int fontSize = Integer.parseInt(lookupProperty("font-size."+categoryName));
    int fontStyle = 0;
    if (new Boolean(lookupProperty("font-bold."+categoryName)).booleanValue())
      fontStyle |= Font.BOLD;
    if (new Boolean(lookupProperty("font-italic."+categoryName)).booleanValue())
      fontStyle |= Font.ITALIC;
    Color foreground = StringParser.parseColor(lookupProperty("foreground."+categoryName));

    Font font = new Font(fontFamily, fontStyle, fontSize);
    TextStyleChooserPanel textStyleChooser = new TextStyleChooserPanel(font, foreground, background, "".equals(categoryName));
    textStyleChooser.addChangeListener(propsChangeListener);

    categoriesToTextStyleChoosers.put(categoryName, textStyleChooser);
    textStyleChoosersToCategories.put(textStyleChooser, categoryName);
    visibleCategoriesToTotal.put(categoryName, categoriesToUpdate);
  }




  /**
   * Creates the UI.
   */

  private void createUI(){
    setLayout(new BorderLayout(10, 10));

    DefaultListModel categoriesModel = new DefaultListModel();
    
    JPanel defaultPanel = new JPanel(new BorderLayout(5, 5));
    defaultPanel.add(BorderLayout.CENTER, (JPanel)categoriesToTextStyleChoosers.get(""));
    JPanel selectionColorPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    selectionColorPanel.add(selectionChooserButton);
    selectionColorPanel.add(selectedChooserButton);
    defaultPanel.add(BorderLayout.SOUTH, selectionColorPanel);
    addPanel("Default settings", categoriesModel, defaultPanel);
    selectionChooserButton.setDefaultCapable(false);
    selectedChooserButton.setDefaultCapable(false);

    JPanel tellPanel = (JPanel)categoriesToTextStyleChoosers.get("tell");
    addPanel("Personal tells", categoriesModel, tellPanel);

    JPanel sayPanel = (JPanel)categoriesToTextStyleChoosers.get("say");
    addPanel("\"Say\" tells", categoriesModel, sayPanel);

    JPanel qtellPanel = (JPanel)categoriesToTextStyleChoosers.get("qtell");
    addPanel("Qtells (Bot tells)", categoriesModel, qtellPanel);

    JPanel shoutPanel = (JPanel)categoriesToTextStyleChoosers.get("shout");
    addPanel("Shouts", categoriesModel, shoutPanel);

    JPanel sshoutPanel = (JPanel)categoriesToTextStyleChoosers.get("sshout");
    addPanel("S-Shouts", categoriesModel, sshoutPanel);

    JPanel announcementPanel = (JPanel)categoriesToTextStyleChoosers.get("announcement");
    addPanel("Announcements", categoriesModel, announcementPanel);

    JPanel kibitzPanel = (JPanel)categoriesToTextStyleChoosers.get("kibitz");
    addPanel("Kibitzes", categoriesModel, kibitzPanel);

    JPanel whisperPanel = (JPanel)categoriesToTextStyleChoosers.get("whisper");
    addPanel("Whispers", categoriesModel, whisperPanel);

    JPanel linkPanel = (JPanel)categoriesToTextStyleChoosers.get("link");
    addPanel("Links", categoriesModel, linkPanel);

    JPanel userPanel = (JPanel)categoriesToTextStyleChoosers.get("user");
    addPanel("User commands", categoriesModel, userPanel);

    JPanel infoPanel = (JPanel)categoriesToTextStyleChoosers.get("info");
    addPanel("Information", categoriesModel, infoPanel);


    categoryHolderPanel.add(BorderLayout.CENTER, defaultPanel);

    final JList categoryList = new JList(categoriesModel);
    categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    categoryList.setSelectedIndex(0);
    JScrollPane scrollPane = new JScrollPane(categoryList);

    JPanel listPanel = new JPanel(new BorderLayout());
    listPanel.add(BorderLayout.NORTH, new JLabel("Text type", JLabel.CENTER));
    listPanel.add(BorderLayout.CENTER, scrollPane);

    categoryList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        Object selectedValue = categoryList.getSelectedValue();
        categoryHolderPanel.removeAll();
        categoryHolderPanel.add((Component)categoriesToPanels.get(selectedValue));
        invalidate();
        validate();
        repaint();
      }
    });

    add(BorderLayout.WEST, listPanel);
    add(BorderLayout.CENTER, categoryHolderPanel);
  }





  /**
   * Assosiates the given category name with the given panel by adding it to
   * the list model and the categoriesToPanels hashtable.
   */

  private void addPanel(String categoryName, DefaultListModel listModel, Component panel){
    listModel.addElement(categoryName);
    categoriesToPanels.put(categoryName, panel);
  }




  /**
   * Applies the changes done by the user.
   */

  public void applyChanges(){
    Enumeration changedProps = props.keys();
    while (changedProps.hasMoreElements()){
      String propertyName = (String)changedProps.nextElement();
      String propertyValue = (String)props.get(propertyName);
      if (!consoleManager.lookupProperty(propertyName).equals(propertyValue))
        consoleManager.setProperty(propertyName, propertyValue, true);
    }

    String newSelectionColor = StringEncoder.encodeColor(selectionChooserButton.getColor());
    if (!newSelectionColor.equals(consoleManager.lookupProperty("output-selection")))
      consoleManager.setProperty("output-selection", newSelectionColor, true);

    String newSelectedColor = StringEncoder.encodeColor(selectedChooserButton.getColor());
    if (!newSelectedColor.equals(consoleManager.lookupProperty("output-selected")))
      consoleManager.setProperty("output-selected", newSelectedColor, true);

    consoleManager.refreshFromProperties();
  }




  /**
   * Returns the property name corresponding to the given category name and
   * property type.
   */

  private String propertyName(String categoryName, String propertyType){
    if (categoryName.equals(""))
      return propertyType;
    else
      return propertyType+"."+categoryName;
  }


}