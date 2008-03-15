package free.jin.console.icc;

import java.util.List;
import java.util.regex.Pattern;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.ics.IcsCustomConsoleDesignation;

public class ChessclubCustomConsoleDesignation extends IcsCustomConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ChessclubCustomConsoleDesignation</code>
   */
  
  public ChessclubCustomConsoleDesignation(Connection connection, String name,
      String encoding, boolean isConsoleCloseable, List channels,
      Pattern messageRegex, boolean isIncludeShouts, boolean isIncludeCShouts){
    super(connection, name, encoding, isConsoleCloseable, channels,
        messageRegex, isIncludeShouts, isIncludeCShouts);
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  protected void addCShouts(){
    addAccepted("sshout", null, ANY_SENDER);
    
    I18n i18n = I18n.get(ChessclubCustomConsoleDesignation.class);
    
    addCommandType(new AbstractCommandType(i18n.getString("sshout.commandName")){
      protected void send(String userText){
        sendTaggedCommand("sshout " + userText);
      }
    });
  }
  
  
  
}
