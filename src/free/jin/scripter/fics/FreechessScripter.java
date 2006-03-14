/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin.scripter.fics;

import free.jin.scripter.*;
import free.jin.Connection;
import free.jin.event.ChatEvent;
import free.jin.event.ChatListener;
import free.jin.event.ListenerManager;
import java.util.Hashtable;
import java.util.Vector;


/**
 * A plugin allowing to run user specified commands or code in response to
 * various server events.
 */

public class FreechessScripter extends Scripter{



  /**
   * The constructor. duh.
   */

  public FreechessScripter(){
    registerScriptDispatcher("chat", new ChatScriptDispatcher());
  }


  /**
   * A <code>ScriptDispatcher</code> for <code>ChatEvents</code>.
   */

  private class ChatScriptDispatcher extends ScriptDispatcher implements ChatListener{

    private final String [] subtypes = new String[]{"personalTell", "partnerTell", "shout", "tshout", "cshout", "announcement", "channelTell", "kibitz", "whisper", "qtell"}; 

    private final Hashtable chatTypesToSubtypes = new Hashtable();

    public ChatScriptDispatcher(){
      chatTypesToSubtypes.put("tell", subtypes[0]);
      chatTypesToSubtypes.put("say", subtypes[0]);
      chatTypesToSubtypes.put("ptell", subtypes[1]);
      chatTypesToSubtypes.put("shout", subtypes[2]);
      chatTypesToSubtypes.put("ishout", subtypes[2]);
      chatTypesToSubtypes.put("tshout", subtypes[3]);
      chatTypesToSubtypes.put("cshout", subtypes[4]);
      chatTypesToSubtypes.put("announcement", subtypes[5]);
      chatTypesToSubtypes.put("channel-tell", subtypes[6]);
      chatTypesToSubtypes.put("kibitz", subtypes[7]);
      chatTypesToSubtypes.put("whisper", subtypes[8]);
      chatTypesToSubtypes.put("qtell", subtypes[9]);
    }                                          

    protected String [] getEventSubtypesImpl(){return subtypes;}

    public boolean isSupportedBy(Connection conn){return true;}

    public void registerForEvent(ListenerManager listenerManager){
      listenerManager.addChatListener(this);
    }

    public void unregisterForEvent(ListenerManager listenerManager){
      listenerManager.removeChatListener(this);
    }

    public void chatMessageReceived(ChatEvent evt){
      Vector varsVector = new Vector(5);

      Object [] forumVar = calcForumVar(evt);

      varsVector.addElement(new Object[]{"message", evt.getMessage()});
      varsVector.addElement(new Object[]{"tellType", evt.getType()});
      varsVector.addElement(new Object[]{"sender", evt.getSender()});
      varsVector.addElement(new Object[]{"title", evt.getSenderTitle()});
      if (forumVar != null)
        varsVector.addElement(forumVar);

      Object [][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, (String)chatTypesToSubtypes.get(evt.getType()), vars);
    }


    private Object [] calcForumVar(ChatEvent evt){
      String type = evt.getType();
      Object forum = evt.getForum();

      if (type.equals("tell") || type.equals("say") || type.equals("ptell") || 
          type.equals("shout") || type.equals("ishout") || type.equals("tshout") ||
          type.equals("cshout") || type.equals("announcement") || type.equals("qtell"))
        return null;
      else if (type.equals("channel-tell"))
        return new Object[]{"channel", forum};
      else if (type.equals("kibitz") || type.equals("whisper"))
        return new Object[]{"gameNumber", forum};
      else
        return null;
    }

    protected Object [][] getAvailableVars(String [] eventSubtypes){
      return new Object[][]{
        {"message", "Hello World!"},
        {"tellType", "channel-tell"},
        {"sender", "AlexTheGreat"},
        {"title", "C"},
        {"channel", new Integer(107)},
        {"gameNumber", new Integer(100)}
      };
    }

  }


}

