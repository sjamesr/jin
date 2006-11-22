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

package free.jin;

import java.awt.Component;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import free.jin.ui.OptionPanel;
import free.util.AWTUtilities;
import free.util.Localization;
import free.util.Pair;
import free.util.Utilities;
import free.util.swing.ColorChooser;
import free.util.swing.PlainTextDialog;
import free.util.swing.SwingUtils;



/**
 * <p>Responsible for translation of text and other locale sensitive data.
 * 
 * <p>Additionally, it has convenience methods to create certain
 * UI elements and set some of their properties to values specified by the
 * <code>I18n</code> object. These methods define an ad-hoc format for
 * specifying the properties of these UI elements in a resource bundle. 
 */

public class I18n{
  
  
  
  /**
   * A cache of <code>I18n</code> objects.
   */
  
  private static final Map cache = new HashMap(); 
  
  
  
  /**
   * Holds the actual translation.
   */
  
  private final Localization localization;
  
  
  
  /**
   * The name of the class whose information we translate. 
   */
  
  private final String className;
  
  
  
  /**
   * The parent <code>I18n</code> object, which is consulted if the delegate
   * does not have a translation for the requested key.
   */
  
  private final I18n parent;
  
  
  
  /**
   * Creates a new <code>I18n</code> object for the specified class and locale
   * and with the specified parent <code>I18n</code> object.
   */
  
  private I18n(Class requestingClass, Locale locale, I18n parent){
    this.localization = Localization.load(requestingClass, locale);
    this.className = Utilities.getClassName(requestingClass);
    this.parent = parent;
  }
  
  
  
  /**
   * Creates a new <code>I18n</code> object for the specified class and locale. 
   */

  private I18n(Class requestingClass, Locale locale){
    this(requestingClass, locale, null);
  }
  
  
  
  /**
   * Returns an <code>I18n</code> object for the specified class and the current Jin locale.
   */
  
  public static I18n get(Class requestingClass){
    I18n result = (I18n)cache.get(requestingClass);
    
    if (result == null){
      result = new I18n(requestingClass, Jin.getInstance().getLocale());
      cache.put(requestingClass, result);
    }
    
    return result;
  }
  
  
  
  /**
   * Returns an <code>I18n</code> object for the specified class,
   * current Jin locale and with a parent created for its superclass and the same
   * locale. The parent also has a parent created for the superclass'
   * superclass and so on recursively, until (and including) the specified
   * base class. 
   */
  
  public static I18n get(Class requestingClass, Class baseClass){
    if (requestingClass.equals(baseClass))
      return I18n.get(requestingClass);
    
    Pair key = new Pair(requestingClass, baseClass);
    I18n result = (I18n)cache.get(key);
    
    if (result == null){
      result = new I18n(requestingClass, Jin.getInstance().getLocale(),
        get(requestingClass.getSuperclass(), baseClass));
      cache.put(key, result);
    }
    
    return result;
  }
  
  
  
  /**
   * Returns the translation for the specified key.
   */
  
  public String getString(String key) throws MissingResourceException{
    String result = getString(key, null);
    if (result != null)
      return result;
    else
      throw new MissingResourceException("Missing i18n resource \"" + key + "\" for class " + className, className, key);
  }
  
  
  
  /**
   * Returns the translation for the specified key, or the specified default value if there is no translation.
   */
  
  public String getString(String key, String defaultValue){
    String result = null;
    if (localization != null)
      result = localization.getString(key);
    
    if (result != null)
      return result;
    else
      return parent == null ? defaultValue : parent.getString(key, defaultValue);
  }
  
  
  
  /**
   * Returns the string obtained by treating the translation for the specified
   * key as a pattern and inserting <code>args</code> into the appropriate
   * locations into it.
   */
  
  public String getFormattedString(String key, Object [] args) throws MissingResourceException{
    return formatMessage(getString(key), args);
  }
  
  
  
  /**
   * Returns the translation for the specified key, parsed as an integer.
   */
  
  public int getInt(String key) throws MissingResourceException{
    return Integer.parseInt(getString(key));
  }
  
  
  
  /**
   * Returns the translation for <code>key</code>, parsed as an integer.
   * If there is no translation <code>key</code>, returns <code>defaultValue</code>.
   */
  
  public int getInt(String key, int defaultValue){
    String translation = getString(key, null);
    if (translation != null)
      return Integer.parseInt(translation);
    else
      return defaultValue;
  }
  
  
  
  /**
   * Formats the message by inserting the specified message arguments into the
   * appropriate locations and returns the result. If <code>messageArgs</code>
   * is <code>null</code> the specified message is returned as-is.
   */
  
