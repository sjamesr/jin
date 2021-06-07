/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002 Alexander Maryanovsky. All rights reserved.
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
package free.jin.sound;

import free.jin.Connection;
import free.jin.FriendsConnection;
import free.jin.Game;
import free.jin.Preferences;
import free.jin.ServerUser;
import free.jin.event.BoardFlipEvent;
import free.jin.event.ChatEvent;
import free.jin.event.ChatListener;
import free.jin.event.ClockAdjustmentEvent;
import free.jin.event.ConnectionListener;
import free.jin.event.FriendsEvent;
import free.jin.event.FriendsListener;
import free.jin.event.GameEndEvent;
import free.jin.event.GameListener;
import free.jin.event.GameStartEvent;
import free.jin.event.IllegalMoveEvent;
import free.jin.event.ListenerManager;
import free.jin.event.MoveMadeEvent;
import free.jin.event.OfferEvent;
import free.jin.event.PlainTextEvent;
import free.jin.event.PlainTextListener;
import free.jin.event.PositionChangedEvent;
import free.jin.event.TakebackEvent;
import free.jin.plugin.Plugin;
import free.util.audio.AudioClip;
import free.util.models.BooleanModel;
import free.util.models.Model;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** The plugin responsible for producing sound on all the relevant events. */
public class SoundManager extends Plugin
    implements PlainTextListener,
        ChatListener,
        ConnectionListener,
        GameListener,
        FriendsListener,
        PropertyChangeListener {

  /** Maps sound filenames to AudioClips loaded from those filenames. */
  protected static final Hashtable FILENAMES_TO_AUDIO_CLIPS = new Hashtable();

  /** Maps chat patterns to filenames containing the sound data. */
  protected final Hashtable chatPatternsToFilenames = new Hashtable();

  /** Maps text patterns to filenames containing the sound data. */
  protected final Hashtable textPatternsToFilenames = new Hashtable();

  /** Maps event names such as ("OnConnect") to AudioClips. */
  protected final Hashtable eventsToAudioClips = new Hashtable();

  /** True when the plugin is "on", i.e. sounds are on. */
  protected BooleanModel soundState;

  /**
   * Initializes the state of the plugin from user properties, loads the sounds and registers all
   * the listeners.
   */
  @Override
  public void start() {
    init();
    loadSounds();
    registerListeners();
  }

  /** Undoes what the <code>start()</code> method does. */
  @Override
  public void stop() {
    unregisterListeners();
    unloadSounds();
  }

  /** Returns a boolean model specifying whether sound is on. */
  @Override
  public Model[] getHotPrefs() {
    return new Model[] {soundState};
  }

  /**
   * Initializes the state of the plugin from the state specified in the user properties. This
   * method is called from the start method of the plugin.
   */
  protected void init() {
    soundState =
        new BooleanModel(getI18n().getString("enableSound"), getPrefs().getBool("on", true));
  }

  /**
   * Saves the current state of the plugin into the user properties. This method is called from the
   * start method of the plugin.
   */
  @Override
  public void saveState() {
    getPrefs().setBool("on", soundState.isOn());
  }

  /** Loads all the sounds and maps them to chat patterns. */
  protected void loadSounds() {
    loadPatternSounds("chat", chatPatternsToFilenames);
    loadPatternSounds("text", textPatternsToFilenames);

    loadEventAudioClip("OnConnect");
    loadEventAudioClip("OnLogin");
    loadEventAudioClip("OnDisconnect");

    loadEventAudioClip("Move");
    loadEventAudioClip("Capture");
    loadEventAudioClip("Castling");
    loadEventAudioClip("IllegalMove");
    loadEventAudioClip("GameEnd");
    loadEventAudioClip("GameStart");

    loadEventAudioClip("DrawOffer");
    loadEventAudioClip("AbortOffer");
    loadEventAudioClip("AdjournOffer");
    loadEventAudioClip("TakebackOffer");

    loadEventAudioClip("FriendConnected");
    loadEventAudioClip("FriendDisconnected");
  }

  /**
   * Loads patterns and their corresponding sounds of the given type, mapping the patterns to sound
   * filenames in the given Hashtable.
   */
  private void loadPatternSounds(String type, Hashtable map) {
    Preferences prefs = getPrefs();
    int numPatterns = prefs.getInt("num-" + type + "-patterns", 0);

    for (int i = 0; i < numPatterns; i++) {
      try {
        String filename = prefs.getString(type + "-sound-" + i);
        String pattern = prefs.getString(type + "-pattern-" + i);
        Pattern regex = Pattern.compile(pattern);

        if (!FILENAMES_TO_AUDIO_CLIPS.containsKey(filename)) {
          // Currently all the sounds are located in the same directory as
          // SoundManager, but it would probably be better to put them in the
          // directory of the subclass and load with getClass().getResource()
          // (and maybe with each super class of that too).
          URL url = SoundManager.class.getResource(filename);
          if (url == null) {
            continue;
          }
          FILENAMES_TO_AUDIO_CLIPS.put(filename, new AudioClip(url));
        }

        map.put(regex, filename);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (PatternSyntaxException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Tries to load an AudioClip for the given event and map the event name to the AudioClip in the
   * <code>eventsToAudioClips</code> hashtable. Silently fails if unsuccessful.
   */
  protected final void loadEventAudioClip(String eventName) {
    try {
      String resourceName = getPrefs().getString(eventName, null);
      if (resourceName == null) return;
      URL url = SoundManager.class.getResource(resourceName);
      if (url == null) return;
      eventsToAudioClips.put(eventName, new AudioClip(url));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Registers all the necessary listeners. */
  protected void registerListeners() {
    Connection conn = getConn();
    ListenerManager listenerManager = conn.getListenerManager();

    listenerManager.addPlainTextListener(this);
    listenerManager.addChatListener(this);
    listenerManager.addConnectionListener(this);
    listenerManager.addGameListener(this);

    if (conn instanceof FriendsConnection)
      ((FriendsConnection) conn).getFriendsListenerManager().addFriendsListener(this);
  }

  /** Unregisters all the listeners registered by <code>registerListeners()</code>. */
  protected void unregisterListeners() {
    Connection conn = getConn();
    ListenerManager listenerManager = conn.getListenerManager();

    listenerManager.removePlainTextListener(this);
    listenerManager.removeChatListener(this);
    listenerManager.removeConnectionListener(this);
    listenerManager.removeGameListener(this);
  }

  /** Unloads all the sounds. */
  protected void unloadSounds() {
    chatPatternsToFilenames.clear();
    FILENAMES_TO_AUDIO_CLIPS.clear();
  }

  /** Listens to ChatEvents and makes appropriate sounds. */
  @Override
  public void chatMessageReceived(ChatEvent evt) {
    if (!isOn()) return;

    String type = evt.getType();
    Object forum = evt.getForum();
    ServerUser sender = evt.getSender();
    String chatMessageType =
        type
            + "."
            + (forum == null ? "" : forum.toString())
            + "."
            + (sender == null ? "" : sender.getName());

    Enumeration patterns = chatPatternsToFilenames.keys();
    while (patterns.hasMoreElements()) {
      Pattern regex = (Pattern) patterns.nextElement();
      Matcher matcher = regex.matcher(chatMessageType);
      if (matcher.find()) {
        String filename = (String) chatPatternsToFilenames.get(regex);
        AudioClip clip = (AudioClip) FILENAMES_TO_AUDIO_CLIPS.get(filename);
        clip.play();
      }
    }
  }

  /** Listens to PlainTextEvents and makes appropriate sounds. */
  @Override
  public void plainTextReceived(PlainTextEvent evt) {
    if (!isOn()) return;

    String line = evt.getText();

    Enumeration patterns = textPatternsToFilenames.keys();
    while (patterns.hasMoreElements()) {
      Pattern regex = (Pattern) patterns.nextElement();
      Matcher matcher = regex.matcher(line);
      if (matcher.find()) {
        String filename = (String) textPatternsToFilenames.get(regex);
        AudioClip clip = (AudioClip) FILENAMES_TO_AUDIO_CLIPS.get(filename);
        clip.play();
      }
    }
  }

  /**
   * Plays the event associated with the given event. Currently recognized event names include:
   *
   * <UL>
   *   <LI>OnConnect - A connection was established.
   *   <LI>OnLogin - Login procedure succeeded.
   *   <LI>OnDisconnect - Disconnected.
   *   <LI>Move - A move is made.
   *   <LI>Capture - A capture move is made.
   *   <LI>Castling - A castling move is made.
   *   <LI>IllegalMove - An illegal move was attempted.
   *   <LI>GameStart - A game started.
   *   <LI>GameEnd - A game ended.
   *   <LI>FriendConnected - A buddy logged in.
   *   <LI>FriendDisconnected - A buddy logged out.
   * </UL>
   *
   * Returns true if the given event is recognized, false otherwise. Note that for various reasons
   * (like the user disabling sounds), the sound may not be played.
   */
  public boolean playEventSound(String eventName) {
    AudioClip clip = (AudioClip) eventsToAudioClips.get(eventName);
    if (clip != null) {
      if (isOn()) clip.play();
      return true;
    }

    return false;
  }

  /** Plays the sound mapped to the "OnConnect" event. */
  @Override
  public void connectionEstablished(Connection conn) {
    playEventSound("OnConnect");
  }

  /** Plays the sound mapped to the "OnLogin" event. */
  @Override
  public void loginSucceeded(Connection conn) {
    playEventSound("OnLogin");
  }

  /** Plays the sound mapped to the "OnDisconnect" event. */
  @Override
  public void connectionLost(Connection conn) {
    playEventSound("OnDisconnect");
  }

  // The rest of ConnectionListener's methods.
  @Override
  public void connectingFailed(Connection conn, String reason) {}

  @Override
  public void connectionAttempted(Connection conn, String hostname, int port) {}

  @Override
  public void loginFailed(Connection conn, String reason) {}

  /** Plays the sound mapped to the "GameStart" event. */
  @Override
  public void gameStarted(GameStartEvent evt) {
    Game game = evt.getGame();
    if ((game.getGameType() == Game.MY_GAME) && game.isPlayed()) {
      playEventSound("GameStart");
      game.addPropertyChangeListener(this);
    }
  }

  /**
   * If a game changes mode from played (by the user) to examined, plays the sound mapped to the
   * "GameEnd" event.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    Object src = evt.getSource();
    String propertyName = evt.getPropertyName();
    if (src instanceof Game) {
      Game game = (Game) src;
      if ((game.getGameType() == Game.MY_GAME)
          && "played".equals(propertyName)
          && evt.getOldValue().equals(Boolean.TRUE)
          && evt.getNewValue().equals(Boolean.FALSE)) playEventSound("GameEnd");
    }
  }

  /** Plays the sound mapped to the "IllegalMove" event. */
  @Override
  public void illegalMoveAttempted(IllegalMoveEvent evt) {
    playEventSound("IllegalMove");
  }

  /** Plays the sound mapped to the "GameEnd" event. */
  @Override
  public void gameEnded(GameEndEvent evt) {
    Game game = evt.getGame();
    if ((game.getGameType() == Game.MY_GAME) && (game.isPlayed())) playEventSound("GameEnd");
    game.removePropertyChangeListener(this);
  }

  /** Plays the "DrawOffer", "AbortOffer" or "AdjournOffer" sounds if needed. */
  @Override
  public void offerUpdated(OfferEvent evt) {
    if (evt.isOffered()) {
      switch (evt.getOfferId()) {
        case OfferEvent.DRAW_OFFER:
          playEventSound("DrawOffer");
          break;
        case OfferEvent.ABORT_OFFER:
          playEventSound("AbortOffer");
          break;
        case OfferEvent.ADJOURN_OFFER:
          playEventSound("AdjournOffer");
          break;
        case OfferEvent.TAKEBACK_OFFER:
          playEventSound("TakebackOffer");
          break;
      }
    }
  }

  @Override
  public void moveMade(MoveMadeEvent evt) {}

  @Override
  public void positionChanged(PositionChangedEvent evt) {}

  @Override
  public void takebackOccurred(TakebackEvent evt) {}

  @Override
  public void clockAdjusted(ClockAdjustmentEvent evt) {}

  @Override
  public void boardFlipped(BoardFlipEvent evt) {}

  /** Invoked when a friend connects. */
  @Override
  public void friendConnected(FriendsEvent evt) {
    playEventSound("FriendConnected");
  }

  /** Invoked when a friend disconnects. */
  @Override
  public void friendDisconnected(FriendsEvent evt) {
    playEventSound("FriendDisconnected");
  }

  @Override
  public void friendAdded(FriendsEvent evt) {}

  @Override
  public void friendRemoved(FriendsEvent evt) {}

  @Override
  public void friendStateChanged(FriendsEvent evt) {}

  /**
   * Returns <code>true</code> if the sound is currently on. Returns <code>false</code> otherwise.
   */
  public boolean isOn() {
    return soundState.isOn();
  }

  /** Returns the string "sound". */
  @Override
  public String getId() {
    return "sound";
  }
}
