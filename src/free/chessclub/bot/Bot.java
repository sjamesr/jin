/**
 * The chessclub.com connection library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chessclub.com connection library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chessclub.com connection library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chessclub.com connection library; if not, write to the Free
 * Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chessclub.bot;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Hashtable;
import java.io.IOException;
import free.chessclub.level2.Datagram;
import free.chessclub.ChessclubConnection;


/**
 * A prototype chessclub.com bot, which connects to the server and does nothing.
 * Provides a basic framework for a bot.
 */

public class Bot extends ChessclubConnection{



  /**
   * A hashtable mapping command names to CommandHandlers.
   */

  private final Hashtable commandHandlers = new Hashtable();




  /**
   * The main method, duh :-). Creates a new Bot and makes it connect to 
   * the server.
   */

  public static void main(String [] args){
    if (args.length<4){
      showUsage();
      System.exit(0);
    }

    String hostname = args[0];
    int port = Integer.parseInt(args[1]);
    String username = args[2];
    String password = args[3];

    try{
      Bot bot = new Bot(hostname, port, username, password);
      bot.connect();
    } catch (IOException e){
        e.printStackTrace();
        System.exit(0);
      }
  }




  /**
   * Creates a new ServerBot which will connect to the given hostname on the
   * given port and will use the given account and password.
   */

  public Bot(String hostname, int port, String username, String password){
    super(hostname, port, username, password, System.out);

    setDGState(Datagram.DG_PERSONAL_TELL, true); // This lets people talk to us.
  }




  /**
   * Writes out usage information into the standard error stream.
   */

  private static void showUsage(){
    System.out.println("Usage: ");
    System.out.println("  java "+Bot.class.getName()+" hostname port username password");
  }




  /**
   * Registers a CommandHandler for the given command. All commands registered
   * this way are case insensitive. There can be only one command handler per 
   * command, so this method will throw an IllegalStateException if you try to
   * register a CommandHandler for a command for which a CommandHandler was 
   * already registered.
   */

  public void registerCommandHandler(String command, CommandHandler commandHandler){
    command = command.toLowerCase();
    CommandHandler oldCommandHandler = (CommandHandler)commandHandlers.put(command, commandHandler);
    if (oldCommandHandler!=null){
      commandHandlers.put(command, oldCommandHandler);
      throw new IllegalArgumentException("Unable to register another CommandHandler for command: "+command);
    }
  }




  /**
   * Sends the given message to the given player as a personal tell.
   */

  public void sendTell(String player, String message){
    sendCommand("tell "+player+" "+message);
  }




  /**
   * Processes personal tells. If the tell type is TELL, this method parses the
   * message and delegates to processCommand. Otherwise, the tell is ignored.
   */

  protected void processPersonalTell(String playerName, String titles, String message, int tellType){
    if (tellType!=TELL)
      return;

    StringTokenizer tokenizer = new StringTokenizer(message, " \"\'", true);
    String [] tokens = new String[tokenizer.countTokens()];
    int numTokens = 0;

    try{
      while (tokenizer.hasMoreTokens()){
        String token = tokenizer.nextToken();
        if (token.equals("\"")||token.equals("\'")){
          tokens[numTokens++] = tokenizer.nextToken(token);
          tokenizer.nextToken(" \"\'"); // Get rid of the string terminating quote.
        }
        else if (!token.equals(" "))
          tokens[numTokens++] = token;
      }
    } catch (NoSuchElementException e){
        sendTell(playerName, "Unterminated string literal");
        return;
      }

    if (numTokens==0){
      sendTell(playerName, "You must specify a command");
      return;
    }

    String issuedCommand = tokens[0];
    String [] args = new String[numTokens-1];
    System.arraycopy(tokens, 1, args, 0, args.length);

    CommandEvent command = new CommandEvent(this, issuedCommand, args, message, playerName, titles);
    processCommand(command);
  }




  /**
   * Asks the appropriate CommandHandler to handle the command.
   */

  protected void processCommand(CommandEvent command){
    String issuedCommand = command.getCommand();
    CommandHandler commandHandler = (CommandHandler)commandHandlers.get(issuedCommand.toLowerCase());
    if (commandHandler==null)
      sendTell(command.getPlayerName(), "Unknown command: "+issuedCommand);
    else
      commandHandler.handleCommand(command);
  }


}
