/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
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

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import free.util.swing.ColorChooser;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;


/**
 * The preferences panel for the console manager.
 */

public class TextPrefsPanel extends PreferencesPanel{



  /**
   * The ConsoleManager this panel shows preferences for.
   */

  protected final ConsoleManager consoleManager;
  
  
  
  /**
   * The preferences we've modified as a result of user actions.
   */

  protected final Preferences prefs;



  /**
   * A list of panels to be displayed. The items in the list are CategoryPanels.
   */

  private final Vector categoryPanels = new Vector();



  /**
   * The currently visible category panel.
   */

  private CategoryPanel currentCategoryPanel;



  /**
   * The selection color choosing button.
   */

  private ColorChooser selectionColorButton;



  /**
   * The selected text color choosing button.
   */

  private ColorChooser selectedColorButton;



  /**
   * The default settings panel.
   */

  private CategoryPanel defaultSettingsPanel;



  /**
   * A holder panel for the current category panel.
   */

  private final JPanel categoryPanelHolder = new JPanel(new BorderLayout());


  
  /**
   * Creates a new TextPrefsPanel for the givenConsoleManager.
   */

  public TextPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;

    prefs = Preferences.createBackedUp(Preferences.createNew(), consoleManager.getPrefs());

    createSettingsPanels();

