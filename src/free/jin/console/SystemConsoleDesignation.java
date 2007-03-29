package free.jin.console;

import free.jin.I18n;
import free.jin.event.JinEvent;



/**
 * The designation of the "System" console.
 */

public class SystemConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>SystemConsoleDesignation</code>.
   */
  
  public SystemConsoleDesignation(){
    super(I18n.get(SystemConsoleDesignation.class).getString("name"), false);
  }
  
  
  
  /**
   * Returns whether the system console should display the specified event.
   */
  
  public boolean accept(JinEvent evt){
    return true;
  }
  
  
  
}
