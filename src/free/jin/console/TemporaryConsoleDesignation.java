package free.jin.console;

import free.jin.Connection;
import free.jin.event.JinEvent;



/**
 * A console designation which, when added, issues a specified list of commands
 * and displays their output, without allowing further interaction with the
 * user.
 * 
 * @author Maryanovsky Alexander
 */

public class TemporaryConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The number of temporary console designations in the program.
   */
  
  private static int temporaryConsoleCount = 0;
  
  
  
  /**
   * Our index among the temporary console designations in the program.
   */
  
  private final int index;
  
  
  
  /**
   * The list of commands to send.
   */
  
  private final String [] commands;
  
  
  
  /**
   * Creates a new <code>TemporaryConsoleDesignation</code> which, when added
   * will issue the specified list of commands.
   */
  
  public TemporaryConsoleDesignation(Connection connection, String name, String encoding, boolean isCloseable, String [] commands){
    super(connection, name, encoding, isCloseable);
    
    this.commands = (String [])commands.clone();
    
    synchronized(TemporaryConsoleDesignation.class){
      index = ++temporaryConsoleCount;
    }
  }
  
  
  
  /**
   * Sends our commands to the server.
   */
  
  public void setConsole(Console console){
    super.setConsole(console);
    
    for (int i = 0; i < commands.length; i++)
      connection.sendTaggedCommand(commands[i], getTag());
  }
  
  
  
  /**
   * Returns our tag.
   */
  
  public String getTag(){
    return "tmp-" + index;
  }
  
  
  
  /**
   * Accepts only events tagged by us.
   */
  
  protected boolean accept(JinEvent evt){
    return isTaggedByUs(evt);
  }
  
  
  
}
