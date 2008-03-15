package free.jin.console.fics;

import java.util.List;
import java.util.regex.Pattern;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.ics.IcsCustomConsoleDesignation;



/**
 * A FICS-specific custom console designation.
 * 
 * @author Maryanovsky Alexander
 */

public class FreechessCustomConsoleDesignation extends IcsCustomConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>FreechessCustomConsoleDesignation</code>.
   */
  
  public FreechessCustomConsoleDesignation(Connection connection, String name,
      String encoding, boolean isConsoleCloseable, List channels,
      Pattern messageRegex, boolean isIncludeShouts, boolean isIncludeCShouts){
    super(connection, name, encoding, isConsoleCloseable, channels,
        messageRegex, isIncludeShouts, isIncludeCShouts);
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  protected void addCShouts(){
    addAccepted("cshout", null, ANY_SENDER);
    
    I18n i18n = I18n.get(FreechessCustomConsoleDesignation.class);
    
    addCommandType(new AbstractCommandType(i18n.getString("cshout.commandName")){
      protected void send(String userText){
        sendTaggedCommand("cshout " + userText);
      }
    });
  }
  
}
