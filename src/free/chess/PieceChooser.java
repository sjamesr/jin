/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import free.util.AWTUtilities;


/**
 * A dialog which lets the user choose a Piece from the given set of pieces.
 */

public class PieceChooser extends JComponent implements ActionListener{


  /**
   * Maps JButtons to Pieces.
   */

  private final Hashtable buttonsToPieces = new Hashtable();
  
  
  
  /**
   * Maps pieces to buttons.
   */
   
  private final Hashtable piecesToButtons = new Hashtable();



  /**
   * The selected piece.
   */

  private Piece chosenPiece = null;




  /**
   * Creates a new PieceChooser with the given array of Pieces and the given
   * PiecePainter.
   */

  public PieceChooser(Piece [] pieces, PiecePainter piecePainter){
    int gridSize = (int)Math.round(Math.ceil(Math.sqrt(pieces.length)));
    setLayout(new GridLayout(gridSize, gridSize, 10, 10));
    for (int i=0;i<pieces.length;i++){
      Piece piece = pieces[i];
      JButton button = new JButton(piece.getTypeName(), new PieceIcon(piece, piecePainter, 32, 32));
      button.setVerticalTextPosition(SwingConstants.BOTTOM);
      button.setHorizontalTextPosition(SwingConstants.CENTER);
      button.setRolloverEnabled(true);
      add(button);
      buttonsToPieces.put(button, piece);
      piecesToButtons.put(piece, button);
      button.addActionListener(this);
    }
  }




  /**
   * Returns the selected Piece.
   */

  public Piece getSelectedPiece(){
    return chosenPiece;
  }
  
  
  
  /**
   * Returns the button corresponding to the specified piece.
   */
   
  public JButton buttonForPiece(Piece piece){
    return (JButton)piecesToButtons.get(piece);
  }



  /**
   * Displays a modal dialog over the given Component at the specified
   * coordinates, which lets the user choose from the given set of Pieces which
   * are painted using the given PiecePainter. Returns the piece chosen by the
   * user.
   */

  public static Piece showPieceChooser(Component parentComponent, int x, int y, 
      Piece [] pieces, PiecePainter painter, Piece defaultPiece){
    final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(parentComponent), "Choose a piece", true);
    
    JComponent content = new JPanel(new BorderLayout());
    dialog.setContentPane(content);
    PieceChooser chooser = new PieceChooser(pieces, painter);
    content.add(chooser, BorderLayout.CENTER);

    content.registerKeyboardAction(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        dialog.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    chooser.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        dialog.dispose();
      }
    });

    JButton defaultButton = chooser.buttonForPiece(defaultPiece);
    dialog.getRootPane().setDefaultButton(defaultButton);
    dialog.pack();
    Point parentLoc = parentComponent.getLocationOnScreen();
    // Try to position the default button under the mouse
    int locX = parentLoc.x + x - defaultButton.getX() - defaultButton.getWidth()/2;
    int locY = parentLoc.y + y - defaultButton.getY() - defaultButton.getHeight()/2 - 20; // 20 is what we think is a typical size for the window title.
    
    dialog.setLocation(locX, locY);
    AWTUtilities.forceToScreen(dialog);
    dialog.setVisible(true);
    
    Piece selectedPiece = chooser.getSelectedPiece();
    if (selectedPiece == null) // User closed the dialog...
      selectedPiece = defaultPiece;

    return selectedPiece;
  }



  /**
   * Handles ActionEvents from the piece buttons.
   */

  public void actionPerformed(ActionEvent evt){
    chosenPiece = (Piece)buttonsToPieces.get(evt.getSource());
    fireActionPerformed(evt);
  }




  /**
   * Adds an ActionListener to this PieceChooser. An ActionEvent is fired when
   * a piece is chosen.
   */

  public void addActionListener(ActionListener listener){
    listenerList.add(ActionListener.class, listener);
  }



  /**
   * Removes an ActionListener from this PieceChooser.
   */

  public void removeActionListener(ActionListener listener){
    listenerList.remove(ActionListener.class, listener);
  }




  /**
   * Fires an ActionEvent.
   */

  protected void fireActionPerformed(ActionEvent evt){
    Object [] listeners = listenerList.getListenerList();
    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, evt.getActionCommand(), evt.getModifiers());

    for (int i=0; i<listeners.length; i+=2) {
      if (listeners[i]==ActionListener.class) {
        ((ActionListener)listeners[i+1]).actionPerformed(e);
      }          
    }
  }

}
