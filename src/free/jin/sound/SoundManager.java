/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.sound;

import free.util.audio.*;
import free.jin.event.*;
import jregex.*;
import free.jin.plugin.Plugin;
import free.jin.JinConnection;
import free.jin.FriendsJinConnection;
import free.jin.Game;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;
import java.net.URL;
import javax.swing.JMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * The plugin responsible for producing sound on all the relevant events.
 */

public class SoundManager extends Plugin implements ChatListener, ConnectionListener, GameListener, FriendsListener{


  /**
   * Maps chat message patterns to filenames containing the sound data.
   */

  protected final Hashtable patToFilenames = new Hashtable();




  /**
   * Maps sound filenames to AudioClips loaded from those filenames.
   */

  protected final Hashtable filenamesToAudioClips = new Hashtable();



  /**
   * Maps event names such as ("OnConnect") to AudioClips.
   */

  protected final Hashtable eventsToAudioClips = new Hashtable();




  /**
   * True when the plugin is "on", i.e. sounds are on.
   */

  protected boolean isOn;




  /**
   * Initializes the state of the plugin from user properties, loads the sounds
   * and registers all the listeners.
   */

  public void start(){
    init();
    loadSounds();
    registerListeners();
  }




  /**
   * Undoes what the <code>start()</code> method does.
   */

  public void stop(){
    saveState();
    unregisterListeners();
    unloadSounds();
  }





  /**
   * Initializes the state of the plugin from the state specified in the user
   * properties.  This method is called from the start method of the plugin.
   */

  protected void init(){
    isOn = getProperty("on", "true").toLowerCase().equals("true");
  }





  /**
   * Saves the current state of the plugin into the user properties. This method
   * is called from the start method of the plugin.
   */

  protected void saveState(){
    setProperty("on", isOn ? "true" : "false", true);
  }




  /**
   * Creates and returns the JMenu for this plugin.
   */

  public JMenu createPluginMenu(){
    JMenu myMenu = new JMenu(getName());
    
    JRadioButtonMenuItem onMenu = new JRadioButtonMenuItem("Sound on", isOn);
    onMenu.setMnemonic('S');
    onMenu.addChangeListener(new ChangeListener(){
      
      public void stateChanged(ChangeEvent evt){
        isOn = ((JRadioButtonMenuItem)(evt.getSource())).isSelected();
      }
      
    });

    JRadioButtonMenuItem offMenu = new JRadioButtonMenuItem("Sound off", !isOn);
    offMenu.setMnemonic('o');
    offMenu.addChangeListener(new ChangeListener(){
      
      public void stateChanged(ChangeEvent evt){
        isOn = !((JRadioButtonMenuItem)(evt.getSource())).isSelected();
      }
      
    });

    ButtonGroup onOffGroup = new ButtonGroup();
    onOffGroup.add(onMenu);
    onOffGroup.add(offMenu);

    myMenu.add(onMenu);
    myMenu.add(offMenu);

    return myMenu;
  }





  /**
   * Loads all the sounds and maps them to chat patterns.
   */

  protected void loadSounds(){
    int numPatterns = Integer.parseInt(getProperty("num-chat-patterns"));

    for (int i=0;i<numPatterns;i++){
      try{
        String filename = getProperty("chat-sound-"+i);
        String pattern = getProperty("chat-pattern-"+i);
        Pattern regex = new Pattern(pattern);
        
        if (!filenamesToAudioClips.containsKey(filename)){
          URL url = getClass().getResource(filename);
          if (url == null)
            continue;
          filenamesToAudioClips.put(filename, new AudioClip(url));
        }

        patToFilenames.put(regex, filename);
      } catch (IOException e){
          e.printStackTrace();
        }
        catch (PatternSyntaxException e){
          e.printStackTrace();
        }
    }

    loadEventAudioClip("OnConnect");
    loadEventAudioClip("OnLogin");
    loadEventAudioClip("OnDisconnect");

    loadEventAudioClip("Move");
    loadEventAudioClip("Capture");
    loadEventAudioClip("Castling");
    loadEventAudioClip("IllegalMove");
    loadEventAudioClip("GameEnd");
    loadEventAudioClip("GameStart");

    loadEventAudioClip("FriendConnected");
    loadEventAudioClip("FriendDisconnected");
  }




  /**
   * Tries to load an AudioClip for the given event and map the event name to the
   * AudioClip in the <code>eventsToAudioClips</code> hashtable. Silently fails if
   * unsuccessful.
   */

  protected final void loadEventAudioClip(String eventName){
    try{
      String resourceName = getProperty(eventName);
      if (resourceName==null)
        return;
      URL url = SoundManager.class.getResource(resourceName);
      if (url == null)
        return;
      eventsToAudioClips.put(eventName, new AudioClip(url));
    } catch (IOException e){
        e.printStackTrace();
      }
  }



  
  /**
   * Registers all the necessary listeners.
   */

