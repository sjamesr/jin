package free.jin.console;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * A <code>ConsoleDesignation</code> which accepts a specified subset of chat
 * events.
 */

public class ChatConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The set of chat types we accept.
   */
  
  private final List acceptedChatTypes = new LinkedList();
  
  
  
  /**
   * Creates a new <code>ChatConsoleDesignation</code> with the specified name
   * and temporary status.
   */
  
  public ChatConsoleDesignation(String name, boolean isConsoleTemporary){
    super(name, isConsoleTemporary);
  }
  
  
  
  /**
   * Make the specified chat event subset accepted. Passing <code>null</code>
   * for an argument means that any value for that chat event property is
   * accepted. For example:
   * <blockquote>addAccepted("channel", new Integer(50), null)</blockquote>
   * accepts all channel 50 tells, while
   * <blockquote>addAccepted(null, null, "AlexTheGreat")</blockquote>
   * accepts all chat events coming from <code>AlexTheGreat</code>.
   */
  
  public void addAccepted(String type, Object forum, String sender){
    acceptedChatTypes.add(new ChatType(type, forum, sender));
  }
  
  
  
  /**
   * Returns whether the specified <code>JinEvent</code> is infact a
   * <code>ChatEvent</code> with one of the accepted types.
   */
  
  public boolean accept(JinEvent evt){
    if (!(evt instanceof ChatEvent))
      return false;
    
    ChatEvent chatEvent = (ChatEvent)evt;
    for (Iterator i = acceptedChatTypes.iterator(); i.hasNext();){
      ChatType chatType = (ChatType)i.next();
      if (chatType.accept(chatEvent))
        return true;
    }
    
    return false;
  }
  
  
  
  
  /**
   * The representation of a subset of chat types.
   */
  
  private static class ChatType{
    
    
    
    /**
     * The actual accepted chat "type", as in <code>ChatEvent.getType()</code>;
     * <code>null</code> stands for "any type". 
     */
    
    private final String type;
    
    
    
    /**
     * The chat forum; <code>null</coe> stands for "any forum".
     */
    
    private final Object forum;
    
    
    
    /**
     * The sender; <code>null</code> stands for "any sender".
     */
    
    private final String sender;
    
    
    
    /**
     * Creates a new <code>ChatType</code> with the specified type (as in
     * <code>ChatEvent.getType()</code>), forum and sender. Passing
     * <code>null</code> for an the argument, means that any value for it will
     * be accepted.
     */
    
    public ChatType(String type, Object forum, String sender){
      this.type = type;
      this.forum = forum;
      this.sender = sender;
    }
    
    
    
    /**
     * Returns whether the specified <code>ChatEvent</code> is of this
     * <code>ChatType</code>.
     */
    
    public boolean accept(ChatEvent evt){
      if ((type != null) && !type.equals(evt.getType()))
        return false;
      
      if ((forum != null) && !forum.equals(evt.getForum()))
        return false;
      
      if ((sender != null) && !sender.equals(evt.getSender()))
        return false;
      
      return true;
    }
    
    
    
  }
  
  
  
}
