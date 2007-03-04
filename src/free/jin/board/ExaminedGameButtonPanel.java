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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.plugin.Plugin;
import free.util.swing.SwingUtils;
import free.workarounds.FixedJPanel;


/**
 * The panel which contains all the action buttons for an examined game of type
 * Game.MY_GAME.
 */

public class ExaminedGameButtonPanel extends FixedJPanel implements ActionListener{



  /**
   * The Plugin we're being used by.
   */

  protected final Plugin plugin;



  /**
   * The Game for which this ExaminedGameButtonPanel is used.
   */

  protected final Game game;



  /**
   * The button which makes the game jump to its beginning.
   */

  protected JButton startButton;
  



  /**
   * The button which makes the game go one ply backward.
   */

  protected JButton backwardButton;




  /**
   * The button which makes the game go one ply forward.
   */

  protected JButton forwardButton;




  /**
   * The button which makes the game jump to its end.
   */

  protected JButton endButton;





  /**
   * Creates a new ExaminedGameButtonPanel. It will be used by the given Plugin
   * for the given Game.
   */

  public ExaminedGameButtonPanel(Plugin plugin, Game game){
    this.plugin = plugin;
    this.game = game;

    init(plugin, game);
  }




  /**
   * Initializes this ExaminedGameButtonPanel. This method calls delegates to
   * {@link #createComponents(Plugin, Game)} and
   * {@link #addComponents(Plugin, Game)}
   */

  protected void init(Plugin plugin, Game game){
    createComponents(plugin, game);
    addComponents(plugin, game);
  }




  /**
   * Creates all the components of this ExaminedGameButtonPanel.
   */

  protected void createComponents(Plugin plugin, Game game){
    startButton = createStartGameButton(plugin, game);
    backwardButton = createBackwardButton(plugin, game);
    forwardButton = createForwardButton(plugin, game);
    endButton = createEndGameButton(plugin, game);
  }
  
  
  
  /**
   * Creates a button with the specified parameters.
   */
  
  protected JButton createButton(String iconName, String tooltipKey, KeyStroke shortcut){
    JButton button = new JButton();
    button.setToolTipText(I18n.get(ExaminedGameButtonPanel.class).getString(tooltipKey));
    button.addActionListener(this);
    button.registerKeyboardAction(this, shortcut, WHEN_IN_FOCUSED_WINDOW); 
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);
    
    Class loader = ExaminedGameButtonPanel.class;
    SwingUtils.makeIconButton(button,
        new ImageIcon(loader.getResource("images/" + iconName + ".png")),
        new ImageIcon(loader.getResource("images/" + iconName + "-rollover.png")),
        new ImageIcon(loader.getResource("images/" + iconName + "-pressed.png"))
    );
    
    return button;
  }
  
  
  
  /**
   * Creates the button which makes the game go its start.
   */

  protected JButton createStartGameButton(Plugin plugin, Game game){
    return createButton("go-first", "gameStartButton.tooltip", 
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  }




  /**
   * Creates the button which makes the game go its end.
   */

  protected JButton createEndGameButton(Plugin plugin, Game game){
    return createButton("go-last", "gameEndButton.tooltip",
        KeyStroke.getKeyStroke(KeyEvent.VK_END, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  }




  /**
   * Creates the button which makes the game go one ply backward.
   */

  protected JButton createBackwardButton(Plugin plugin, Game game){
    return createButton("go-previous", "backwardButton.tooltip",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  }



  /**
   * Creates the button which makes the game go one ply forward.
   */

  protected JButton createForwardButton(Plugin plugin, Game game){
    return createButton("go-next", "forwardButton.tooltip",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  }




  /**
   * Adds all the components to this ExaminedGameButtonPanel.
   */

  protected void addComponents(Plugin plugin, Game game){
    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    
    add(startButton);
    add(backwardButton);
    add(forwardButton);
    add(endButton);
  }




  /**
   * ActionListener implementation. Executes the appropriate command depending
   * on the button that was pressed.
   */

  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();

    Connection conn = plugin.getConn();
    if (source==startButton){
      conn.goToBeginning(game);
    }
    else if (source==endButton){
      conn.goToEnd(game);
    }
    else if (source==forwardButton){
      conn.goForward(game, 1);
    }
    else if (source==backwardButton){
      conn.goBackward(game, 1);
    }
  }



  /**
   * Overrides getMaximumSize() to return the value of getPreferredSize().
   */

  public Dimension getMaximumSize(){
    return getPreferredSize();
  }


}
