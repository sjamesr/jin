package free.jin.console.ics;

import free.jin.Connection;
import free.jin.console.SystemConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * A partial system console implementation for ICS-based servers. 
 *
 * @author Maryanovsky Alexander
 */

public abstract class ICSSystemConsoleDesignation extends SystemConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ICSSystemConsoleDesignation</code> with the specified
   * connection and encoding.
   */
  
  public ICSSystemConsoleDesignation(Connection connection, String encoding){
    super(connection, encoding);
  }
  
  
  
  /**
   * Returns whether the specified chat event is a personal tell to the user.
   */
  
  protected boolean isPersonalTell(ChatEvent evt){
    String type = evt.getType();
    return "tell".equals(type) || "say".equals(type) || "ptell".equals(type);
  }
  
  
  
}
