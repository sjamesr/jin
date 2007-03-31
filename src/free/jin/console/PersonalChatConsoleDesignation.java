package free.jin.console;

import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * A <code>ConsoleDesignation</code> for person-to-person chat.
 */

public class PersonalChatConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The user with whom we're chatting.
   */
  
  private final ServerUser conversationPartner;
  
  
  
  /**
   * Creates a new <code>PersonalChatConsoleDesignation</code> with the
   * specified conversation partner.
   */
  
  public PersonalChatConsoleDesignation(ServerUser conversationPartner, boolean isConsoleTemporary){
    super(conversationPartner.getName(), isConsoleTemporary);
    
    this.conversationPartner = conversationPartner;
  }
  
  
  
  /**
   * Accepts only person-to-person chat events.
   */
  
  public boolean accept(JinEvent evt){
    if (!(evt instanceof ChatEvent))
      return false;
    
    ChatEvent chatEvent = (ChatEvent)evt;
    return (chatEvent.getCategory() == ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY) &&
      chatEvent.getSender().equals(conversationPartner);
  }
  
  
  
}
