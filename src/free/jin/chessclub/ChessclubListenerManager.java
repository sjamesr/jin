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

package free.jin.chessclub;

import free.chessclub.level2.Datagram;
import free.jin.chessclub.event.ArrowEvent;
import free.jin.chessclub.event.ChessEventEvent;
import free.jin.chessclub.event.ChessEventListener;
import free.jin.chessclub.event.ChessclubGameListener;
import free.jin.chessclub.event.CircleEvent;
import free.jin.event.BasicListenerManager;
import free.jin.event.ChatListener;
import free.jin.event.FriendsListener;
import free.jin.event.GameEvent;
import free.jin.event.GameListListener;
import free.jin.event.GameListener;
import free.jin.event.MatchOfferListener;
import free.jin.event.SeekListener;


/**
 * A chessclub.com specific extension of <code>BasicListenerManager</code>.
 * Used by <code>JinChessclubConnection</code>.
 */

public class ChessclubListenerManager extends BasicListenerManager{
  
  
  
  /**
   * The source <code>JinChessclubConnection</code>.
   */
  
  private final JinChessclubConnection source;
  
  
  
  /**
   * Creates a new <code>ChessclubListenerManager</code> with the specified
   * source <code>JinChessclubConnection</code>.
   */
  
  public ChessclubListenerManager(JinChessclubConnection source){
    super(source);
    
    this.source = source;
  }
  
  
  
  
  /**
   * Adds the specified <code>ChatListener</code> to receive notifications when
   * chat related messages arrive from the server.
   */
  
  public void addChatListener(ChatListener listener){
    super.addChatListener(listener);
    
    if (listenerList.getListenerCount(ChatListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_PERSONAL_TELL);
      source.addDatagramListener(source, Datagram.DG_PERSONAL_QTELL);
      source.addDatagramListener(source, Datagram.DG_SHOUT);
      source.addDatagramListener(source, Datagram.DG_CHANNEL_TELL);
      source.addDatagramListener(source, Datagram.DG_CHANNEL_QTELL);
      source.addDatagramListener(source, Datagram.DG_KIBITZ);
    }
  }
  
  
  
  /**
   * Removes the given <code>ChatListener</code> from the list of
   * <code>ChatListener</code>s receiving notification when chat related
   * messages arrive from the server.
   */
  
  public void removeChatListener(ChatListener listener){
    super.removeChatListener(listener);
    
    if (listenerList.getListenerCount(ChatListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_PERSONAL_TELL);
      source.removeDatagramListener(source, Datagram.DG_PERSONAL_QTELL);
      source.removeDatagramListener(source, Datagram.DG_SHOUT);
      source.removeDatagramListener(source, Datagram.DG_CHANNEL_TELL);
      source.removeDatagramListener(source, Datagram.DG_CHANNEL_QTELL);
      source.removeDatagramListener(source, Datagram.DG_KIBITZ);
    }
  }
  
  
  
  /**
   * Adds the specified <code>GameListener</code> to the list of listeners
   * receiving notifications of <code>GameEvent</code>s. This method will accept
   * and handle <code>ChessclubGameListener</code>s properly (by calling the
   * chessclub specific methods in that interface when needed) as well as
   * regular <code>GameListener</code>s.
   */
  
