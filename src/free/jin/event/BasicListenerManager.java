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

package free.jin.event;

import free.jin.Connection;
import free.jin.FriendsConnection;
import free.jin.GameListConnection;
import free.jin.MatchOfferConnection;
import free.jin.SeekConnection;
import free.util.EventListenerList;
import free.util.UnsupportedOperationException;


/**
 * A basic implementation of the <code>ListenerManager</code> interface which
 * uses an <code>EventListenerList</code> to hold the listeners. It also
 * provides convenient event firing methods.
 */

public class BasicListenerManager implements ListenerManager, SeekListenerManager,
    GameListListenerManager, FriendsListenerManager, MatchOfferListenerManager{
  
  
  
  /**
   * The source of the events.
   */
  
  protected final Connection source;
  
  
  
  /**
   * The <code>EventListenerList</code> where we keep all of our listeners.
   */
  
  protected final EventListenerList listenerList = new EventListenerList();
  
  
  
  /**
   * Creates a new <code>BasicListenerManager</code> with the specified source
   * <code>Connection</code>.
   */
  
  public BasicListenerManager(Connection source){
    this.source = source;
  }
  
  
  
  /**
   * Adds the given <code>ConnectionListener</code> to receive connection
   * related events.
   */
  
  @Override
  public void addConnectionListener(ConnectionListener listener){
    listenerList.add(ConnectionListener.class, listener);
  }
  
  
  
  /**
   * Removes the given <code>ConnectionListener</code> from receiving connection
   * events.
   */
  
  @Override
  public void removeConnectionListener(ConnectionListener listener){
    listenerList.remove(ConnectionListener.class, listener);
  }
  
  
  
  /**
   * Notifies all interested <code>Connection</code> listeners that an attempt
   * to connect is being made.
   */
  
  public void fireConnectionAttempted(Connection conn, String hostname, int port){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ConnectionListener.class){
        ConnectionListener listener = (ConnectionListener)listeners[i+1];
        try{
          listener.connectionAttempted(conn, hostname, port);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Notifies all interested <code>Connection</code> listeners that a connection
   * to the server has been established. 
   */
  
  public void fireConnectionEstablished(Connection conn){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ConnectionListener.class){
        ConnectionListener listener = (ConnectionListener)listeners[i+1];
        try{
          listener.connectionEstablished(conn);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Notifies all interested <code>Connection</code> listeners that the attempt
   * to connect failed.
   */
  
  public void fireConnectingFailed(Connection conn, String reason){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ConnectionListener.class){
        ConnectionListener listener = (ConnectionListener)listeners[i+1];
        try{
          listener.connectingFailed(conn, reason);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Notifies all interested <code>Connection</code> listeners that login
   * succeeded.
   */
  
  public void fireLoginSucceeded(Connection conn){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ConnectionListener.class){
        ConnectionListener listener = (ConnectionListener)listeners[i+1];
        try{
          listener.loginSucceeded(conn);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Notifies all interested <code>Connection</code> listeners that login
   * failed.
   */
  
  public void fireLoginFailed(Connection conn, String reason){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ConnectionListener.class){
        ConnectionListener listener = (ConnectionListener)listeners[i+1];
        try{
          listener.loginFailed(conn, reason);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Notifies all interested <code>Connection</code> listeners that the
   * connection to the server was lost.
   */
  
  public void fireConnectionLost(Connection conn){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ConnectionListener.class){
        ConnectionListener listener = (ConnectionListener)listeners[i+1];
        try{
          listener.connectionLost(conn);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the given <code>PlainTextListener</code> to receive notification when
   * otherwise unidentified text arrives from the server.
   */
  
  @Override
  public void addPlainTextListener(PlainTextListener listener){
    listenerList.add(PlainTextListener.class, listener);
  }
  
  
  
  /**
   * Removes the given <code>PlainTextListener</code> from the list of listeners
   * receiving notification when otherwise unidentified text arrives from 
   * the server.
   */
  
  @Override
  public void removePlainTextListener(PlainTextListener listener){
    listenerList.remove(PlainTextListener.class, listener);
  }
  
  
  
  /**
   * Fires the specified <code>PlainTextEvent</code> to all interested listeners.
   */
  
  public void firePlainTextEvent(PlainTextEvent evt){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == PlainTextListener.class){
        PlainTextListener listener = (PlainTextListener)listeners[i+1];
        try{
          listener.plainTextReceived(evt);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the specified <code>ChatListener</code> to receive notification when
   * chat related messages arrive from the server.
   */
  
  @Override
  public void addChatListener(ChatListener listener){
    listenerList.add(ChatListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified <code>ChatListener</code> from the list of
   * <code>ChatListeners</code> receiving notification when chat related
   * messages arrive from the server.
   */
  
  @Override
  public void removeChatListener(ChatListener listener){
    listenerList.remove(ChatListener.class, listener);
  }
  
  
  
  /**
   * Dispatches the specified <code>ChatEvent</code> to all interested
   * listeners.
   */
  
  public void fireChatEvent(ChatEvent evt){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ChatListener.class){
        ChatListener listener = (ChatListener)listeners[i+1];
        try{
          listener.chatMessageReceived(evt);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the specified <code>GameListener</code> to the list of listeners
   * receiving notifications of <code>GameEvent</code>s.
   */
  
  @Override
  public void addGameListener(GameListener listener){
    listenerList.add(GameListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified <code>GameListener</code> from the list of listeners
   * receiving notifications of <code>GameEvent</code>s.
   */
  
  @Override
  public void removeGameListener(GameListener listener){
    listenerList.remove(GameListener.class, listener);
  }
  
  
  
  /**
   * Dispatches the specified <code>GameEvent</code> to all interested
   * listeners.
   */
  
  public void fireGameEvent(GameEvent evt){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == GameListener.class){
        GameListener listener = (GameListener)listeners[i+1];
        try{
          if (evt instanceof GameStartEvent)
            listener.gameStarted((GameStartEvent)evt);
          else if (evt instanceof GameEndEvent)
            listener.gameEnded((GameEndEvent)evt);
          else if (evt instanceof MoveMadeEvent)
            listener.moveMade((MoveMadeEvent)evt);
          else if (evt instanceof PositionChangedEvent)
            listener.positionChanged((PositionChangedEvent)evt);  
          else if (evt instanceof TakebackEvent)
            listener.takebackOccurred((TakebackEvent)evt);
          else if (evt instanceof IllegalMoveEvent)
            listener.illegalMoveAttempted((IllegalMoveEvent)evt);
          else if (evt instanceof ClockAdjustmentEvent)
            listener.clockAdjusted((ClockAdjustmentEvent)evt);
          else if (evt instanceof BoardFlipEvent)
            listener.boardFlipped((BoardFlipEvent)evt);
          else if (evt instanceof OfferEvent)
            listener.offerUpdated((OfferEvent)evt);
          else
            throw new IllegalArgumentException("Unknown GameEvent type: "+evt.getClass());
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the specified <code>SeekListener</code> to the list of listeners
   * receiving notification of <code>SeekEvent</code>s.
   */
  
  @Override
  public void addSeekListener(SeekListener listener){
    if (!(source instanceof SeekConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of SeekConnection");
    
    listenerList.add(SeekListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified <code>SeekListener</code> from the list of listeners
   * receiving notification of <code>SeekEvent</code>s.
   */
  
  @Override
  public void removeSeekListener(SeekListener listener){
    if (!(source instanceof SeekConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of SeekConnection");
    
    listenerList.remove(SeekListener.class, listener);
  }
  
  
  
  /**
   * Fires the specified <code>SeekEvent</code> to all interested
   * <code>SeekListener</code>s.
   */
  
  public void fireSeekEvent(SeekEvent evt){
    if (!(source instanceof SeekConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of SeekConnection");
    
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == SeekListener.class){
        SeekListener listener = (SeekListener)listeners[i+1];
        try{
          switch(evt.getID()){
            case SeekEvent.SEEK_ADDED:
              listener.seekAdded(evt);
              break;
            case SeekEvent.SEEK_REMOVED:
              listener.seekRemoved(evt);
              break;
            default:
              throw new IllegalArgumentException("Unknown SeekEvent ID: "+evt.getID());
          }
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the specified <code>GameListListener</code> to receive notifications
   * of <code>GameListEvent</code>s.
   */
  
  @Override
  public void addGameListListener(GameListListener listener){
    if (!(source instanceof GameListConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of GameListConnection");
    
    listenerList.add(GameListListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified <code>GameListListener</code> from the list of
   * listeners receiving notifications of <code>GameListEvent</code>s.
   */
  
  @Override
  public void removeGameListListener(GameListListener listener){
    if (!(source instanceof GameListConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of GameListConnection");
    
    listenerList.remove(GameListListener.class, listener);
  }
  
  
  
  /**
   * Fires the specified <code>GameListEvent</code> to all interested
   * <code>GameListListener</code>s.
   */
  
  public void fireGameListEvent(GameListEvent evt){
    if (!(source instanceof GameListConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of GameListConnection");
    
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == GameListListener.class){
        GameListListener listener = (GameListListener)listeners[i+1];
        try{
          listener.gameListArrived(evt);
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the specified <code>FriendsListener</code> to the list of listeners
   * receiving notifications of <code>FriendsEvent</code>s.
   */
  
  @Override
  public void addFriendsListener(FriendsListener listener){
    if (!(source instanceof FriendsConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of FriendsConnection");
    
    listenerList.add(FriendsListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified <code>FriendsListener</code> from the list of
   * listeners receiving notifications of <code>FriendsEvent</code>s.
   */
  
  @Override
  public void removeFriendsListener(FriendsListener listener){
    if (!(source instanceof FriendsConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of FriendsConnection");
    
    listenerList.remove(FriendsListener.class, listener);
  }
  
  
  
  /**
   * Dispatches the given <code>FriendsEvent</code> to all interested listeners.
   */
  
  public void fireFriendsEvent(FriendsEvent evt){
    if (!(source instanceof FriendsConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of FriendsConnection");
    
    Object [] listenerList = this.listenerList.getListenerList();
    for (int i = 0; i < listenerList.length; i += 2){
      if (listenerList[i] == FriendsListener.class){
        FriendsListener listener = (FriendsListener)listenerList[i+1];
        try{
          switch (evt.getID()){
            case FriendsEvent.FRIEND_CONNECTED:
              listener.friendConnected(evt);
              break;
            case FriendsEvent.FRIEND_DISCONNECTED:
              listener.friendDisconnected(evt);
              break;
            case FriendsEvent.FRIEND_ADDED:
              listener.friendAdded(evt);
              break;
            case FriendsEvent.FRIEND_REMOVED:
              listener.friendRemoved(evt);
              break;
            case FriendsEvent.FRIEND_STATE_CHANGED:
              listener.friendStateChanged(evt);
              break;
          }
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
  /**
   * Adds the specified <code>MatchOfferListener</code> to receive notifications
   * of <code>MatchOfferEvent</code>s.
   */
  
  @Override
  public void addMatchOfferListener(MatchOfferListener listener){
    if (!(source instanceof MatchOfferConnection))
      throw new UnsupportedOperationException("The source connection is not an instance of MatchOfferConnection");
    
    listenerList.add(MatchOfferListener.class, listener);
  }
  
  
  
  /**
   * Removes the specified <code>MatchOfferListener</code> from the list of
   * listeners receiving notifications of <code>MatchOfferEvent</code>s.
   */
  
  @Override
  public void removeMatchOfferListener(MatchOfferListener listener){
    if (!(source instanceof MatchOfferConnection))
      throw new UnsupportedOperationException("The source connection is not an instance of MatchOfferConnection");
    
    listenerList.remove(MatchOfferListener.class, listener);
  }
  
  
  
  /**
   * Dispatches the specified <code>MatchOfferEvent</code> to all interested
   * listeners.
   */
  
  public void fireMatchOfferEvent(MatchOfferEvent evt){
    if (!(source instanceof MatchOfferConnection))
      throw new UnsupportedOperationException("The source Connection is not an instance of MatchOfferConnection");
    
    Object [] listenerList = this.listenerList.getListenerList();
    for (int i = 0; i < listenerList.length; i += 2){
      if (listenerList[i] == MatchOfferListener.class){
        MatchOfferListener listener = (MatchOfferListener)listenerList[i+1];
        try{
          switch (evt.getID()){
            case MatchOfferEvent.MATCH_OFFER_MADE:
              listener.matchOfferMade(evt);
              break;
            case MatchOfferEvent.MATCH_OFFER_WITHDRAWN:
              listener.matchOfferWithdrawn(evt);
              break;
          }
        } catch (RuntimeException e){
            e.printStackTrace();
          }
      }
    }
  }
  
  
  
}
