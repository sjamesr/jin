/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2008 Alexander Maryanovsky.
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

package free.jin.console.icc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.ServerUser;
import free.jin.console.Console;
import free.jin.console.ConsoleDesignation;
import free.jin.console.ics.IcsConsoleManager;
import free.jin.console.ics.IcsCustomConsoleDesignation;
import free.jin.console.ics.IcsPersonalChatConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.ui.PreferencesPanel;


/**
 * An extension of the default ConsoleManager for the chessclub.com server.
 */

public class ChessclubConsoleManager extends IcsConsoleManager{
  
  
  
  /**
   * The list of channels enabled on WCL.
   */
  
  private static final int [] WCL_CHANNELS = new int[]{
    0, 1, 2, 3, 5, 24, 46, 47, 49, 90, 100, 120, 147, 165, 221, 222, 223, 224,
    225, 228, 230, 250, 272, 274, 300, 302, 306, 309, 345, 348, 388,
    393, 394, 395, 396, 399
  };
  
  
  
  /**
   * {@inheritDoc} 
   */
  
  @Override
  protected String getDefaultTextForChat(ChatEvent evt, String encoding){
    String type = evt.getType();
    ServerUser sender = evt.getSender();
    String title = evt.getSenderTitle();
    String message = decode(evt.getMessage(), encoding);
    Object forum = evt.getForum();
    
    if ("qtell".equals(type))
      return ChessclubConsoleManager.parseQTell(evt);
    else if ("channel-qtell".equals(type))
      return ChessclubConsoleManager.parseChannelQTell(evt);
    
    if (evt.getCategory() == ChatEvent.GAME_CHAT_CATEGORY)
      forum = ((Game)forum).getID();

    Object [] args = new Object[]{String.valueOf(sender), title, String.valueOf(forum), message};
    
    I18n i18n = I18n.get(ChessclubConsoleManager.class);
    return i18n.getFormattedString(type + ".displayPattern", args);
  }
  
  
  
  /**
   * Returns whether we're connecting to the WCL server.
   */
  
  public boolean isWcl(){
    return "wcl".equals(getServer().getId());
  }
  
  
  
  /**
   * Returns an ICC-specific system console designation.
   */
  
  @Override
  protected ConsoleDesignation createSystemConsoleDesignation(){
    return new ChessclubSystemConsoleDesignation(getConn(), getEncoding());
  }
  
  
  
  /**
   * Returns an ICC-specific help console designation.
   */
  
  @Override
  protected ConsoleDesignation createHelpConsoleDesignation(boolean isCloseable){
    return new ChessclubHelpConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Returns an ICC-specific general chat console designation.
   */
  
  @Override
  protected ConsoleDesignation createGeneralChatConsoleDesignation(boolean isCloseable){
    if (isWcl())
      return new WclGeneralChatConsoleDesignation(getConn(), getEncoding(), isCloseable);
    else
      return new IccGeneralChatConsoleDesignation(getConn(), getEncoding(), isCloseable);
//    if (isWcl()){
//      Channel lobby = (Channel)getChannels().get(new Integer(250));
//      return new IcsChannelConsoleDesignation(getConn(), lobby, getEncoding(), isCloseable);
//    }
//    else
//      return new ShoutChatConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Creates an ICC-specific personal chat console designation.
   */
  
  @Override
  protected ConsoleDesignation createPersonalChatConsoleDesignation(ServerUser user, boolean isCloseable){
    return new IcsPersonalChatConsoleDesignation(getConn(), user, getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Creates an ICC-specific game chat console designation.
   */
  
  @Override
  protected ConsoleDesignation createGameConsoleDesignation(Game game){
    return new ChessclubGameConsoleDesignation(getConn(), game, getEncoding());
  }
  
  
  
  /**
   * Creates a <code>ChessclubConsole</code> with the specified designation.
   */

  @Override
  protected Console createConsole(ConsoleDesignation designation){
    return new ChessclubConsole(this, designation);
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  @Override
  protected IcsCustomConsoleDesignation loadCustomConsoleDesignation(String prefsPrefix,
      String title, String encoding, List channels, Pattern messageRegex,
      boolean includeShouts, boolean includeCShouts){
    return new ChessclubCustomConsoleDesignation(getConn(), title, encoding, false,
        channels, messageRegex, includeShouts, includeCShouts);
  }
  
  
  
  /**
   * Returns the set of ICC/WCL channels.
   */
  
  @Override
  protected SortedMap createChannels(){
    int [] channelsOrder = (int [])getPrefs().lookup("channels.order." + getServer().getId());
    final Map channelsOrderMap = new HashMap();
    for (int i = 0; i < channelsOrder.length; i++)
      channelsOrderMap.put(new Integer(channelsOrder[i]), new Integer(i));
    
    Comparator channelsComparator = new Comparator(){
      @Override
      public int compare(Object arg0, Object arg1){
        Integer i1 = (Integer)arg0;
        Integer i2 = (Integer)arg1;
        
        Integer position1 = (Integer)channelsOrderMap.get(i1);
        Integer position2 = (Integer)channelsOrderMap.get(i2);
        
        if (position1 == null){
          if (position2 == null)
            return i1.intValue() - i2.intValue();
          else
            return 1;
        }
        else{
          if (position2 == null)
            return -1;
          else
            return position1.intValue() - position2.intValue();
        }
      }
    };
    
    boolean isWcl = isWcl();
    SortedMap channels = new TreeMap(channelsComparator);
    
    if (isWcl){
      for (int i = 0; i < WCL_CHANNELS.length; i++){
        int channelNumber = WCL_CHANNELS[i];
        channels.put(new Integer(channelNumber), new WclChannel(channelNumber));
      }
    }
    else{
      for (int i = 0; i < 400; i++)
        channels.put(new Integer(i), new IccChannel(i));
    }
    
    return channels;
  }
  
  
  
  /**
   * Return a <code>PreferencesPanel</code> for changing the console
   * manager's settings.
   */

  @Override
  public PreferencesPanel getPreferencesUI(){
    return new ChessclubConsolePrefsPanel(this);
  }
  
  
  
  /**
   * Returns a string that should be displayed for the given ChatEvent when the
   * ChatEvent contains a qtell.
   */

  public static String parseQTell(ChatEvent evt){
    String message = evt.getMessage();
    int index;
    while ((index = message.indexOf("\\n")) != -1)
      message = message.substring(0, index) + "\n:" + message.substring(index + 2);
    while ((index = message.indexOf("\\h")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\H")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\b")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    return ":" + message;
  }
  
  
  
  /**
   * Returns a string that should be displayed for the given ChatEvent when the
   * ChatEvent contains a channel qtell.
   */

  public static String parseChannelQTell(ChatEvent evt){
    String message = evt.getMessage();
    Object forum = evt.getForum();
    int index;
    while ((index = message.indexOf("\\n")) != -1)
      message = message.substring(0, index) + "\n" + forum + ">" + message.substring(index + 2);
    while ((index = message.indexOf("\\h")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\H")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\b")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    return forum + ">" + message;
  }
  
  
  
  /**
   * ICC no longer allows users to do almost anything, so we conveniently
   * display the help channel for guest users on-login.
   */
  
  @Override
  public void loginSucceeded(Connection conn){
    super.loginSucceeded(conn);
    
    if (getUser().isGuest())
      activateHelpConsole(MAIN_CONTAINER_ID);
  }
  
  
  
}
