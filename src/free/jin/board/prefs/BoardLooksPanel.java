/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.board.prefs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import free.chess.BoardPainter;
import free.chess.ColoredBoardPainter;
import free.chess.ColoredPiecePainter;
import free.chess.PiecePainter;
import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.board.BoardManager;
import free.jin.board.BoardPattern;
import free.jin.board.JinBoard;
import free.jin.board.PieceSet;
import free.util.AWTUtilities;
import free.util.TableLayout;
import free.util.swing.ColorChooser;
import free.util.swing.PreferredSizedPanel;
import free.util.swing.UrlDisplayingAction;


/**
 * A preferences panel allowing the user to modify the looks of the board. 
 */
 
public class BoardLooksPanel extends BoardModifyingPrefsPanel{
  
  
  
  /**
   * The list holding all the available piece set names. 
   */
   
  protected final JList pieceSets;
  
  
  
  /**
   * The color chooser for the white piece's color.
   */
   
  protected final ColorChooser whiteColor; 
  


  /**
   * The color chooser for the black piece's color.
   */
   
  protected final ColorChooser blackColor;
  
  
  
  /**
   * The color chooser for the white piece's outline color.
   */
   
  protected final ColorChooser whiteOutline;
  
  
  
  /**
   * The color chooser for the black piece's outline color.
   */
   
  protected final ColorChooser blackOutline; 

  
  
  /**
   * The list holding all the available board pattern names.
   */
   
  protected final JList boardPatterns;
  
  
  
  /**
   * The color chooser for the board's dark squares' color.
   */
   
  protected final ColorChooser darkSquares;
   
  

  /**
   * The color chooser for the board's light squares' color.
   */
   
  protected final ColorChooser lightSquares;
  
  
  
  /**
   * The panel containing all of the piece color selection controls. This is
   * needed so that they can be disabled when the selected piece painter is
   * not a ColoredPiecePainter.
   */
   
  private Container pieceColorsPanel;
  
  
  
  /**
   * The panel containing all of the board color selection controls. This is
   * needed so that they can be disabled when the selected board painter is
   * not a ColoredBoardPainter.
   */
   
  private Container boardColorsPanel;
  
  
  
  /**
   * Creates a new <code>BoardLooksPanel</code> for the specified
   * <code>BoardManager</code> and with the specified preview board.
   */
   
