/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.jin.console.ics;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.Channel;
import free.jin.console.ChatConsoleDesignation;
import free.jin.console.Console;
import free.jin.event.ChatEvent;



/**
 * A chat console designation which displays a specified set of channels.
 */

public class IcsChannelConsoleDesignation extends ChatConsoleDesignation{
  
  
  
  /**
   * The channels we're displaying.
   */
  
  private final Channel [] channels;
  
  
  
  /**
   * Creates a new <code>IcsChannelConsoleDesignation</code> for the specified
   * set of channels.
   * 
   * @param connection The connection to the server.
   * @param channel The channels to display.
   * @param name The channel/console name.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  public IcsChannelConsoleDesignation(Connection connection, Channel [] channels, String name, String encoding, boolean isConsoleCloseable){
    super(connection, name, encoding, isConsoleCloseable);
    
    this.channels = (Channel [])channels.clone();
    
    for (int i = 0; i < channels.length; i++)
      addAccepted("channel-tell", channels[i].getId(), ANY_SENDER);
    
    addAccepted("announcement", null, ANY_SENDER); // Almost all consoles should display announcements
    
    for (int i = 0; i < channels.length; i++)
      addCommandType(new TellChannelCommandType(this.channels[i]));
  }
  
  
  
  /**
   * Creates a new <code>IcsChannelConsoleDesignation</code> for the specified
   * channel.
   */
  
  public IcsChannelConsoleDesignation(Connection connection, Channel channel, String encoding, boolean isConsoleCloseable){
    this(connection, new Channel[]{channel}, channel.getShortName(), encoding, isConsoleCloseable);
  }
  
  
  
  /**
   * Appends the text for the specified chat event to the console.
   */
  
  protected void appendChat(ChatEvent evt){
    if ("channel-tell".equals(evt.getType())){
      String senderName = evt.getSender().getName();
      String senderTitle = evt.getSenderTitle();
      int senderRating = evt.getSenderRating();
      String message = decode(evt.getMessage());

      String text = 
        senderName +
        (senderTitle == null ? "" : senderTitle) +
        (senderRating == -1 ? "" : "(" + senderRating + ")") +
        ": " +
        message;
      
      Console console = getConsole();
      console.addToOutput(text, console.textTypeForEvent(evt));
    }
    else if ("announcement".equals(evt.getType())){
      I18n i18n = I18n.get(IcsChannelConsoleDesignation.class);
      
      String name = evt.getSender().getName();
      String message = evt.getMessage();
      
      String text = i18n.getFormattedString("announcementPattern", new Object[]{name, message});
      
      Console console = getConsole();
      console.addToOutput(text, console.textTypeForEvent(evt));
    }
    else
      super.appendChat(evt);
  }
  
  
  
  /**
   * A command type which sends the user's message to a specified channel.
   */
  
  private class TellChannelCommandType extends AbstractCommandType{
    
    
    
    /**
     * The channel.
     */
    
    private final Channel channel;
    
    
    
    /**
     * Creates a new <code>TellChannelCommandType</code> for the specified
     * channel.
     */
    
    public TellChannelCommandType(Channel channel){
      super(I18n.get(TellChannelCommandType.class).getFormattedString("name", new Object[]{channel.getShortName()}));
      
      this.channel = channel;
    }
    
    
    
    /**
     * Sends <code>userText</code> as a message to the channel.
     */
    
    protected void send(String userText){
      connection.sendTaggedCommand("xtell " + channel.getId() + " " + userText, getTag());
    }
    
    
    
    /**
     * Does nothing since the server will echo us anyway.
     */
    
    protected void echo(String userText){}
    
    
    
  }

  
  
}