  public void addGameListener(GameListener listener){
    super.addGameListener(listener);
    
    if (listenerList.getListenerCount(GameListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_MY_GAME_STARTED);
      source.addDatagramListener(source, Datagram.DG_STARTED_OBSERVING);
      source.addDatagramListener(source, Datagram.DG_ISOLATED_BOARD);
      source.addDatagramListener(source, Datagram.DG_MY_GAME_CHANGE);
      source.addDatagramListener(source, Datagram.DG_MY_GAME_RESULT);
      source.addDatagramListener(source, Datagram.DG_POSITION_BEGIN);
      source.addDatagramListener(source, Datagram.DG_MY_RELATION_TO_GAME);
      source.addDatagramListener(source, Datagram.DG_SEND_MOVES);
      source.addDatagramListener(source, Datagram.DG_MOVE_SMITH);
      source.addDatagramListener(source, Datagram.DG_MOVE_ALGEBRAIC);
      source.addDatagramListener(source, Datagram.DG_IS_VARIATION);
      source.addDatagramListener(source, Datagram.DG_BACKWARD);
      source.addDatagramListener(source, Datagram.DG_TAKEBACK);
      source.addDatagramListener(source, Datagram.DG_ILLEGAL_MOVE);
      source.addDatagramListener(source, Datagram.DG_MSEC);
      source.addDatagramListener(source, Datagram.DG_OFFERS_IN_MY_GAME);
      source.addDatagramListener(source, Datagram.DG_MORETIME);
      source.addDatagramListener(source, Datagram.DG_FLIP);
      source.addDatagramListener(source, Datagram.DG_KNOWS_FISCHER_RANDOM);
      source.addDatagramListener(source, Datagram.DG_ARROW);
      source.addDatagramListener(source, Datagram.DG_UNARROW);
      source.addDatagramListener(source, Datagram.DG_CIRCLE);
      source.addDatagramListener(source, Datagram.DG_UNCIRCLE);
      // Do not add listeners after this one - it marks the end of the
      // datagram changes (see JinChessclubConnection.gameDatagramsStateChanged).

      source.setStyle(13);
    }
  }
  
  
  
  /**
   * Removes the specified <code>GameListener</code> from the list of listeners
   * receiving notifications of <code>GameEvent</code>s. This method can be used
   * to remove <code>ChessclubGameListener</code>s added via the
   * <code>addGameListener(GameListener)</code> method.
   */
  
  public void removeGameListener(GameListener listener){
    super.removeGameListener(listener);
    
    if (listenerList.getListenerCount(GameListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_MY_GAME_STARTED);
      source.removeDatagramListener(source, Datagram.DG_STARTED_OBSERVING);
      source.removeDatagramListener(source, Datagram.DG_ISOLATED_BOARD);
      source.removeDatagramListener(source, Datagram.DG_MY_GAME_CHANGE);
      source.removeDatagramListener(source, Datagram.DG_MY_GAME_RESULT);
      source.removeDatagramListener(source, Datagram.DG_POSITION_BEGIN);
      source.removeDatagramListener(source, Datagram.DG_MY_RELATION_TO_GAME);
      source.removeDatagramListener(source, Datagram.DG_SEND_MOVES);
      source.removeDatagramListener(source, Datagram.DG_MOVE_SMITH);
      source.removeDatagramListener(source, Datagram.DG_MOVE_ALGEBRAIC);
      source.removeDatagramListener(source, Datagram.DG_IS_VARIATION);
      source.removeDatagramListener(source, Datagram.DG_BACKWARD);
      source.removeDatagramListener(source, Datagram.DG_TAKEBACK);
      source.removeDatagramListener(source, Datagram.DG_ILLEGAL_MOVE);
      source.removeDatagramListener(source, Datagram.DG_MSEC);
      source.removeDatagramListener(source, Datagram.DG_OFFERS_IN_MY_GAME);
      source.removeDatagramListener(source, Datagram.DG_MORETIME);
      source.removeDatagramListener(source, Datagram.DG_FLIP);
      source.removeDatagramListener(source, Datagram.DG_KNOWS_FISCHER_RANDOM);
      source.removeDatagramListener(source, Datagram.DG_ARROW);
      source.removeDatagramListener(source, Datagram.DG_UNARROW);
      source.removeDatagramListener(source, Datagram.DG_CIRCLE);
      source.removeDatagramListener(source, Datagram.DG_UNCIRCLE);
      // Do not remove listeners after this one - it marks the end of the
      // datagram changes (see JinChessclubConnection.gameDatagramsStateChanged).
      
      source.setStyle(1);
    }
  }
  
  
  