  private static String formatMessage(String message, Object [] messageArgs){
    return messageArgs == null ? message : MessageFormat.format(message, messageArgs);
  }
  
  
  
  /**
   * Creates a <code>JLabel</code> using the specified i18n key.
   */
  
  public JLabel createLabel(String i18nKey){
    JLabel label = new JLabel();
    
    SwingUtils.applyLabelSpec(label, getString(i18nKey + ".text"));    
    
    return label;
  }
  
  
  
  /**
   * Creates a <code>JRadioButton</code> using the specified i18n key.
   */
  
  public JRadioButton createRadioButton(String i18nKey){
    return (JRadioButton)initAbstractButton(new JRadioButton(), i18nKey);
  }

  
  
  /**
   * Creates a <code>JCheckBox</code> using the specified i18n key.
   */
  
  public JCheckBox createCheckBox(String i18nKey){
    return (JCheckBox)initAbstractButton(new JCheckBox(), i18nKey);
  }
  
  
  
  /**
   * Creates a <code>JButton</code> using the specified i18n key.
   */
  
  public JButton createButton(String i18nKey){
    return (JButton)initAbstractButton(new JButton(), i18nKey);
  }
  
  
  
  /**
   * Creates a <code>JMenuItem</code> using the specified i18n key.
   */
  
  public JMenuItem createMenuItem(String i18nKey){
    return createMenuItem(i18nKey, null);
  }
  
  
  
  /**
   * Creates a <code>JMenuItem</code> using the specified i18n key.
   * The text of the menu item is treated as a pattern and
   * <code>textArgs</code>, if not <code>null</code> are inserted at the
   * appropriate locations.
   */
  
  public JMenuItem createMenuItem(String i18nKey, Object [] textArgs){
    JMenuItem menuItem = new JMenuItem();
    
    String labelSpec = getString(i18nKey + ".text");
    
    labelSpec = formatMessage(labelSpec, textArgs);
    
    // This doesn't work right if textArgs contains ampersands
    SwingUtils.applyLabelSpec(menuItem, labelSpec);
    
    return menuItem;
    
  }
  
  
  
  /**
   * Creates a <code>ColorChooser</code> from the specified i18n key.
   */
  
  public ColorChooser createColorChooser(String i18nKey){
    ColorChooser colorChooser = new ColorChooser();
    
    SwingUtils.applyLabelSpec(colorChooser, getString(i18nKey + ".text"));
    colorChooser.setToolTipText(getString(i18nKey + ".tooltip", null));
    
    return colorChooser;
  }

  
  
  /**
   * Initializes the specified <code>AbstractButton</code> from the specified
   * i18n key and with the specified initial state.
   * This method is useful if you have a custom <code>AbstractButton</code>,
   * and so can't use the <code>create</code> methods.
   */
  
  public AbstractButton initAbstractButton(AbstractButton button, String i18nKey){
    SwingUtils.applyLabelSpec(button, getString(i18nKey + ".text"));
    button.setToolTipText(getString(i18nKey + ".tooltip", null));
    
    return button;
  }
  
  
  
  /**
   * Creates a titled border with the specified i18n key.
   */
  
  public TitledBorder createTitledBorder(String i18nKey){
    String title = getString(i18nKey + ".title");
    
    return BorderFactory.createTitledBorder(title);
  }



  /**
   * Obtains the title of the <code>OptionPanel</code> with the specified i18n key.
   */
  
  private String getOptionPanelTitle(String i18nKey){
    return getString(i18nKey + ".title");
  }



  /**
   * Obtains the message of the <code>OptionPanel</code> with the specified i18n key.
   * The message string is treated as a pattern and <code>args</code>, if not
   * <code>null</code> are inserted at the appropriate locations.
   */
  
  private String getOptionPanelMessage(String i18nKey, Object [] args){
    String message = getString(i18nKey + ".message");
    
    if (args != null)
      message = formatMessage(message, args);
    
    return message;
  }



  /**
   * <p>Creates and shows an error panel.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key. The panel's message
   * is treated as a pattern and <code>messageArgs</code>, if not
   * <code>null</code> are inserted at the appropriate locations.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public void error(String i18nKey, Component hintParent, Object [] messageArgs){
    String title = getOptionPanelTitle(i18nKey);
    String message = getOptionPanelMessage(i18nKey, messageArgs);
    
    OptionPanel.error(title, message, hintParent);
  }



  /**
   * <p>Creates and shows an error panel.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained  using the specified i18n key.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public void error(String i18nKey, Component hintParent){
    error(i18nKey, hintParent, null);
  }



  /**
   * <p>Creates and shows an error panel.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * The panel's message is treated as a pattern
   * and <code>messageArgs</code>, if not <code>null</code> are inserted at the
   * appropriate locations.
   */
  
