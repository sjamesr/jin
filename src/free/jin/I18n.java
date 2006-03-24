package free.jin;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;

import free.jin.ui.OptionPanel;
import free.util.Utilities;
import free.util.swing.ColorChooser;



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
   * The delegate <code>ResourceBundle</code>.
   */
  
  private final ResourceBundle delegate;
  
  
  
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
    ResourceBundle delegate = null;
    try{
      delegate = getResourceBundle(requestingClass, locale);
    } catch (MissingResourceException e){}
    
    this.delegate = delegate;
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
    return new I18n(requestingClass, Jin.getInstance().getLocale());
  }
  
  
  
  /**
   * Returns an <code>I18n</code> object for the specified class,
   * current Jin locale and with a parent created for its superclass and the same
   * locale. The parent also has a parent created for the superclass'
   * superclass and so on recursively, until (and including) the specified
   * base class. 
   */
  
  public static I18n get(Class requestingClass, Class baseClass){
    Locale locale = Jin.getInstance().getLocale();
    
    if (requestingClass.equals(baseClass))
      return new I18n(requestingClass, locale);
    
    return new I18n(requestingClass, locale,
        get(requestingClass.getSuperclass(), baseClass));
  }
  
  
  
  /**
   * Returns the <code>ResourceBundle</code> for the specified class and locale.
   */
  
  private static ResourceBundle getResourceBundle(Class requestingClass, Locale locale){
    String packageName = Utilities.getPackageName(requestingClass);
    String bundleName = "".equals(packageName) ? "localization" : packageName + "." + "localization"; 
    
    return ResourceBundle.getBundle(bundleName, locale, requestingClass.getClassLoader());
  }
  
  
  
  /**
   * Returns the translation for the specified key.
   */
  
  public String getString(String key) throws MissingResourceException{
    try{
      if (delegate == null)
        throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + key,
            getClass().getName(), key);
      else
        return delegate.getString(key);
    } catch (MissingResourceException e){
        if (parent == null)
          throw e;
        else
          return parent.getString(key);
      }
  }
  
  
  
  /**
   * Returns the translation for the specified key, parsed as an integer.
   */
  
  public int getInt(String key) throws MissingResourceException{
    return Integer.parseInt(getString(key));
  }
  
  
  
  /**
   * Creates a <code>JLabel</code> using the specified i18n key.
   */
  
  public JLabel createLabel(String i18nKey){
    JLabel label = new JLabel();
    
    label.setText(getString(i18nKey + ".text"));
    label.setDisplayedMnemonicIndex(getInt(i18nKey + ".displayedMnemonicIndex"));
    
    return label;
  }
  
  
  
  /**
   * Creates a <code>JLabel</code> with no mnemonic using the specified i18n key.
   */
  
  public JLabel createLabelNoMnemonic(String i18nKey){
    JLabel label = new JLabel();
    
    label.setText(getString(i18nKey + ".text"));
    
    return label;
  }
  
  
  
  /**
   * Creates a <code>JRadioButton</code> using the specified i18n key.
   */
  
  public JRadioButton createRadioButton(String i18nKey){
    return (JRadioButton)init(new JRadioButton(), i18nKey);
  }

  
  
  /**
   * Creates a <code>JCheckBox</code> using the specified i18n key.
   */
  
  public JCheckBox createCheckBox(String i18nKey){
    return (JCheckBox)init(new JCheckBox(), i18nKey);
  }
  
  
  
  /**
   * Creates a <code>JButton</code> using the specified i18n key.
   */
  
  public JButton createButton(String i18nKey){
    return (JButton)init(new JButton(), i18nKey);
  }
  
  
  
  /**
   * Creates a <code>JMenuItem</code> using the specified i18n key.
   */
  
  public JMenuItem createMenuItem(String i18nKey){
    JMenuItem menuItem = new JMenuItem();
    
    menuItem.setText(getString(i18nKey + ".text"));
    menuItem.setDisplayedMnemonicIndex(getInt(i18nKey + ".displayedMnemonicIndex"));
    
    return menuItem;
  }
  
  
  
  /**
   * Creates a <code>ColorChooser</code> from the specified i18n key.
   */
  
  public ColorChooser createColorChooser(String i18nKey){
    ColorChooser colorChooser = new ColorChooser(getString(i18nKey + ".text"));
    colorChooser.setDisplayedMnemonicIndex(getInt(i18nKey + ".displayedMnemonicIndex"));
    
    String tooltipText = getString(i18nKey + ".tooltip");
    if (!"".equals(tooltipText))
      colorChooser.setToolTipText(tooltipText);
    
    return colorChooser;
  }

  
  
  /**
   * Initializes the specified <code>AbstractButton</code> from the specified
   * i18n key and with the specified initial state.
   */
  
  private AbstractButton init(AbstractButton button, String i18nKey){
    button.setText(getString(i18nKey + ".text"));
    button.setDisplayedMnemonicIndex(getInt(i18nKey + ".displayedMnemonicIndex"));
    
    String tooltipText = getString(i18nKey + ".tooltip");
    if (!"".equals(tooltipText))
      button.setToolTipText(tooltipText);
    
    return button;
  }



  /**
   * Obtains the title of the <code>OptionPanel</code> with the specified i18n key.
   */
  
  private String getTitle(String i18nKey){
    return getString(i18nKey + ".title");
  }



  /**
   * Obtains the message of the <code>OptionPanel</code> with the specified i18n key.
   * If <code>args</code> is not null, the value of the message is treated as a
   * pattern and is formatted via <code>MessageFormat.format(message, args)</code>.
   */
  
  private String getMessage(String i18nKey, Object [] args){
    String message = getString(i18nKey + ".message");
    
    if (args != null)
      message = MessageFormat.format(message, args);
    
    return message;
  }



  /**
   * <p>Creates and shows an error panel.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key. The panel's message
   * is treated as a pattern and is formatted via
   * <code>MessageFormat.format(message, messageArgs)</code>.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public void error(String i18nKey, Object [] messageArgs, Component hintParent){
    String title = getTitle(i18nKey);
    String message = getMessage(i18nKey, messageArgs);
    
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
    error(i18nKey, null, hintParent);
  }



  /**
   * <p>Creates and shows an error panel.
   * <p>The information required to display the panel (such as the title and 
   * the message) is obtained using the specified i18n key.
   * The panel's message is treated as a pattern
   * and is formatted via <code>MessageFormat.format(message, messageArgs)</code>.
   */
  
  public void error(String i18nKey, Object [] messageArgs){
    error(i18nKey, messageArgs, null);
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
   * and is formatted via <code>MessageFormat.format(message, messageArgs)</code>.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public Object confirm(Object defaultOption, String i18nKey, Component hintParent, Object [] messageArgs){
    String title = getTitle(i18nKey);
    String message = getMessage(i18nKey, messageArgs);
    
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
   * and is formatted via <code>MessageFormat.format(message, messageArgs)</code>.
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
   * and is formatted via <code>MessageFormat.format(message, messageArgs)</code>.
   * <p><code>hintParent</code> specifies the component over which the error
   * message should be displayed. This is optional and may be <code>null</code>.
   */
  
  public Object question(Object defaultOption, String i18nKey, Component hintParent, Object [] messageArgs){
    String title = getTitle(i18nKey);
    String message = getMessage(i18nKey, messageArgs);
    
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
   * and is formatted via <code>MessageFormat.format(message, messageArgs)</code>.
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
  
  
  
}