  /**
   * Overrides <code>BasicJinListenerManager.fireGameEvent</code> to handling
   * firing of chessclub.com specific events.
   */
  
  public void fireGameEvent(GameEvent evt){
    if (!isChessclubSpecificEvent(evt))
      super.fireGameEvent(evt);
    else{
      Object [] listeners = listenerList.getListenerList();
      for (int i = 0; i < listeners.length; i += 2){
        if (listeners[i] == GameListener.class){
          GameListener listener = (GameListener)listeners[i+1];
          try{
            if (listener instanceof ChessclubGameListener){
              ChessclubGameListener chessclubListener = (ChessclubGameListener)listener;
              
              if (evt instanceof CircleEvent){
                CircleEvent cevt = (CircleEvent)evt;
                
                if (cevt.getId() == CircleEvent.CIRCLE_ADDED)
                  chessclubListener.circleAdded(cevt);
                else if (cevt.getId() == CircleEvent.CIRCLE_REMOVED)
                  chessclubListener.circleRemoved(cevt);
              }
              else if (evt instanceof ArrowEvent){
                ArrowEvent aevt = (ArrowEvent)evt;
                
                if (aevt.getId() == ArrowEvent.ARROW_ADDED)
                  chessclubListener.arrowAdded(aevt);
                else if (aevt.getId() == ArrowEvent.ARROW_REMOVED)
                  chessclubListener.arrowRemoved(aevt);
              }
              else
                throw new IllegalArgumentException("Unknown GameEvent type: " + evt.getClass());
            }
          } catch (RuntimeException e){
              e.printStackTrace();
            }
        }
      }
    }
  }
  
  
  
  /**
   * Returns whether the specified event is a chessclub.com specific
   * <code>GameEvent</code>.
   */
  
  private boolean isChessclubSpecificEvent(GameEvent evt){
    return (evt instanceof CircleEvent) || (evt instanceof ArrowEvent);
  }
  
  
  
  /**
   * Adds the specified <code>SeekListener</code> to the list of listeners
   * receiving notification of <code>SeekEvent</code>s.
   */
  
  public void addSeekListener(SeekListener listener){
    super.addSeekListener(listener);
    
    if (listenerList.getListenerCount(SeekListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_SEEK);
      source.addDatagramListener(source, Datagram.DG_SEEK_REMOVED);
      // Do not add listeners after this one - it marks the end of the
      // datagram changes (see JinChessclubConnection.seekDatagramsStateChanged).
    }
  }
  
  
  
  /**
   * Removes the specified <code>SeekListener</code> from the list of listeners
   * receiving notification of <code>SeekEvent</code>s.
   */
  
  public void removeSeekListener(SeekListener listener){
    super.removeSeekListener(listener);
    
    if (listenerList.getListenerCount(SeekListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_SEEK);
      source.removeDatagramListener(source, Datagram.DG_SEEK_REMOVED);
      // Do not remove listeners after this one - it marks the end of the
      // datagram changes (see JinChessclubConnection.seekDatagramsStateChanged).
    }
  }
  
  
  
  /**
   * Adds the specified <code>GameListListener</code> to receive notifications
   * of <code>GameListEvent</code>s.
   */
  
  public void addGameListListener(GameListListener listener){
    super.addGameListListener(listener);
    
    if (listenerList.getListenerCount(GameListListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_GAMELIST_BEGIN);
      source.addDatagramListener(source, Datagram.DG_GAMELIST_ITEM);
    }
  }
  
  
  
  /**
   * Removes the given <code>GameListListener</code> from the list of listeners
   * receiving notifications of <code>GameListEvent</code>s.
   */
  
  public void removeGameListListener(GameListListener listener){
    super.removeGameListListener(listener);
    
    if (listenerList.getListenerCount(GameListListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_GAMELIST_BEGIN);
      source.removeDatagramListener(source, Datagram.DG_GAMELIST_ITEM);
    }
  }
  
  
  
