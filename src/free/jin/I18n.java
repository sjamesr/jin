package free.jin;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
 * Responsible for translation of text and other locale sensitive data. 
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
  
  public I18n(Class requestingClass, Locale locale, I18n parent){
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

  public I18n(Class requestingClass, Locale locale){
    this(requestingClass, locale, null);
  }
  
  
  
  /**
   * Creates and returns an <code>I18n</code> object for the specified class
   * and locale and with a parent created for its superclass and the same
   * locale. The parent also has a parent created for the superclass'
   * superclass and so on recursively, until (and including) the specified
   * base class. 
   */
  
  public static I18n createI18nHierarchy(Class requestingClass, Class baseClass, Locale locale){
    if (requestingClass.equals(baseClass))
      return new I18n(requestingClass, locale);
    
    return new I18n(requestingClass, locale,
        createI18nHierarchy(requestingClass.getSuperclass(), baseClass, locale));
  }
  
  
  
  /**
   * Returns the <code>ResourceBundle</code> for the specified class and locale.
   */
  
  private static ResourceBundle getResourceBundle(Class requestingClass, Locale locale){
    String className = requestingClass.getName();
    int lastDotIndex = className.lastIndexOf(".");
    String packageName = lastDotIndex == -1 ? "" : className.substring(0, lastDotIndex);
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
  
  
  
}
