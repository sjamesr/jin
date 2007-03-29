package free.jin.console;

import free.jin.event.JinEvent;



/**
 * Defines the types of events a console is willing to display and the types of
 * commands a user might issue through that console.
 */

public interface ConsoleDesignation{
  
  
  
  /**
   * Returns the name of this designation.
   */
  
  String getName();
  
  
  
  /**
   * Returns whether the console should display the specified Jin event.
   */
  
  boolean accept(JinEvent evt);
  
  
  
  /**
   * Returns whether the console is temporary.
   */
  
  boolean isConsoleTemporary();
  
  
  
}