    createLayout();
  }



  /**
   * Adds the specified CategoryPanel to the list of displayed panels.
   */

  protected void addCategoryPanel(CategoryPanel panel){
    categoryPanels.addElement(panel);
    
    TextStyleChooserPanel textStyleChooser = panel.getTextStyleChooser();
    if (textStyleChooser != null)
      textStyleChooser.addChangeListener(settingsChangeListener);
  }



  /**
   * A flag we set to true so we know not to handle echo change events.
   */

  private boolean handlingChangeEvent = false;



  /**
   * The listener that listens to settings changes in the various panels
   * and updates the properties accordingly. You may register this
   * listener as the change listener of any custom components you're adding.
   * Note that if you do that, you should also override
   * <code>updatePropertiesFrom</code> and <code>updatePanels</code> to apply
   * changes and update your components respectively.
   */

  protected final ChangeListener settingsChangeListener = new ChangeListener(){
    public void stateChanged(ChangeEvent evt){
      if (handlingChangeEvent)
        return;

      handlingChangeEvent = true;

      updatePropertiesFrom(currentCategoryPanel);
      updatePanels();
      fireStateChanged();

      handlingChangeEvent = false;
    }
  };



  /**
   * Updates the properties from the settings of the specified CategoryPanel.
   * This method is called whenever the settings on the current CategoryPanel
   * change.
   */

  protected void updatePropertiesFrom(CategoryPanel categoryPanel){
    TextStyleChooserPanel textStyleChooser = categoryPanel.getTextStyleChooser();
    if (textStyleChooser == null)
      throw new IllegalStateException("Updating preferences from a " +
                                      "nondisplayed category panel");  

    if (categoryPanel == defaultSettingsPanel){
      prefs.setColor("output-selection", selectionColorButton.getColor());
      prefs.setColor("output-selected", selectedColorButton.getColor());
      prefs.setColor("background", textStyleChooser.getSelectedBackground());

      if (textStyleChooser.isAntialiasingSelectionEnabled())
        prefs.setBool("output-text.antialias",
          defaultSettingsPanel.getTextStyleChooser().isAntialias());
    }

    Font font = textStyleChooser.getSelectedFont();
    Color foreground = textStyleChooser.getSelectedForeground();

    String [] categoriesToUpdate = categoryPanel.getCategories();
    for (int i = 0; i < categoriesToUpdate.length; i++){
      String category = categoriesToUpdate[i];

      setProperty(category, "font-family", font.getFamily());
      setProperty(category, "font-size", new Integer(font.getSize()));
      setProperty(category, "font-bold", font.isBold() ? Boolean.TRUE : Boolean.FALSE);
      setProperty(category, "font-italic", font.isItalic() ? Boolean.TRUE : Boolean.FALSE);
      setProperty(category, "foreground", foreground);
    }
  }



  /**
   * Updates all the CategoryPanels and their TextStyleChooserPanels from the
   * properties. This method is called whenever the properties change which may
   * require updating some of the category panels.
   */

  protected void updatePanels(){
    selectionColorButton.setColor(prefs.getColor("output-selection", UIManager.getColor("textHighlight")));
    selectedColorButton.setColor(prefs.getColor("output-selected", UIManager.getColor("textHighlightText")));

    Color background = prefs.getColor("background");
    boolean antialias = prefs.getBool("output-text.antialias", false);

    for (int i = 0; i < categoryPanels.size(); i++){
      CategoryPanel panel = (CategoryPanel)categoryPanels.elementAt(i);
      TextStyleChooserPanel textStyleChooser = panel.getTextStyleChooser();
      if (textStyleChooser == null)
        continue;
      
      String mainCategory = panel.getMainCategory();

      Font font = getCategoryFont(mainCategory);
      Color foreground = (Color)prefs.lookup("foreground." + mainCategory, Color.white);

      textStyleChooser.setSelectedFont(font);
      textStyleChooser.setSelectedForeground(foreground);
      textStyleChooser.setSelectedBackground(background);
      textStyleChooser.setAntialias(antialias);
    }
  }



  /**
   * Creates all the settings panels.
   */

  protected void createSettingsPanels(){
    createDefaultSettingsPanel();
   
    createSettingsPanelsFromProperties();
  }



  /**
   * Creates the default settings panel.
   */

  private void createDefaultSettingsPanel(){
    Font font = getCategoryFont("");
    Color foreground = prefs.getColor("foreground");
    Color background = prefs.getColor("background");

    boolean antialiasingSupported;
    try{
      antialiasingSupported = Class.forName("java.awt.Graphics2D") != null;
    } catch (ClassNotFoundException e){
        antialiasingSupported = false;
      }
    boolean antialiasingValue = prefs.getBool("output-text.antialias", false);

    TextStyleChooserPanel defaultSettingsChooserPanel = 
      new TextStyleChooserPanel(font, foreground, background, antialiasingValue, true, antialiasingSupported);

    selectionColorButton = createSelectionColorButton();
    selectedColorButton = createSelectedColorButton();

    defaultSettingsPanel = 
      new CategoryPanel(I18n.get(TextPrefsPanel.class).getString("defaultTextCategoryName"), 
        defaultSettingsChooserPanel, new String[]{""});
    defaultSettingsPanel.setLayout(new BorderLayout(5, 5));
    defaultSettingsPanel.add(defaultSettingsChooserPanel, BorderLayout.CENTER);
    JPanel selectionColorPanel = new JPanel(new GridLayout(1, 2, 15, 5));
    selectionColorPanel.add(selectionColorButton);
    selectionColorPanel.add(selectedColorButton);
    defaultSettingsPanel.add(selectionColorPanel, BorderLayout.SOUTH);

    addCategoryPanel(defaultSettingsPanel);
    selectionColorButton.addChangeListener(settingsChangeListener);
    selectedColorButton.addChangeListener(settingsChangeListener);
  }



  /**
   * Creates settings panels as specified in the properties.
   */

  private void createSettingsPanelsFromProperties(){
    int categoriesCount = prefs.getInt("preferences.categories.count", 0);
    
    // We need this one because the categories (and thus the localization)
    // are defined in a server specified package
    I18n consoleManagerI18n = consoleManager.getI18n();

    for (int i = 0; i < categoriesCount; i++){
      CategoryPanel categoryPanel;

      boolean isCustomPanel = prefs.getBool("preferences.categories." + i + ".custom", false);

      if (isCustomPanel){
        String id = prefs.getString("preferences.categories." + i + ".id");
        categoryPanel = createCustomCategoryPanel(id);
      }
      else{
        String categoryNameKey = prefs.getString("preferences.categories." + i + ".nameKey");
        String categoryName = consoleManagerI18n.getString(categoryNameKey);
        String categoriesString = prefs.getString("preferences.categories." + i + ".ids");
        StringTokenizer categoriesTokenizer = new StringTokenizer(categoriesString, ";");

        String [] categories = new String[categoriesTokenizer.countTokens()];
        for (int categoryIndex = 0; categoryIndex < categories.length; categoryIndex++)
          categories[categoryIndex] = categoriesTokenizer.nextToken();

        categoryPanel = new CategoryPanel(categoryName, categories);
      }

      addCategoryPanel(categoryPanel);
    }


  }



  /**
   * <P>Creates a custom CategoryPanel with the specified id. This is used to
   * enable subclasses to create their own, custom, server specific (or
   * otherwise specific) panels. In order to add such a panel, mark the panel
   * as custom (in plugin properties) and give it an ID. Then override this
   * method in the subclass and check the ID when invoked. When the given ID 
   * matches the one you specified in the properties, return your custom
   * <code>CategoryPanel</code>. If the ID does not match, simply invoke the
   * superclass' method and return whatever it returns.
   */

  protected CategoryPanel createCustomCategoryPanel(String id){
    return null;
  }



  /**
   * Creates and returns the <code>ColorChooser</code> used for choosing
   * the color of the selection.
   */

  protected ColorChooser createSelectionColorButton(){
    ColorChooser button = I18n.get(TextPrefsPanel.class).createColorChooser("selectionColorChooser");
    button.setColor(prefs.getColor("output-selection", UIManager.getColor("textHighlight")));

    return button;
  }



  /**
   * Creates and returns the <code>ColorChooser</code> used for choosing
   * the color of selected text.
   */

  protected ColorChooser createSelectedColorButton(){
    ColorChooser button = I18n.get(TextPrefsPanel.class).createColorChooser("selectedTextColorChooser");
    button.setColor(prefs.getColor("output-selected", UIManager.getColor("textHighlightText")));

    return button;
  }



  /**
   * Lays out all the components as necessary.
   */

  protected void createLayout(){
    setLayout(new BorderLayout(10, 10));

    final JList categoryList = new JList(categoryPanels);
    categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(categoryList);

    JPanel listPanel = new JPanel(new BorderLayout(2, 2));
    JLabel textTypeLabel = I18n.get(TextPrefsPanel.class).createLabel("textTypeLabel");
    textTypeLabel.setHorizontalAlignment(JLabel.CENTER);
    textTypeLabel.setLabelFor(categoryList);
    listPanel.add(textTypeLabel, BorderLayout.NORTH);
    listPanel.add(scrollPane, BorderLayout.CENTER);

    categoryList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        CategoryPanel selectedValue = (CategoryPanel)categoryList.getSelectedValue();
        setCurrentPanel(selectedValue);
      }
    });

    add(listPanel, BorderLayout.WEST);
    add(categoryPanelHolder, BorderLayout.CENTER);

    // The call propagates to the registered selection listener which sets the current panel properly
    categoryList.setSelectedIndex(0);
  }



  /**
   * Sets the currently displayed panel to the specified CategoryPanel.
   */

  private void setCurrentPanel(CategoryPanel panel){
    categoryPanelHolder.removeAll();
    currentCategoryPanel = panel;
    if (currentCategoryPanel != null){
      currentCategoryPanel.createTextStyleChooser();
      TextStyleChooserPanel textStyleChooser = panel.getTextStyleChooser();
      textStyleChooser.addChangeListener(settingsChangeListener);
      
      
      categoryPanelHolder.add(currentCategoryPanel, BorderLayout.CENTER);
      invalidate();
      validate();
      repaint(); // sigh
    }
  }



  /**
   * Applies the changes done by the user.
   */

  public void applyChanges(){
    Preferences consolePrefs = consoleManager.getPrefs();

    Enumeration prefNames = prefs.getPreferenceNames();
    while (prefNames.hasMoreElements()){
      String propertyName = (String)prefNames.nextElement();
      Object propertyValue = prefs.get(propertyName);

      consolePrefs.set(propertyName, propertyValue);
    }

    if (!selectionColorButton.getColor().equals(UIManager.getColor("textHighlight")))
      consolePrefs.setColor("output-selection", selectionColorButton.getColor());
    
    if (!selectedColorButton.getColor().equals(UIManager.getColor("textHighlightText")))
      consolePrefs.setColor("output-selected", selectedColorButton.getColor());

    consoleManager.refreshFromProperties();
  }



  /**
   * Returns the font used for the specified category.
   */

  protected Font getCategoryFont(String categoryName){
    String fontFamily = (String)prefs.lookup("font-family." + categoryName, "Monospaced");
    int fontSize = ((Integer)prefs.lookup("font-size." + categoryName, new Integer(14))).intValue();
    int fontStyle = 0;
    if (((Boolean)prefs.lookup("font-bold." + categoryName, Boolean.FALSE)).booleanValue())
      fontStyle |= Font.BOLD;
    if (((Boolean)prefs.lookup("font-italic." + categoryName, Boolean.FALSE)).booleanValue())
      fontStyle |= Font.ITALIC;

    return new Font(fontFamily, fontStyle, fontSize);
  }



  /**
   * Sets the value of the given property type in the given category to the
   * specified value. If the property is marked as unmodifiable, the call is
   * silently ignored.
   */

  protected void setProperty(String categoryName, String propertyType, Object propertyValue){
    String propertyName = (categoryName.equals("") ? propertyType : propertyType + "." + categoryName);
    if (prefs.getBool(propertyName + ".unmodifiable", false))
      return;

    prefs.set(propertyName, propertyValue);
  }



  /**
   * An extension of JPanel which also holds category information.
   */

  protected class CategoryPanel extends JPanel{

    

    /**
     * The long name of the category.
     */

    private final String categoryName;



    /**
     * The TextStyleChooserPanel in this panel.
     */

    private TextStyleChooserPanel textStyleChooser = null;



    /**
     * The IDs (short names) of categories that need to be updated when the
     * settings of the text style chooser change.
     */

    private final String [] categories;
    
    

    /**
     * Creates a new CategoryPanel with the specified long category name,
     * the TextStyleChooserPanel in this panel and a list of category IDs (short
     * names) that need to be modified when the TextStyleChooserPanel's settings
     * change. The "main" category, which is the category used to display the
     * settings is the first element (at index zero) in the categories array.
     */

    public CategoryPanel(String categoryName, TextStyleChooserPanel textStyleChooser,
        String [] categories){
      this.categoryName = categoryName;
      this.textStyleChooser = textStyleChooser;
      this.categories = categories;
    }
    
    
    
    /**
     * Creates a new CategoryPanel with the specified long category name,
     * a list of category IDs (short names) that need to be modified when the
     * TextStyleChooserPanel's settings change and a lazily created
     * TextStyleChooserPanel. The "main" category, which is the category used to
     * display the settings is the first element (at index zero) in the
     * categories array.
     */
     
    public CategoryPanel(String categoryName, String [] categories){
      this.categoryName = categoryName;
      this.categories = categories;
    }



    /**
     * Returns the TextStyleChooser in this panel, or <code>null</code> if it
     * hasn't been created yet.
     */

    public TextStyleChooserPanel getTextStyleChooser(){
      return textStyleChooser;
    }
    
    
    
    /**
     * Creates the TextStyleChooserPanel.
     */
     
    public void createTextStyleChooser(){
      if (textStyleChooser == null){
        String mainCategory = getMainCategory();
        
        Color bg = prefs.getColor("background");
        boolean antialias = prefs.getBool("output-text.antialias", false);
        Font font = getCategoryFont(mainCategory);
        Color fg = (Color)prefs.lookup("foreground." + mainCategory, Color.white);
        
        textStyleChooser = new TextStyleChooserPanel(font, fg, bg, antialias, false, false);
        this.setLayout(new BorderLayout());
        this.add(textStyleChooser, BorderLayout.CENTER);
      }
    }



    /**
     * Returns the IDs (short names) of categories that need to be updated when
     * the text style chooser's settings change.
     */

    public String [] getCategories(){
      return (String [])categories.clone();
    }



    /**
     * Returns the main category. This is the category used to visualize the
     * setting.
     */

    public String getMainCategory(){
      // If you update this, check ChannelTextPrefsPanel.createChannelsCategoryPanel() as well
      // as it assumes the main category is the first element as well.
      return getCategories()[0];
    }



    /**
     * Returns the name specified in the constructor.
     */

    public String toString(){
      return categoryName;
    }

  }

  

}