  /**
   * Adds the specified <code>ChessEventListener</code> to the list of listeners
   * receiving notifications when an event is added.
   */
  
  public void addChessEventListener(ChessEventListener listener){
    listenerList.add(ChessEventListener.class, listener);
    
    if (listenerList.getListenerCount(ChessEventListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_TOURNEY);
      source.addDatagramListener(source, Datagram.DG_REMOVE_TOURNEY);
    }
  }
  
  
  
  /**
   * Removes the specified <code>ChessEventListener</code> from the list of
   * listeners receiving notifications when an event is added.
   */
  
  public void removeChessEventListener(ChessEventListener listener){
    listenerList.remove(ChessEventListener.class, listener);
    
    if (listenerList.getListenerCount(ChessEventListener.class)==0){
      source.removeDatagramListener(source, Datagram.DG_TOURNEY);
      source.removeDatagramListener(source, Datagram.DG_REMOVE_TOURNEY);
    }
  }
  
  
  
  /**
   * Dispatches the specified <code>ChessEventEvent</code> to all interested
   * listeners.
   */

  public void fireChessEventEvent(ChessEventEvent evt){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ChessEventListener.class){
        ChessEventListener listener = (ChessEventListener)listeners[i+1];
        try{
          switch (evt.getID()){
            case ChessEventEvent.EVENT_ADDED:
              listener.chessEventAdded(evt);
              break;
            case ChessEventEvent.EVENT_REMOVED:
              listener.chessEventRemoved(evt);
              break;
          }
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

  public void addFriendsListener(FriendsListener listener){
    super.addFriendsListener(listener);

    if (listenerList.getListenerCount(FriendsListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_NOTIFY_ARRIVED);
      source.addDatagramListener(source, Datagram.DG_NOTIFY_LEFT);
      source.addDatagramListener(source, Datagram.DG_NOTIFY_STATE);
      source.addDatagramListener(source, Datagram.DG_MY_NOTIFY_LIST);
      // Do not add listeners after this one - it marks the end of the
      // datagram changes (see JinChessclubConnection.friendsDatagramsStateChanged).
    }
  }
  
  
  
  /**
   * Removes the specified <code>FriendsListener</code> from the list of
   * listeners receiving notifications of <code>FriendsEvent</code>s.
   */
  
  public void removeFriendsListener(FriendsListener listener){
    super.removeFriendsListener(listener);

    if (listenerList.getListenerCount(FriendsListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_NOTIFY_ARRIVED);
      source.removeDatagramListener(source, Datagram.DG_NOTIFY_LEFT);
      source.removeDatagramListener(source, Datagram.DG_NOTIFY_STATE);
      source.removeDatagramListener(source, Datagram.DG_MY_NOTIFY_LIST);
      // Do not remove listeners after this one - it marks the end of the
      // datagram changes (see JinChessclubConnection.friendsDatagramsStateChanged).
    }
  }
  
  
  
  /**
   * Adds the specified <code>MatchOfferListener</code> to receive notifications
   * of <code>MatchOfferEvent</code>s.
   */
  
  public void addMatchOfferListener(MatchOfferListener listener){
    super.addMatchOfferListener(listener);
    
    if (listenerList.getListenerCount(MatchOfferListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_MATCH);
      source.addDatagramListener(source, Datagram.DG_MATCH_REMOVED);
    }
  }
  
  
  
  /**
   * Removes the specified <code>MatchOfferListener</code> from the list of
   * listeners receiving notifications of <code>MatchOfferEvent</code>s.
   */
  
  public void removeMatchOfferListener(MatchOfferListener listener){
    super.removeMatchOfferListener(listener);
    
    if (listenerList.getListenerCount(MatchOfferListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_MATCH);
      source.removeDatagramListener(source, Datagram.DG_MATCH_REMOVED);
    }
  }
  
  
  
}