  public BoardLooksPanel(BoardManager boardManager, JinBoard previewBoard){
    super(boardManager, previewBoard);
    
    I18n i18n = I18n.get(BoardLooksPanel.class);
    
    pieceSets = new JList(getPieceSets());
    boardPatterns = new JList(getBoardPatterns());
    whiteColor = i18n.createColorChooser("whiteColorChooser");
    blackColor = i18n.createColorChooser("blackColorChooser");
    whiteOutline = i18n.createColorChooser("whiteOutlineChooser");
    blackOutline = i18n.createColorChooser("blackOutlineChooser");
    darkSquares = i18n.createColorChooser("darkSquaresChooser");
    lightSquares = i18n.createColorChooser("lightSquaresChooser");
    
    whiteColor.setColor(boardManager.getWhitePieceColor());
    blackColor.setColor(boardManager.getBlackPieceColor());
    whiteOutline.setColor(boardManager.getWhiteOutlineColor());
    blackOutline.setColor(boardManager.getBlackOutlineColor());
    darkSquares.setColor(boardManager.getDarkSquareColor());
    lightSquares.setColor(boardManager.getLightSquareColor());
    
    JComponent piecesPanel = createPiecesUI();
    JComponent boardPanel = createBoardUI();

    pieceSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    boardPatterns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    pieceSets.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        PieceSet set = (PieceSet)pieceSets.getSelectedValue();
        PiecePainter painter = set.getPiecePainter();
        
        if (painter instanceof ColoredPiecePainter){
          ColoredPiecePainter cPainter = (ColoredPiecePainter)painter; 
          cPainter.setWhiteColor(whiteColor.getColor()); 
          cPainter.setBlackColor(blackColor.getColor()); 
          cPainter.setWhiteOutline(whiteOutline.getColor()); 
          cPainter.setBlackOutline(blackOutline.getColor()); 
        }
         
        BoardLooksPanel.this.previewBoard.setPiecePainter(painter);
        AWTUtilities.setContainerEnabled(pieceColorsPanel, painter instanceof ColoredPiecePainter);
        fireStateChanged();
      }
    });

    boardPatterns.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        BoardPattern pattern = (BoardPattern)boardPatterns.getSelectedValue();
        BoardPainter painter = pattern.getBoardPainter();
        
        if (painter instanceof ColoredBoardPainter){
          ColoredBoardPainter cPainter = (ColoredBoardPainter)painter;
          cPainter.setDarkColor(darkSquares.getColor()); 
          cPainter.setLightColor(lightSquares.getColor()); 
        }
         
        BoardLooksPanel.this.previewBoard.setBoardPainter(painter);
        AWTUtilities.setContainerEnabled(boardColorsPanel, painter instanceof ColoredBoardPainter);
        fireStateChanged();
      }
    });
    
    pieceSets.setSelectedValue(boardManager.getPieceSet(), true);
    boardPatterns.setSelectedValue(boardManager.getBoardPattern(), true);

    ChangeListener pieceColorChangeListener = new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        ColoredPiecePainter painter =
          (ColoredPiecePainter)(BoardLooksPanel.this.previewBoard.getPiecePainter());
        painter.setWhiteColor(whiteColor.getColor());
        painter.setBlackColor(blackColor.getColor());
        painter.setWhiteOutline(whiteOutline.getColor());
        painter.setBlackOutline(blackOutline.getColor());

        BoardLooksPanel.this.previewBoard.repaint();
        fireStateChanged();
      }
    };
    whiteColor.addChangeListener(pieceColorChangeListener);
    blackColor.addChangeListener(pieceColorChangeListener);
    whiteOutline.addChangeListener(pieceColorChangeListener);
    blackOutline.addChangeListener(pieceColorChangeListener);
    
    ChangeListener boardColorChangeListener = new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        // The controls should be disabled when the painter isn't colored
        ColoredBoardPainter painter =
          (ColoredBoardPainter)(BoardLooksPanel.this.previewBoard.getBoardPainter()); 
        painter.setDarkColor(darkSquares.getColor());
        painter.setLightColor(lightSquares.getColor());
        
        BoardLooksPanel.this.previewBoard.repaint();
        fireStateChanged();
      }
    };
    darkSquares.addChangeListener(boardColorChangeListener);
    lightSquares.addChangeListener(boardColorChangeListener);
    
    
    JButton downloadExtras = null;
    if (Jin.getInstance().isUserExtensible()){
      downloadExtras = i18n.createButton("downloadExtrasButton");
      downloadExtras.addActionListener(new UrlDisplayingAction("http://www.jinchess.com/extras/"));
    }
    
    JPanel boardAndExtraPanel = new JPanel();
    boardAndExtraPanel.setLayout(new BoxLayout(boardAndExtraPanel, BoxLayout.Y_AXIS));
    boardPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    if (downloadExtras != null)
      downloadExtras.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    boardAndExtraPanel.add(boardPanel);
    if (downloadExtras != null){
      boardAndExtraPanel.add(Box.createVerticalStrut(15));
      boardAndExtraPanel.add(downloadExtras);
      boardAndExtraPanel.add(Box.createVerticalStrut(15));
    }
   
    
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.add(BorderLayout.WEST, piecesPanel);
    contentPanel.add(BorderLayout.CENTER, Box.createHorizontalStrut(10));
    contentPanel.add(BorderLayout.EAST, boardAndExtraPanel);
    
    JLabel noticeLabel = new JLabel(i18n.getString("colorsNotice"));
    
    contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    noticeLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));    
    add(contentPanel);
    add(Box.createVerticalStrut(10));
    add(noticeLabel);
  }
  
  
  
  /**
   * Gets the list of piece sets from the board manager.
   */
   
  private PieceSet [] getPieceSets(){
    Map resources = boardManager.getResources("pieces");
    PieceSet [] pieceSets = (PieceSet [])resources.values().toArray(new PieceSet[0]);
    
    Collections.sort(Arrays.asList(pieceSets), new Comparator(){
      public int compare(Object arg1, Object arg2){
        PieceSet set1 = (PieceSet)arg1;
        PieceSet set2 = (PieceSet)arg2;
        return set1.getName().compareTo(set2.getName());
      }
    });
    
    return pieceSets;
  }
  
  
  
  /**
   * Gets the list of board patterns from the board manager.
   */
   
  private BoardPattern [] getBoardPatterns(){
    Map resources = boardManager.getResources("boards");
    BoardPattern [] boardPatterns = (BoardPattern [])resources.values().toArray(new BoardPattern[0]);
    
    Collections.sort(Arrays.asList(boardPatterns), new Comparator(){
      public int compare(Object arg1, Object arg2){
        BoardPattern pat1 = (BoardPattern)arg1;
        BoardPattern pat2 = (BoardPattern)arg2;
        return pat1.getName().compareTo(pat2.getName());
      }
    });
    
    return boardPatterns;
  }
  
  
  
  /**
   * Sets the initial properties of the preview board.
   */
   
  public void initPreviewBoard(){
    PieceSet pieceSet = (PieceSet)pieceSets.getSelectedValue();
    if (pieceSet != null){
      PiecePainter piecePainter = pieceSet.getPiecePainter();
      if (piecePainter instanceof ColoredPiecePainter){
        ColoredPiecePainter cPainter = (ColoredPiecePainter)piecePainter; 
        cPainter.setWhiteColor(whiteColor.getColor()); 
        cPainter.setBlackColor(blackColor.getColor()); 
        cPainter.setWhiteOutline(whiteOutline.getColor()); 
        cPainter.setBlackOutline(blackOutline.getColor()); 
      }
      previewBoard.setPiecePainter(piecePainter);
    }

    BoardPattern boardPattern = (BoardPattern)boardPatterns.getSelectedValue();
    if (boardPattern != null){
      BoardPainter boardPainter = boardPattern.getBoardPainter();
      if (boardPainter instanceof ColoredBoardPainter){
        ColoredBoardPainter cPainter = (ColoredBoardPainter)boardPainter;
        cPainter.setDarkColor(darkSquares.getColor()); 
        cPainter.setLightColor(lightSquares.getColor()); 
      }
      previewBoard.setBoardPainter(boardPainter);
    }
  }
  
  
  
  /**
   * Creates the user interface for selecting the looks of the piece set.
   */
   
  private JComponent createPiecesUI(){
    I18n i18n = I18n.get(BoardLooksPanel.class);
    
    JComponent piecesPanel = new JPanel();
    piecesPanel.setLayout(new BoxLayout(piecesPanel, BoxLayout.Y_AXIS));
    piecesPanel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("piecesPanel"),
      BorderFactory.createEmptyBorder(0, 10, 10, 10)));
    
      
    JLabel pieceSetLabel = i18n.createLabel("pieceSetLabel"); 
    pieceSetLabel.setLabelFor(pieceSets);
    pieceSetLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    
    JScrollPane scrollPane = 
      new JScrollPane(pieceSets, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    
    JPanel colorsPanel = new PreferredSizedPanel(new TableLayout(1, 0, 5));
    colorsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    colorsPanel.add(whiteColor);
    colorsPanel.add(blackColor);
    colorsPanel.add(whiteOutline);
    colorsPanel.add(blackOutline);
    
    piecesPanel.add(pieceSetLabel);
    piecesPanel.add(Box.createVerticalStrut(5));
    piecesPanel.add(scrollPane);
    piecesPanel.add(Box.createVerticalStrut(10));
    piecesPanel.add(colorsPanel);
        
    pieceColorsPanel = colorsPanel;
    
    return piecesPanel;
  }
  
  
  
  /**
   * Creates the user interface for selecting the looks of the piece set.
   */
   
  private JComponent createBoardUI(){
    I18n i18n = I18n.get(BoardLooksPanel.class);
    
    JComponent boardPanel = new JPanel();
    boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.Y_AXIS));
    boardPanel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("boardPanel"),
      BorderFactory.createEmptyBorder(0, 10, 10, 10)));
    
    
    JLabel patternLabel = i18n.createLabel("boardPatternLabel");
    patternLabel.setLabelFor(boardPatterns);
    patternLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

    
    JScrollPane scrollPane = 
      new JScrollPane(boardPatterns, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    
    JPanel colorsPanel = new PreferredSizedPanel(new TableLayout(1, 0, 5));
    colorsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    colorsPanel.add(darkSquares);    
    colorsPanel.add(lightSquares);

    boardPanel.add(patternLabel);
    boardPanel.add(Box.createVerticalStrut(5));
    boardPanel.add(scrollPane);
    boardPanel.add(Box.createVerticalStrut(10));
    boardPanel.add(colorsPanel);
    
    boardColorsPanel = colorsPanel;
    
    return boardPanel;
  }
  
  
  
  /**
   * Applies any changes made by the user.
   */
   
  public void applyChanges() throws BadChangesException{
    boardManager.setPieceSet((PieceSet)pieceSets.getSelectedValue());
    boardManager.setBoardPattern((BoardPattern)boardPatterns.getSelectedValue());
    boardManager.setWhitePieceColor(whiteColor.getColor());
    boardManager.setBlackPieceColor(blackColor.getColor());
    boardManager.setWhiteOutlineColor(whiteOutline.getColor());
    boardManager.setBlackOutlineColor(blackOutline.getColor());
    boardManager.setDarkSquareColor(darkSquares.getColor());
    boardManager.setLightSquareColor(lightSquares.getColor());
  }
  

  
}
