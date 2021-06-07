/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.scripter.icc;

import free.jin.Connection;
import free.jin.event.ChatEvent;
import free.jin.event.ChatListener;
import free.jin.event.ListenerManager;
import free.jin.scripter.Scripter;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A plugin allowing to run user specified commands or code in response to various server events.
 */
public class ChessclubScripter extends Scripter {

  /** The constructor. duh. */
  public ChessclubScripter() {
    registerScriptDispatcher("chat", new ChatScriptDispatcher());
  }

  /** A <code>ScriptDispatcher</code> for <code>ChatEvents</code>. */
  private class ChatScriptDispatcher extends ScriptDispatcher implements ChatListener {

    private final String[] subtypes =
        new String[] {
          "personalTell",
          "partnerTell",
          "qtell",
          "shout",
          "sshout",
          "announcement",
          "channelTell",
          "channelQTell",
          "kibitz",
          "whisper"
        };

    private final Hashtable chatTypesToSubtypeNames = new Hashtable();

    public ChatScriptDispatcher() {
      chatTypesToSubtypeNames.put("tell", subtypes[0]);
      chatTypesToSubtypeNames.put("say", subtypes[0]);
      chatTypesToSubtypeNames.put("ptell", subtypes[1]);
      chatTypesToSubtypeNames.put("qtell", subtypes[2]);
      chatTypesToSubtypeNames.put("atell", subtypes[0]);
      chatTypesToSubtypeNames.put("shout", subtypes[3]);
      chatTypesToSubtypeNames.put("ishout", subtypes[3]);
      chatTypesToSubtypeNames.put("sshout", subtypes[4]);
      chatTypesToSubtypeNames.put("announcement", subtypes[5]);
      chatTypesToSubtypeNames.put("channel-tell", subtypes[6]);
      chatTypesToSubtypeNames.put("channel-atell", subtypes[6]);
      chatTypesToSubtypeNames.put("channel-qtell", subtypes[7]);
      chatTypesToSubtypeNames.put("kibitz", subtypes[8]);
      chatTypesToSubtypeNames.put("whisper", subtypes[9]);
    }

    @Override
    protected String[] getEventSubtypesImpl() {
      return subtypes;
    }

    @Override
    public boolean isSupportedBy(Connection conn) {
      return true;
    }

    @Override
    public void registerForEvent(ListenerManager listenerManager) {
      listenerManager.addChatListener(this);
    }

    @Override
    public void unregisterForEvent(ListenerManager listenerManager) {
      listenerManager.removeChatListener(this);
    }

    @Override
    public void chatMessageReceived(ChatEvent evt) {
      Vector varsVector = new Vector(5);

      Object[] forumVar = calcForumVar(evt);

      varsVector.addElement(new Object[] {"message", evt.getMessage()});
      varsVector.addElement(new Object[] {"tellType", evt.getType()});
      varsVector.addElement(new Object[] {"sender", evt.getSender().getName()});
      varsVector.addElement(new Object[] {"title", evt.getSenderTitle()});
      if (forumVar != null) varsVector.addElement(forumVar);

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, (String) chatTypesToSubtypeNames.get(evt.getType()), vars);
    }

    private Object[] calcForumVar(ChatEvent evt) {
      String type = evt.getType();
      Object forum = evt.getForum();

      if (type.equals("tell")
          || type.equals("say")
          || type.equals("ptell")
          || type.equals("qtell")
          || type.equals("atell")
          || type.equals("shout")
          || type.equals("ishout")
          || type.equals("sshout")
          || type.equals("announcement")) return null;
      else if (type.equals("channel-tell")
          || type.equals("channel-atell")
          || type.equals("channel-qtell")) return new Object[] {"channel", forum};
      else if (type.equals("kibitz") || type.equals("whisper"))
        return new Object[] {"gameNumber", forum};
      else return null;
    }

    @Override
    protected Object[][] getAvailableVars(String[] eventSubtypes) {
      return new Object[][] {
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
