/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

package free.jin.chessclub; // Not in free.jin.chessclub.event because we need
                            // to call package friendly methods in JinChessclubConnection

import free.jin.chessclub.event.*;
import free.jin.event.*;
import free.chessclub.level2.Datagram;
import free.jin.event.BasicListenerManager;


/**
 * A chessclub.com specific extension of BasicListenerManager. Used by
 * <code>JinChessclubConnection</code>.
 */

public class ChessclubListenerManager extends BasicListenerManager{



  /**
   * The source JinChessclubConnection.
   */

  private final JinChessclubConnection source;



  /**
   * Creates a new <code>ChessclubListenerManager</code> with the given source
   * <code>JinChessclubConnection</code>.
   */

  public ChessclubListenerManager(JinChessclubConnection source){
    super(source);

    this.source = source;
  }




  /**
   * Adds the given ChatListener to receive notification when chat related
   * messages arrive from the server.
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
   * Removes the given ChatListener from the list of ChatListeners receiving
   * notification when chat related messages arrive from the server.
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
   * Adds the given GameListener to the list of listeners receiving
   * notifications of GameEvents. This method will accept and handle properly 
   * ChessclubGameListeners (by calling the chessclub specific methods in that
   * interface when needed) as well as regular GameListeners.
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
      source.setStyle(13);
    }
  }



  /**
   * Removes the given GameListener from the list of listeners receiving 
   * notifications of GameEvents. This method can be also used to remove
   * ChessclubGameListeners added via the addGameListener(GameListener) method.
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
      source.setStyle(1);
      source.lastGameListenerRemoved();
    }
  }



  
  /**
   * Overrides BasicJinListenerManager.fireGameEvent to handling firing of
   * chessclub.com specific events.
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
   * Returns true if the given event is a chessclub.com specific GameEvent.
   */

  private boolean isChessclubSpecificEvent(GameEvent evt){
    return (evt instanceof CircleEvent) || (evt instanceof ArrowEvent);
  }





  /**
   * Adds the given SeekListener to the list of listeners receiving notification
   * of SeekEvents.
   */

  public void addSeekListener(SeekListener listener){
    super.addSeekListener(listener);

    if (listenerList.getListenerCount(SeekListener.class) == 1){
      source.addDatagramListener(source, Datagram.DG_SEEK);
      source.addDatagramListener(source, Datagram.DG_SEEK_REMOVED);
    }
    else
      source.notFirstListenerAdded(listener);
  }




  /**
   * Removes the given SeekListener from the list of listeners receiving 
   * notification of SeekEvents.
   */

  public void removeSeekListener(SeekListener listener){
    super.removeSeekListener(listener);

    if (listenerList.getListenerCount(SeekListener.class) == 0){
      source.removeDatagramListener(source, Datagram.DG_SEEK);
      source.removeDatagramListener(source, Datagram.DG_SEEK_REMOVED);

      source.lastSeekListenerRemoved();
    }
  }




  /**
   * Adds the given GameListListener to receive notifications of GameListEvents.
   */

  public void addGameListListener(GameListListener listener){
    super.addGameListListener(listener);

    if (listenerList.getListenerCount(GameListListener.class)==1){
      source.addDatagramListener(source, Datagram.DG_GAMELIST_BEGIN);
      source.addDatagramListener(source, Datagram.DG_GAMELIST_ITEM);
    }
  }




  /**
   * Removes the given GameListListener from the list of listeners receiving
   * notifications of GameListEvents.
   */

  public void removeGameListListener(GameListListener listener){
    super.removeGameListListener(listener);

    if (listenerList.getListenerCount(GameListListener.class)==0){
      source.removeDatagramListener(source, Datagram.DG_GAMELIST_BEGIN);
      source.removeDatagramListener(source, Datagram.DG_GAMELIST_ITEM);
    }
  }




  /**
   * Adds the given ChessEventListener to the list of listeners receiving
   * notifications when an event is added.
   */

  public void addChessEventListener(ChessEventListener listener){
    listenerList.add(ChessEventListener.class, listener);

    if (listenerList.getListenerCount(ChessEventListener.class)==1){
      source.addDatagramListener(source, Datagram.DG_TOURNEY);
      source.addDatagramListener(source, Datagram.DG_REMOVE_TOURNEY);
    }
  }



  /**
   * Removes the given ChessEventListener from the list of listeners receiving
   * notifications when an event is added.
   */

  public void removeChessEventListener(ChessEventListener listener){
    listenerList.remove(ChessEventListener.class, listener);

    if (listenerList.getListenerCount(ChessEventListener.class)==0){
      source.removeDatagramListener(source, Datagram.DG_TOURNEY);
      source.removeDatagramListener(source, Datagram.DG_REMOVE_TOURNEY);
    }
  }




  /**
   * Dispatches the given ChessEventEvent to all interested listeners.
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



}