  public void error(String i18nKey, Object [] messageArgs){
    error(i18nKey, null, messageArgs);
  }



  /**
   * <p>Creates and shows an error panel.
   * <p>The information required to display the panel (such as the title and 
   * the message) using the specified i18n key.
   */
  
  public void error(String i18nKey){
    error(i18nKey, null, null);
  }



  /**
   * <p>Creates, displays a confirmation panel and returns the result value.
   * Possible result values are {@link OptionPanel#OK} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * The panel's message is treated as a pattern
   * and <code>messageArgs</code>, if not <code>null</code> are inserted at the
   * appropriate locations.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public Object confirm(Object defaultOption, String i18nKey, Component hintParent, Object [] messageArgs){
    String title = getOptionPanelTitle(i18nKey);
    String message = getOptionPanelMessage(i18nKey, messageArgs);
    
    return OptionPanel.confirm(defaultOption, title, message, hintParent);
  }



  /**
   * <p>Creates, displays a confirmation panel and returns the result value.
   * Possible result values are {@link OptionPanel#OK} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public Object confirm(Object defaultOption, String i18nKey, Component hintParent){
    return confirm(defaultOption, i18nKey, hintParent, null);
  }



  /**
   * <p>Creates, displays a confirmation panel and returns the result value.
   * Possible result values are {@link OptionPanel#OK} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * The panel's message is treated as a pattern
   * and <code>messageArgs</code>, if not <code>null</code> are inserted at the
   * appropriate locations.
   */
  
  public Object confirm(Object defaultOption, String i18nKey, Object [] messageArgs){
    return confirm(defaultOption, i18nKey, null, messageArgs);
  }



  /**
   * <p>Creates, displays a confirmation panel and returns the result value.
   * Possible result values are {@link OptionPanel#OK} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   */
  
  public Object confirm(Object defaultOption, String i18nKey){
    return confirm(defaultOption, i18nKey, null, null);
  }



  /**
   * <p>Creates, displays a yes/no question dialog with the specified arguments and
   * returns the result value. The possible result options are {@link OptionPanel#YES},
   * {@link OptionPanel#NO} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * The panel's message is treated as a pattern
   * and <code>messageArgs</code>, if not <code>null</code> are inserted at the
   * appropriate locations.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public Object question(Object defaultOption, String i18nKey, Component hintParent, Object [] messageArgs){
    String title = getOptionPanelTitle(i18nKey);
    String message = getOptionPanelMessage(i18nKey, messageArgs);
    
    return OptionPanel.question(defaultOption, title, message, hintParent);
  }



  /**
   * <p>Creates, displays a yes/no question dialog with the specified arguments and
   * returns the result value. The possible result options are {@link OptionPanel#YES},
   * {@link OptionPanel#NO} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public Object question(Object defaultOption, String i18nKey, Component hintParent){
    return question(defaultOption, i18nKey, hintParent, null);
  }



  /**
   * <p>Creates, displays a yes/no question dialog with the specified arguments and
   * returns the result value. The possible result options are {@link OptionPanel#YES},
   * {@link OptionPanel#NO} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * The panel's message is treated as a pattern
   * and <code>messageArgs</code>, if not <code>null</code> are inserted at the
   * appropriate locations.
   */
  
  public Object question(Object defaultOption, String i18nKey, Object [] messageArgs){
    return question(defaultOption, i18nKey, null, messageArgs);
  }



  /**
   * <p>Creates, displays a yes/no question dialog with the specified arguments and
   * returns the result value. The possible result options are {@link OptionPanel#YES},
   * {@link OptionPanel#NO} and {@link OptionPanel#CANCEL}.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   */
  
  public Object question(Object defaultOption, String i18nKey){
    return question(defaultOption, i18nKey, null, null);
  }
  
  
  
  /**
   * Displays text in a <code>PlainTextDialog</code>.
   */
  
  public void showPlainTextDialog(String i18nKey, Component hintParent){
    String title = getString(i18nKey + ".title");
    String message = getString(i18nKey + ".message");

    PlainTextDialog textDialog = new PlainTextDialog(hintParent, title, message);
    textDialog.setTextAreaFont(new Font("Monospaced", Font.PLAIN, 12));
    AWTUtilities.centerWindow(textDialog, hintParent);
    textDialog.setVisible(true);
  }
  
  
  
}
