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

package free.jin.chessclub.board;

import javax.swing.*;
import free.jin.board.BoardPanel;
import free.jin.plugin.Plugin;
import free.jin.Game;
import free.chess.JBoard;
import free.chess.Player;
import free.jin.chessclub.UserImageInternalFrame;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * Extends BoardPanel to provide chessclub.com specific functionalities.
 */

public class ChessclubBoardPanel extends BoardPanel implements MouseListener{



  /**
   * Creates a new ChessclubBoardPanel with the given Plugin and Game.
   */

  public ChessclubBoardPanel(Plugin plugin, Game game){
    super(plugin, game);

    new PlayerImageChecker(Player.WHITE_PLAYER).start();
    new PlayerImageChecker(Player.BLACK_PLAYER).start();
  }




  /**
   * Overrides BoardPanel.createWhiteLabel(Game) to return a chessclub.com specific
   * version.
   */

  protected JLabel createWhiteLabel(Game game){
    JLabel label = super.createWhiteLabel(game);
    label.setText(game.getWhiteName()+game.getWhiteTitles()+" "+game.getWhiteRating());

    return label;
  }





  /**
   * Overrides BoardPanel.createBlackLabel(Game) to return a chessclub.com specific
   * version.
   */

  protected JLabel createBlackLabel(Game game){
    JLabel label = super.createBlackLabel(game);
    label.setText(game.getBlackName()+game.getBlackTitles()+" "+game.getBlackRating());

    return label;
  }




  /**
   * MouseListener implementation.
   */

  public void mouseEntered(MouseEvent evt){}
  public void mouseExited(MouseEvent evt){}
  public void mousePressed(MouseEvent evt){}
  public void mouseReleased(MouseEvent evt){}




  /**
   * MouseListener implementation. Shows an internal frame with the picture of
   * the player whose label was clicked.
   */ 

  public void mouseClicked(MouseEvent evt){
    Object source = evt.getSource();

    if ((source==whiteLabel)||(source==blackLabel)){
      String name = (source==whiteLabel ? getGame().getWhiteName() : getGame().getBlackName());
      URL url;
      try{
        url = new URL("http://www.chessclub.com/mugshots/"+name+".jpg");
      } catch (MalformedURLException e){
          e.printStackTrace();
          return;
        }

      JInternalFrame frame = new UserImageInternalFrame(url, name);
      frame.setLocation(0,0);
      plugin.getPluginContext().getMainFrame().getDesktop().add(frame, JLayeredPane.PALETTE_LAYER);
      frame.setSize(frame.getPreferredSize());
      frame.setVisible(true);
      frame.toFront();
    }
  }



  /**
   * This thread tries to open the URL of the picture of the given player and
   * if it succeeds, it makes the label representing his name clickable.
   */

  private class PlayerImageChecker extends Thread{


    /**
     * The Player whose image's existance we'll try to determine.
     */

    private final Player player;



    /**
     * Creates a new PlayerImageChecker with the given player's image.
     */

    public PlayerImageChecker(Player player){
      super("PlayerImageChecker");
      this.player = player;
    }



    /**
     * Tries to obtain the image. If an IOException is thrown, we'll assume that
     * no image exists. If everything goes fine, we make the label of that player
     * look like a link and register the ChessclubBoardPanel as a mouse listener.
     */

    public void run(){
      try{
        String playerName = player.isWhite() ? getGame().getWhiteName() : getGame().getBlackName();
        URL url = new URL("http://www.chessclub.com/mugshots/"+playerName+".jpg");
        InputStream in = url.openStream();
        in.close();

        // openStream() didn't throw an exception, so we'll assume it's ok.
        JLabel label = player.isWhite() ? whiteLabel : blackLabel;
        label.setForeground(Color.blue);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(ChessclubBoardPanel.this);
      } catch (IOException e){}
    }


  }

}