  protected void registerListeners(){
    JinConnection conn = getConnection();
    JinListenerManager listenerManager = conn.getJinListenerManager();

    listenerManager.addChatListener(this);
    listenerManager.addConnectionListener(this);
    listenerManager.addGameListener(this);
    if (conn instanceof FriendsJinConnection)
      ((FriendsJinConnection)conn).getFriendsJinListenerManager().addFriendsListener(this);
  }





  /**
   * Unregisters all the listeners registered by <code>registerListeners()</code>.
   */

  protected void unregisterListeners(){
    JinConnection conn = getConnection();
    JinListenerManager listenerManager = conn.getJinListenerManager();

    listenerManager.removeChatListener(this);
    listenerManager.removeConnectionListener(this);
    listenerManager.removeGameListener(this);
    if (conn instanceof FriendsJinConnection)
      ((FriendsJinConnection)conn).getFriendsJinListenerManager().removeFriendsListener(this);
  }




  /**
   * Unloads all the sounds.
   */

  protected void unloadSounds(){
    patToFilenames.clear();
    filenamesToAudioClips.clear();
  }




  /**
   * Listens to ChatEvents and makes appropriate sounds.
   */

  public void chatMessageReceived(ChatEvent evt){
    if (!isOn())
      return;

    String type = evt.getType();
    Object forum = evt.getForum();
    String sender = evt.getSender();
    String chatMessageType = type+"."+(forum == null ? "" : forum.toString())+"."+sender;

    Enumeration enum = patToFilenames.keys();
    while (enum.hasMoreElements()){
      Pattern regex = (Pattern)enum.nextElement();
      Matcher matcher = regex.matcher(chatMessageType);
      if (matcher.find()){
        String filename = (String)patToFilenames.get(regex);
        AudioClip clip = (AudioClip)filenamesToAudioClips.get(filename);
        clip.play();
      }
    } 
  }





  /**
   * Plays the event associated with the given event.
   * Currently recognized event names include:
   * <UL>
   *   <LI> OnConnect - A connection was established.
   *   <LI> OnLogin - Login procedure succeeded.
   *   <LI> OnDisconnect - Disconnected.
   *   <LI> Move - A move is made.
   *   <LI> Capture - A capture move is made.
   *   <LI> Castling - A castling move is made.
   *   <LI> IllegalMove - An illegal move was attempted.
   *   <LI> GameStart - A game started.
   *   <LI> GameEnd - A game ended.
   *   <LI> FriendConnected - A friend logged on.
   *   <LI> FriendDisconnected - A friend logged off.
   * </UL>
   * Returns true if the given event is recognized, false otherwise. Note that
   * for various reasons (like the user disabling sounds), the sound may not
   * be played.
   */

  public boolean playEventSound(String eventName){
    AudioClip clip = (AudioClip)eventsToAudioClips.get(eventName);
    if (clip!=null){
      if (isOn())
        clip.play();
      return true;
    }

    return false;
  }




  /**
   * Plays the sound mapped to the "OnConnect" event.
   */

  public void connectionEstablished(ConnectionEvent evt){
    playEventSound("OnConnect");
  }




  /**
   * Plays the sound mapped to the "OnLogin" event.
   */

  public void connectionLoggedIn(ConnectionEvent evt){
    playEventSound("OnLogin");
  }




  /**
   * Plays the sound mapped to the "OnDisconnect" event.
   */

  public void connectionLost(ConnectionEvent evt){
    playEventSound("OnDisconnect");
  }



  
  /**
   * Plays the sound mapped to the "GameStart" event.
   */

  public void gameStarted(GameStartEvent evt){
    Game game = evt.getGame();
    if ((game.getGameType() == Game.MY_GAME) && game.isPlayed())
      playEventSound("GameStart");
  }




  /**
   * Plays the sound mapped to the "IllegalMove" event.
   */

  public void illegalMoveAttempted(IllegalMoveEvent evt){
    playEventSound("IllegalMove");
  }




  /**
   * Plays the sound mapped to the "GameEnd" event.
   */

  public void gameEnded(GameEndEvent evt){
    Game game = evt.getGame();
    if ((game.getGameType()==Game.MY_GAME)&&(game.isPlayed()))
      playEventSound("GameEnd");
  }




  public void moveMade(MoveMadeEvent evt){}
  public void positionChanged(PositionChangedEvent evt){}
  public void takebackOccurred(TakebackEvent evt){}
  public void clockAdjusted(ClockAdjustmentEvent evt){}
  public void boardFlipped(BoardFlipEvent evt){}





  /**
   * Called when a friend logs on to the server.
   */

  public void friendConnected(FriendsEvent evt){
    playEventSound("FriendConnected");
  }





  /**
   * Called when a friend logs out from the server.
   */

  public void friendDisconnected(FriendsEvent evt){
    playEventSound("FriendDisconnected");
  }





  public void friendOnline(FriendsEvent evt){}
  public void friendAdded(FriendsEvent evt){}
  public void friendRemoved(FriendsEvent evt){}




  /**
   * Returns <code>true</code> if the sound is currently on. Returns
   * <code>false</code> otherwise.
   */

  public boolean isOn(){
    return isOn;
  }


}
