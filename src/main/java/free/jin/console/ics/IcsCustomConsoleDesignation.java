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

package free.jin.console.ics;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.CustomConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;
import free.jin.event.PlainTextEvent;



/**
 * An ICS-specific custom console designation.
 * 
 * @author Maryanovsky Alexander
 */

public abstract class IcsCustomConsoleDesignation extends CustomConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>IcsCustomConsoleDesignation</code>.
   */
  
  public IcsCustomConsoleDesignation(Connection connection, String name, String encoding, boolean isConsoleCloseable,
      List channels, Pattern messageRegex, boolean isIncludeShouts, boolean isIncludeCShouts){
    super(connection, name, encoding, isConsoleCloseable, channels, messageRegex);
    
    for (Iterator i = channels.iterator(); i.hasNext();){
      IcsChannel channel = (IcsChannel)i.next();
      addAccepted("channel-tell", channel.getId(), ANY_SENDER);
      addAccepted("channel-qtell", channel.getId(), ANY_SENDER); // FICS doesn't have channel-qtells
      addCommandType(new TellChannelCommandType(channel));
    }
    
    addAccepted("announcement", null, ANY_SENDER); // Almost all consoles should display announcements
    
    if (isIncludeShouts)
      addShouts();
    
    if (isIncludeCShouts)
      addCShouts();
  }
  
  
  
  /**
   * Adds shouts to this designation.
   */
  
  protected void addShouts(){
    addAccepted("shout", null, ANY_SENDER);
    addAccepted("ishout", null, ANY_SENDER);
    
    I18n i18n = I18n.get(IcsCustomConsoleDesignation.class);
    
    addCommandType(new AbstractCommandType(i18n.getString("shout.commandName")){
      @Override
      protected void send(String userText){
        sendCommand("shout " + userText);
      }
    });
      
    addCommandType(new AbstractCommandType(i18n.getString("ishout.commandName")){
      @Override
      protected void send(String userText){
        sendCommand("i " + userText);
      }
    });
  }
  
  
  
  /**
   * Adds c-shouts (sshouts, for ICC) to this designation.
   */
  
  protected abstract void addCShouts();
    
  
  
  /**
   * {@inheritDoc}
   */
  
  @Override
  protected boolean accept(JinEvent evt){
    if (super.accept(evt))
      return true;
    
    if (messageRegex != null){
      String message = null;
      if (evt instanceof PlainTextEvent)
        message = ((PlainTextEvent)evt).getText();
      else if (evt instanceof ChatEvent)
        message = ((ChatEvent)evt).getMessage();
    
      if (messageRegex.matcher(message).matches())
        return true;
    }
    
    return false;
  }
  
  
  
  /**
   * A command type which sends the user's message to a specified channel.
   */
  
  private class TellChannelCommandType extends AbstractCommandType{
    
    
    
    /**
     * The channel we're sending tells to.
     */
    
    private final IcsChannel channel;
    
    
    
    /**
     * Creates a new <code>TellChannelCommandType</code> for the specified
     * channel.
     */
    
    public TellChannelCommandType(IcsChannel channel){
      super(I18n.get(TellChannelCommandType.class).getFormattedString("name", new Object[]{String.valueOf(channel.getId())}));
      
      this.channel = channel;
    }
    
    
    
    /**
     * Sends a channel tell.
     */
    
    @Override
    protected void send(String userText){
      sendCommand("xtell " + channel.getId() + " " + userText);
    }
    
    
    
  }
  
  
  
}
