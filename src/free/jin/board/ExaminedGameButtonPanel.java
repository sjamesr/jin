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

package free.jin.board;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.KeyStroke;

import free.jin.Game;
import free.jin.I18n;
import free.jin.plugin.Plugin;
import free.util.swing.SwingUtils;
import free.workarounds.FixedJPanel;


/**
 * The panel which contains all the action buttons for an examined game of type
 * Game.MY_GAME.
 */

public final class ExaminedGameButtonPanel extends FixedJPanel{



  /**
   * The Plugin we're being used by.
   */

  private final Plugin plugin;



  /**
   * The Game for which this ExaminedGameButtonPanel is used.
   */

  private final Game game;



  /**
   * The button which makes the game jump to its beginning.
   */

  private final JButton startButton;
  



  /**
   * The button which makes the game go one ply backward.
   */

  private final JButton backwardButton;




  /**
   * The button which makes the game go one ply forward.
   */

  private final JButton forwardButton;




  /**
   * The button which makes the game jump to its end.
   */

  private final JButton endButton;



  /**
   * Creates a new <code>ExaminedGameButtonPanel</code>. It will be used by the
   * given <code>Plugin</code> for the given <code>Game</code>.
   */

  public ExaminedGameButtonPanel(Plugin plugin, Game game){
    this.plugin = plugin;
    this.game = game;
    
    startButton = createStartGameButton();
    backwardButton = createBackwardButton();
    forwardButton = createForwardButton();
    endButton = createEndGameButton();
    
    createUI();
  }




  /**
   * Creates a button with the specified parameters.
   */
  
  private JButton createButton(String iconName, String tooltipKey, KeyStroke shortcut, ActionListener listener){
    JButton button = new JButton();
    button.setToolTipText(I18n.get(ExaminedGameButtonPanel.class).getString(tooltipKey));
    button.addActionListener(listener);
    button.registerKeyboardAction(listener, shortcut, WHEN_IN_FOCUSED_WINDOW); 
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);
    
    Class loader = ExaminedGameButtonPanel.class;
    Image image = Toolkit.getDefaultToolkit().getImage(
        loader.getResource("images/" + iconName + ".png"));

    SwingUtils.makeIconButton(button, image);
    
    return button;
  }
  
  
  
  /**
   * Returns the keyboard modifier used in the keyboard shortcuts for our
   * buttons. 
   */
  
  private static int getButtonKeyModifier(){
    if (SwingUtils.isMacLnF())
      return KeyEvent.CTRL_DOWN_MASK;
    else
      return KeyEvent.ALT_DOWN_MASK;
  }
  
  
  
  /**
   * Creates the button which makes the game go its start.
   */

  private JButton createStartGameButton(){
    return createButton("go-first", "gameStartButton.tooltip", 
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, getButtonKeyModifier()),
        new ActionListener(){
          public void actionPerformed(ActionEvent evt){
            plugin.getConn().goToBeginning(game);
          }
        });
  }




  /**
   * Creates the button which makes the game go its end.
   */

  private JButton createEndGameButton(){
    return createButton("go-last", "gameEndButton.tooltip",
        KeyStroke.getKeyStroke(KeyEvent.VK_END, getButtonKeyModifier()),
        new ActionListener(){
          public void actionPerformed(ActionEvent evt){
            plugin.getConn().goToEnd(game);
          }
        });
  }




  /**
   * Creates the button which makes the game go one ply backward.
   */

  private JButton createBackwardButton(){
    return createButton("go-previous", "backwardButton.tooltip",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, getButtonKeyModifier()),
        new ActionListener(){
          public void actionPerformed(ActionEvent arg0){
            plugin.getConn().goBackward(game, 1);
          }
        });
  }



  /**
   * Creates the button which makes the game go one ply forward.
   */

  private JButton createForwardButton(){
    return createButton("go-next", "forwardButton.tooltip",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, getButtonKeyModifier()),
        new ActionListener(){
          public void actionPerformed(ActionEvent arg0){
            plugin.getConn().goForward(game, 1);
          }
        });
  }




  /**
   * Creates the UI of this <code>ExaminedGameButtonPanel</code>.
   */

  private void createUI(){
    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    
    add(startButton);
    add(backwardButton);
    add(forwardButton);
    add(endButton);
  }



  /**
   * Overrides getMaximumSize() to return the value of getPreferredSize().
   */

  public Dimension getMaximumSize(){
    return getPreferredSize();
  }
  
  
  
}
