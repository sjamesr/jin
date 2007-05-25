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

import java.util.LinkedList;
import java.util.List;

import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.ServerUser;
import free.jin.console.Console;
import free.jin.console.GameConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * The game chat console for ICS-like servers.
 */

public abstract class ICSGameConsoleDesignation extends GameConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ICSGameConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param game The game we're covering.
   * @param encoding The encoding to use for encoding/decoding messages.
   */
  
  public ICSGameConsoleDesignation(Connection connection, Game game, String encoding){
    super(connection, game, encoding, game.getGameType() == Game.ISOLATED_BOARD);
  }
  
  
  
  /**
   * Accepts certain "say" and "tell" chat events, in addition to what the
   * superclass accepts. 
   */
  
  protected boolean accept(JinEvent evt){
    if (super.accept(evt))
      return true;
    
    if (!(evt instanceof ChatEvent))
      return false;
    
    ChatEvent chatEvent = (ChatEvent)evt;
    String type = chatEvent.getType();
    
    ServerUser whitePlayer = connection.userForName(game.getWhiteName());
    ServerUser blackPlayer = connection.userForName(game.getBlackName());
    ServerUser sender = chatEvent.getSender();
    ServerUser me = connection.getUser();
    
    if (("say".equals(type) || "tell".equals(type)) &&
        ((whitePlayer.equals(sender) && blackPlayer.equals(me)) ||
         (blackPlayer.equals(sender) && whitePlayer.equals(me))))
      return true;
    
    return false;
  }
  
  
  
  /**
   * Returns the command to kibitz to a game.
   */
  
  protected abstract String getKibitzToCommand();
    
  
  
  /**
   * Returns the command to whisper to a game.
   */
  
  protected abstract String getWhisperToCommand();
  
  
  
  /**
   * Returns out command types.
   */
  
  public CommandType[] createCommandTypes(){
    List commandTypes = new LinkedList();
    
    if ((game.getGameType() == Game.MY_GAME) && game.isPlayed())
      commandTypes.add(new TellOpponentCommandType());
    
    commandTypes.add(new TellAllCommandType());
    commandTypes.add(new TellObserversCommandType());
    
    return (CommandType [])commandTypes.toArray(new CommandType[commandTypes.size()]);
  }
  
  
  
  /**
   * The command type for the "tell opponent" command type.
   */
  
  private class TellOpponentCommandType extends AbstractCommandType{
    
    
    
    /**
     * The opponent we're talking to.
     */
    
    private final ServerUser opponent;
    
    
    
    /**
     * Creates a new <code>TellOpponentCommand</code>
     */
    
    public TellOpponentCommandType(){
      super(I18n.get(TellOpponentCommandType.class).getString("name"));
      
      this.opponent = game.getUserPlayer().isWhite() ? 
          connection.userForName(game.getBlackName()) :
          connection.userForName(game.getWhiteName());
    }
    
    
    
    /**
     * Sends the message to the opponent.
     */
    
    protected void send(String userText){
      // We don't use "say" because the "say" target might've changed since the
      // game was finished, but we want to keep talking to the same person, and
      // tell always does that, so it's safer.
      connection.sendTaggedCommand("xtell " + opponent.getName() + "! " + userText, getTag());
    }
    
    
    
    /**
     * Echoes the command to the console.
     */
    
    protected void echo(String userText){
      Console console = getConsole();
      console.addToOutput(connection.getUser().getName() + ": " + userText, console.getUserTextType());
    }
    
    
    
  }

  
  
  /**
   * The command type for the "tell all" command type.
   */
  
  private class TellAllCommandType extends AbstractCommandType{
    
    
    
    /**
     * Creates a new <code>TellAllCommand</code>
     */
    
    public TellAllCommandType(){
      super(I18n.get(TellAllCommandType.class).getString("name"));
    }
    
    
    
    /**
     * Sends the message to everyone at the board.
     */
    
    protected void send(String userText){
      connection.sendTaggedCommand(getKibitzToCommand() + " " + game.getID() + " " + userText, getTag());
    }
    
    
    
    /**
     * Does nothing, since the message will be echoed to us anyway.
     */
    
    protected void echo(String userText){
      
      
    }
    
    
    
  }
  
  
  
  /**
   * The command type for the "tell observers" command type.
   */
  
  private class TellObserversCommandType extends AbstractCommandType{
    
    
    
    /**
     * Creates a new <code>TellObserversCommand</code>
     */
    
    public TellObserversCommandType(){
      super(I18n.get(TellObserversCommandType.class).getString("name"));
    }
    
    
    
    /**
     * Sends the message to all the game observers.
     */
    
    protected void send(String userText){
      connection.sendTaggedCommand(getWhisperToCommand() + " " + game.getID() + " " + userText, getTag());
    }
    
    
    
    /**
     * Echoes the message to the console, but only if we're one of the players
     * (since if we aren't, we'll get it echoed back to us anyway).
     */
    
    protected void echo(String userText){
      ServerUser whitePlayer = connection.userForName(game.getWhiteName());
      ServerUser blackPlayer = connection.userForName(game.getBlackName());
      ServerUser me = connection.getUser();
      if (whitePlayer.equals(me) || blackPlayer.equals(me)){
        Console console = getConsole();
        console.addToOutput(me.getName() + ": " + userText, console.getUserTextType());
      }
    }
    
    
    
  }
  
  
  
}
