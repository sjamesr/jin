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

package free.jin.seek;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.*;

import free.chess.Chess;
import free.chess.FischerTimeControl;
import free.chess.Player;
import free.chess.WildVariant;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.Seek;
import free.jin.ServerUser;
import free.jin.plugin.Plugin;
import free.jin.seek.event.SeekSelectionEvent;
import free.jin.seek.event.SeekSelectionListener;
import free.util.ImageUtilities;
import free.util.TableLayout;
import free.util.swing.WrapLayout;


/**
 * The sought graph component.
 */

public class SoughtGraph extends JComponent{
  
  
  
  /**
   * The amount of slots dedicated to bullet.
   */
  
  protected static final int BULLET_SLOTS = 7;
  
  
  
  /**
   * The amount of slots dedicated to blitz.
   */
  
  protected static final int BLITZ_SLOTS = 28;
  
  
  
  /**
   * The amount of slots dedicated to standard.
   */
  
  protected static final int STANDARD_SLOTS = 15;
  
  
  
  /**
   * The total amount of rating (vertical) slots.
   */
  
  protected static final int RATING_SLOTS = 32;
  
  
  
  /**
   * The percentage of the width dedicated to the graph itself.
   */
  
  protected static final double GRAPH_WIDTH_PERCENTAGE = 0.92;
  
  
  
  /**
   * The percentage of the height dedicated to the graph itself.
   */
  
  protected static final double GRAPH_HEIGHT_PERCENTAGE = 0.85;
  
  
  
  /**
   * The Plugin this SoughtGraph is used by.
   */
  
  protected final Plugin plugin;
  
  
  
  /**
   * The size of the smallest seek image. 
   */
  
  private final int minSeekImageSize;
  
  
  
  /**
   * The size of the largest seek image.
   */
  
  private final int maxSeekImageSize;
  
  
  
  /**
   * Maps Seeks to Point objects indicating their locations on the graph.
   */
  
  protected final Hashtable seeksToLocations = new Hashtable();
  
  
  
  /**
   * A matrix of seeks which maps locations to seeks in those locations.
   */
  
  protected final Seek [][] seekMatrix = new Seek[BULLET_SLOTS+BLITZ_SLOTS+STANDARD_SLOTS][RATING_SLOTS];
  
  
  
  /**
   * The seek currently under the mouse cursor.
   */
  
  protected Seek curSeek = null;
  
  
  
  /**
   * The current location of the mouse.
   */
  
  protected Point curMouseLocation = null;
  
  
  
  /**
   * The background image.
   */
  
  private final Image bgImage;
  
  
  
  /**
   * The name of the fast games category. 
   */
  
  private final String fastCategoryName;
  
  
  
  /**
   * The name of the moderate speed games category.
   */
  
  private final String moderateCategoryName;
  
  
  
  /**
   * The name of the slow games category.
   */
  
  private final String slowCategoryName;
  
  
  
  /**
   * An array whose indices specify the size of the seek images and whose values
   * are Hashtables mapping seek type keys (Strings) to cached Images. The seek
   * type keys are in the following format:
   * "seek-image.rated/unrated.computer/human.variant"
   */
  
  private final Hashtable [] seekImageCache;
  
  
  
  /**
   * Our legend popup.
   */
  
  private final JPopupMenu legendPopup;
  
  
  
  /**
   * The <code>ServerUser</code> object for the account we're logged in with.
   */
  
  private ServerUser user;
  
  
  
  /**
   * Creates a new SoughtGraph with the give user Plugin.
   */
  
  public SoughtGraph(Plugin plugin){
    this.plugin = plugin;
    
    Preferences prefs = plugin.getPrefs();
    I18n i18n = plugin.getI18n();
    
    setFont(new Font("SansSerif", Font.PLAIN, 10));
    
    this.bgImage = getToolkit().getImage(SoughtGraph.class.getResource("background.gif"));
    ImageUtilities.preload(bgImage);
    
    fastCategoryName = i18n.getString(prefs.getString("fastCategory.nameKey"));
    moderateCategoryName = i18n.getString(prefs.getString("moderateCategory.nameKey"));
    slowCategoryName = i18n.getString(prefs.getString("slowCategory.nameKey"));
    
    int [] seekImageSizes = new int[]{5, 7, 9, 11, 13, 15};
    int maxSize = 0, minSize = Integer.MAX_VALUE;
    for (int i = 0; i < seekImageSizes.length; i++){
      if (seekImageSizes[i] > maxSize)
        maxSize = seekImageSizes[i];
      if (seekImageSizes[i] < minSize)
        minSize = seekImageSizes[i];
    }
    
    minSeekImageSize = minSize;
    maxSeekImageSize = maxSize;
    
    seekImageCache = new Hashtable[maxSize+1];
    for (int i = 0; i < seekImageSizes.length; i++)
      seekImageCache[seekImageSizes[i]] = new Hashtable(10);
    
    legendPopup = createLegendPopup();
    
    createUI();
    enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK|MouseEvent.MOUSE_EVENT_MASK);
    
    setToolTipText(""); // Enables tooltips
  }
  
  
  
  /**
   * Creates the UI.
   */
  
  private void createUI(){
    setLayout(new FlowLayout(FlowLayout.RIGHT));
    
    Icon legendButtonIcon = new ImageIcon(SoughtGraph.class.getResource("legend.png"));
    final JButton legendButton = new JButton(legendButtonIcon);
    
    legendButton.setFocusable(false);
    legendButton.setMargin(new Insets(0, 0, 0, 0));
    
    // setMargin isn't enough for Ocean or Windows L&Fs
    Dimension legendButtonSize = new Dimension(legendButtonIcon.getIconWidth() + 4, legendButtonIcon.getIconHeight() + 4);
    legendButton.setMinimumSize(legendButtonSize);
    legendButton.setPreferredSize(legendButtonSize);
    legendButton.setMaximumSize(legendButtonSize);
    
    legendButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        legendPopup.show(legendButton, 0, legendButton.getHeight());
      }
    });
    
    add(legendButton);
  }
  
  
  
  /**
   * Creates the legend popup.
   */
  
  private JPopupMenu createLegendPopup(){
    I18n i18n = I18n.get(SoughtGraph.class);
    Class imageLoader = SoughtGraph.class;
    String imageDir = "images/legend/";
    
    final int vRelatedGap = 3;
    final int vUnrelatedGap = 10;
    final int hRelatedGap = 10;
    final int border = 5;
    final int indent = 15;
    
    
    Icon normalIcon = new ImageIcon(imageLoader.getResource(imageDir + "normal.png"));
    Icon wildIcon = new ImageIcon(imageLoader.getResource(imageDir + "wild.png"));
    Icon humanIcon = new ImageIcon(imageLoader.getResource(imageDir + "human.png"));
    Icon computerIcon = new ImageIcon(imageLoader.getResource(imageDir + "computer.png"));
    Icon ratedIcon = new ImageIcon(imageLoader.getResource(imageDir + "rated.png"));
    Icon unratedIcon = new ImageIcon(imageLoader.getResource(imageDir + "unrated.png"));
    
    // Normal/Wild
    JPanel chessTypePanel = new JPanel(new TableLayout(2, hRelatedGap, 0));
    chessTypePanel.setOpaque(false);
    chessTypePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    chessTypePanel.add(new JLabel(normalIcon));
    chessTypePanel.add(i18n.createLabel("normalChessLabel"));
    chessTypePanel.add(new JLabel(wildIcon));
    chessTypePanel.add(i18n.createLabel("wildChessLabel"));
    
    // Human/Computer
    JPanel opponentTypePanel = new JPanel(new TableLayout(2, hRelatedGap, 0));
    opponentTypePanel.setOpaque(false);
    opponentTypePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    opponentTypePanel.add(new JLabel(humanIcon));
    opponentTypePanel.add(i18n.createLabel("humanLabel"));
    opponentTypePanel.add(new JLabel(computerIcon));
    opponentTypePanel.add(i18n.createLabel("computerLabel"));
    
    // Rated/Unrated row
    JPanel ratednessPanel = new JPanel(new TableLayout(2, hRelatedGap, 0));
    ratednessPanel.setOpaque(false);
    ratednessPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    ratednessPanel.add(new JLabel(ratedIcon));
    ratednessPanel.add(i18n.createLabel("ratedLabel"));
    ratednessPanel.add(new JLabel(unratedIcon));
    ratednessPanel.add(i18n.createLabel("unratedLabel"));
    
    // Title labels
    JLabel insideLabel = i18n.createLabel("insideLabel");
    JLabel outlineLabel = i18n.createLabel("outlineLabel");
    JLabel shapeLabel = i18n.createLabel("shapeLabel");
    
    JPanel legendPanel = new JPanel(null);
    legendPanel.setOpaque(false);
    legendPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
    
    legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.PAGE_AXIS));
    chessTypePanel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
    opponentTypePanel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
    ratednessPanel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
    
    legendPanel.add(insideLabel);
    legendPanel.add(Box.createVerticalStrut(vRelatedGap));
    legendPanel.add(chessTypePanel);
    legendPanel.add(Box.createVerticalStrut(vUnrelatedGap));
    legendPanel.add(outlineLabel);
    legendPanel.add(Box.createVerticalStrut(vRelatedGap));
    legendPanel.add(opponentTypePanel);
    legendPanel.add(Box.createVerticalStrut(vUnrelatedGap));
    legendPanel.add(shapeLabel);
    legendPanel.add(Box.createVerticalStrut(vRelatedGap));
    legendPanel.add(ratednessPanel);
    
    Dimension legendPanelSize = legendPanel.getPreferredSize();
    
    JToolTip tooltip = new JToolTip();
    tooltip.setLayout(WrapLayout.getInstance());
    tooltip.add(legendPanel);
    tooltip.setPreferredSize(new Dimension(legendPanelSize.width + 6, legendPanelSize.height + 6));
    
    JPopupMenu popup = new JPopupMenu();
    popup.setLayout(WrapLayout.getInstance());
    popup.add(tooltip);
    
    return popup;
  }
  
  
  
  /**
   * Returns the etime, multiplied by 3.
   */
  
  private static int calcEtimeTimes3(Seek seek){
    FischerTimeControl tc = (FischerTimeControl)seek.getTimeControl();
    return 3 * tc.getInitial()/(60*1000) + 2 * tc.getIncrement()/1000;
  }
  
  
  
  /**
   * This method calculates the desired location of the given seek. Note that the
   * seek might end up elsewhere because that location is already taken.
   */
  
  protected Point mapSeek(Seek seek){
    int etimeM3 = calcEtimeTimes3(seek);
    int rating = seek.isSeekerRated() ? seek.getSeekerRating() : 0;
    
    int x,y;
    
    if (etimeM3<9) // Bullet
      x = (etimeM3-2)*BULLET_SLOTS/7; // 7 is the amount of possible etime values for bullet.
    else if (etimeM3<45) // Blitz
      x = BULLET_SLOTS+(etimeM3-7-2)*BLITZ_SLOTS/36; // 36 is the amount of possible etime values for blitz.
    else // Standard
      x = (int)(BULLET_SLOTS+BLITZ_SLOTS+STANDARD_SLOTS*(1-Math.pow(1.1,-((etimeM3-7-36-2)/10.0))));
    // The formula is just something exponential that looks good.
    
    if (rating==0)
      y = 0;
    else if (rating<1000)
      y = 1+rating/200;
    else if (rating<1500)
      y = 6+(rating-1000)/50;
    else if (rating<2000)
      y = 16+(rating-1500)/50;
    else if (rating<3000)
      y = 26+(rating-2000)/200;
    else
      y = 31;
    
    return new Point(x,y);
  }
  
  
  
  /**
   * This method returns the actual location where the given seek will be put
   * depending on the given desired location. This method always returns the
   * location of an empty slot.
   */
  
  protected Point fitSeek(Seek seek, Point desiredSlot){
    if (seekMatrix[desiredSlot.x][desiredSlot.y]==null)
      return desiredSlot;
    
    int etimeM3 = calcEtimeTimes3(seek);
    boolean isBullet = etimeM3<9;
    boolean isBlitz = (!isBullet)&&(etimeM3<45);
    boolean isStandard = (etimeM3>=45);
    
    int x = desiredSlot.x;
    int y = desiredSlot.y;
    
    int direction = 0;
    int spiralLength = 1;
    while(true){
      for (int i=0;i<2;i++){
        for (int j=0;j<spiralLength;j++){
          switch(direction){
            case 0: x++; break; // Right
            case 1: y++; break; // Up
            case 2: x--; break; // Left
            case 3: y--; break; // Down 
          }
          
          if ((x<0)||(x>=seekMatrix.length)||(y<0)||(y>=seekMatrix[0].length))
            continue;
          
          if (isBullet&&(x>=BULLET_SLOTS)) // Don't put a bullet seek on a non-bullet slot.
            continue; 
          
          if (isBlitz&&((x<BULLET_SLOTS)||(x>=BULLET_SLOTS+BLITZ_SLOTS))) // Same for blitz.
            continue; 
          
          if (isStandard&&(x<BULLET_SLOTS+BLITZ_SLOTS)) // And same for standard.
            continue;
          
          if (seekMatrix[x][y]==null)
            return new Point(x,y);
        }
        direction++;
        if (direction==4)
          direction = 0;
      }
      spiralLength++;
    }
  }
  
  
  
  /**
   * Adds the given Seek to this SoughtGraph.
   */
  
  public void addSeek(Seek seek){
    if (!(seek.getTimeControl() instanceof FischerTimeControl))
      return;
    
    Point desiredSlot = mapSeek(seek);
    Point actualSlot = fitSeek(seek, desiredSlot);
    
    seeksToLocations.put(seek, actualSlot);
    seekMatrix[actualSlot.x][actualSlot.y] = seek;
    
    Rectangle seekBounds = getSeekBounds(actualSlot.x, actualSlot.y, null);
    repaint(seekBounds.x - 2, seekBounds.y - 2, seekBounds.width + 4, seekBounds.height + 4);
    
    if (curMouseLocation!=null)
      updateCurrentSeek(curMouseLocation.x, curMouseLocation.y);
  }
  
  
  
  /**
   * Removes the given Seek from this SoughtGraph.
   */
  
  public void removeSeek(Seek seek){
    Point location = (Point)seeksToLocations.remove(seek);
    if (location == null)
      return;
    
    seekMatrix[location.x][location.y] = null;
    
    Rectangle seekBounds = getSeekBounds(location.x, location.y, null);
    repaint(seekBounds.x - 2, seekBounds.y - 2, seekBounds.width + 4, seekBounds.height + 4);
    
    if ((seek == curSeek) && (curMouseLocation != null)) // The !=null check is just in case.
      updateCurrentSeek(curMouseLocation.x, curMouseLocation.y);
  }
  
  
  
  /**
   * Removes all the seeks.
   */
  
  public void removeAllSeeks(){
    seeksToLocations.clear();
    
    for (int i = 0; i < seekMatrix.length; i++)
      for (int j = 0; j < seekMatrix[i].length; j++)
        seekMatrix[i][j] = null;
    
    repaint();
  }
  
  
  
  /**
   * Returns the bounding rectangle of the seek at the given location in the
   * seek matrix.
   */
  
  protected Rectangle getSeekBounds(int x, int y, Rectangle rect){
    if (rect == null)
      rect = new Rectangle();
    
    int width = getWidth();
    int height = getHeight();
    
    int graphX = (int)(width*(1-GRAPH_WIDTH_PERCENTAGE))+1;
    int graphY = 0;
    int graphWidth = (int)(width*GRAPH_WIDTH_PERCENTAGE);
    int graphHeight = (int)(height*GRAPH_HEIGHT_PERCENTAGE);
    
    double slotWidth = ((double)graphWidth)/(BULLET_SLOTS+BLITZ_SLOTS+STANDARD_SLOTS);
    double slotHeight = ((double)graphHeight)/RATING_SLOTS;
    
    rect.x = (int)(graphX+x*slotWidth);
    rect.y = (int)(graphY+graphHeight-(y+1)*slotHeight);
    rect.width = (int)slotWidth;
    rect.height = (int)slotHeight;
    
    return rect;
  }
  
  
  
  /**
   * Paints this SoughtGraph on the given Graphics.
   */
  
  public void paintComponent(Graphics g){
    Rectangle clipRect = g.getClipBounds();
    
    int width = getWidth();
    int height = getHeight();
    
    Color bg = getBackground();
    Color fg = getForeground();
    Color lightFG = new Color((bg.getRed()+fg.getRed())/2, (bg.getGreen()+fg.getGreen())/2, (bg.getBlue()+fg.getBlue())/2);
    
    g.setColor(bg);
    g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
    
    if (bgImage != null){
      Rectangle imageRect = new Rectangle(0, 0, bgImage.getWidth(null), bgImage.getHeight(null));
      while (imageRect.x < width){
        imageRect.y = 0;
        while (imageRect.y < height){
          if (imageRect.intersects(clipRect))
            g.drawImage(bgImage, imageRect.x, imageRect.y, this);
          imageRect.y += imageRect.height;
        }
        imageRect.x += imageRect.width;
      }
    }
    
    int graphX = (int)(width*(1-GRAPH_WIDTH_PERCENTAGE));
    int graphY = 0;
    int graphWidth = (int)(width*GRAPH_WIDTH_PERCENTAGE);
    int graphHeight = (int)(height*GRAPH_HEIGHT_PERCENTAGE);
    
    double slotWidth = ((double)graphWidth)/(BULLET_SLOTS+BLITZ_SLOTS+STANDARD_SLOTS);
    double slotHeight = ((double)graphHeight)/RATING_SLOTS;
    
    int bulletWidth = (int)(slotWidth*BULLET_SLOTS);
    int blitzWidth = (int)(slotWidth*BLITZ_SLOTS);
    int standardWidth = (int)(slotWidth*STANDARD_SLOTS);
    
    // The axes
    g.setColor(fg);
    g.drawLine(graphX,graphY+graphHeight,graphX+graphWidth,graphY+graphHeight);
    g.drawLine(graphX,graphY,graphX,graphY+graphHeight);
    // The axises' arrows
    g.fillPolygon(new int[]{graphX+5,graphX-5,graphX},new int[]{graphY+9,graphY+9,graphY},3);
    g.fillPolygon(new int[]{graphX+graphWidth,graphX+graphWidth-9,graphX+graphWidth-9},new int[]{graphY+graphHeight,graphY+graphHeight-5,graphY+graphHeight+5},3);
    
    
    g.setColor(lightFG);
    
    // The line separating bullet from blitz.
    int bbX = (int)(graphX+BULLET_SLOTS*slotWidth);
    g.drawLine(bbX, graphY, bbX, graphY+graphHeight);
    
    // The line separating blitz from standard.
    int bsX = (int)(graphX+(BULLET_SLOTS+BLITZ_SLOTS)*slotWidth);
    g.drawLine(bsX, graphY, bsX, graphY+graphHeight);
    
    
    g.setColor(fg);
    
    // The speed category names    
    g.setFont(getFont().deriveFont(Math.min(
        (float)(height - (graphY + graphHeight)) / 3, // The category strings
        (float)(graphX) / 3))); // The rating strings (should divide by "2000".length(), but we want it slightly larger
    FontMetrics fm = g.getFontMetrics();
    int ratingStrWidth = fm.stringWidth("2000");
    while (ratingStrWidth > graphX){
      Font font = g.getFont();
      g.setFont(font.deriveFont(font.getSize2D() - 1));
      fm = g.getFontMetrics();
      ratingStrWidth = fm.stringWidth("2000");
    }
    
    int timeStringY = graphY + graphHeight + fm.getMaxAscent() + 1;
    g.drawString(fastCategoryName, graphX + (bulletWidth - fm.stringWidth(fastCategoryName))/2, timeStringY);
    g.drawString(moderateCategoryName, graphX + bulletWidth + (blitzWidth - fm.stringWidth(moderateCategoryName))/2,
        timeStringY);
    g.drawString(slowCategoryName, graphX + bulletWidth + blitzWidth + 
        (standardWidth - fm.stringWidth(slowCategoryName))/2, timeStringY);
    
    // The current seek description string.
    if (curSeek != null){
      String seekString = getSeekString(curSeek);
      g.drawString(seekString, fm.getHeight()/3, height - fm.getMaxDescent() - 1);
    }
    
    
    // The "1000", "1500" and "2000" strings.
    String tenString = "1000";
    String fifteenString = "1500";
    String twentyString = "2000";
    
    int strWidth = fm.stringWidth("2000");
    
    g.drawString(tenString, (graphX - strWidth)/2, (int)(graphY + graphHeight - 6*slotHeight));
    g.drawString(fifteenString, (graphX - strWidth)/2, (int)(graphY + graphHeight - 16*slotHeight));
    g.drawString(twentyString, (graphX - strWidth)/2, (int)(graphY + graphHeight - 26*slotHeight));
    
    
    // The seeks.    
    Rectangle seekBounds = new Rectangle();
    for (int i=0;i<seekMatrix.length;i++){
      for (int j=0;j<seekMatrix[i].length;j++){
        Seek seek = seekMatrix[i][j];
        if (seek==null)
          continue;
        
        seekBounds = getSeekBounds(i, j, seekBounds);
        if (seekBounds.intersects(clipRect)){
//        g.setClip(clipRect);
          drawSeek(g, seek, seekBounds);
        }
      }
    }
  }
  
  
  
  /**
   * Returns the Image that should be drawn for the specified Seek at the
   * specified size.
   */
  
  private Image getSeekImage(Rectangle seekBounds, Seek seek){
    int size = Math.min(seekBounds.width, seekBounds.height);
    if (size <= 0)
      throw new IllegalArgumentException("Seek bounds size must be positive");
    
    int index = -1;
    
    if (size >= seekImageCache.length)
      index = seekImageCache.length-1;
    else if (seekImageCache[size] != null)
      index = size;
    
    if (index == -1){
      for (int i = size+1; i < seekImageCache.length; i++)
        if (seekImageCache[i] != null){
          index = i;
          break;
        }
    }
    
    if (index == -1){
      for (int i = size; i > 0; i--)
        if (seekImageCache[i] != null){
          index = i;
          break;
        }
    }
    
    if (index == -1)
      throw new IllegalStateException("Couldn't find suitable seek images");
    
    Hashtable sizeImages = seekImageCache[index];
    
    if (user == null) // Lazily create
      user = plugin.getConn().getUser();
    
    boolean isMySeek = seek.getSeeker().equals(user);
    String playerType = seek.isSeekerComputer() ? "comp" : "human";
    String ratedString = seek.isRated() ? "rated" : "unrated";
    boolean isWild = !seek.getVariant().equals(Chess.getInstance());
    
    String imageName = (isMySeek ? "own" : 
      ratedString + "_" + playerType + (isWild ? "_wild" : "")) + ".png";
    
    Image image = (Image)sizeImages.get(imageName);
    if (image == null){
      String imageFile = "images/"+index+"/"+imageName;
      image = getToolkit().getImage(SoughtGraph.class.getResource(imageFile));
      if (ImageUtilities.preload(image) != ImageUtilities.COMPLETE)
        return null;
      sizeImages.put(imageName, image);
    }
    
    return image;
  }
  
  
  
  /**
   * Draws a single seek within the given bounds.
   */
  
  protected void drawSeek(Graphics g, Seek seek, Rectangle seekBounds){
    Image seekImage = getSeekImage(seekBounds, seek);
    
    if (seekImage == null){
      g.setColor(Color.black);
      g.drawOval(seekBounds.x, seekBounds.y, seekBounds.width-1, seekBounds.height-1);
      return;
    }
    
    int imageWidth = seekImage.getWidth(null);
    int imageHeight = seekImage.getHeight(null);
    
    g.drawImage(seekImage, 
        seekBounds.x + (seekBounds.width - imageWidth)/2, 
        seekBounds.y + (seekBounds.height - imageHeight)/2,
        null);
  }
  
  
  
  /**
   * Returns a string representing the given seek.
   */
  
  protected String getSeekString(Seek seek){
//  <name><titles> <rating> <(provisional)> seeks <time> <inc> [isRated] [wild] [color] [minrating]-[maxrating] [manual] [formula]
    
    I18n i18n = I18n.get(SoughtGraph.class);
    String provisional = i18n.getString("provisional");
    String rated = i18n.getString("rated");
    String unrated = i18n.getString("unrated");
    String white = i18n.getString("white");
    String black = i18n.getString("black");
    String manualAcceptIndicator = i18n.getString("manualAcceptIndicator");
    String formulaIndicator = i18n.getString("formulaIndicator");
    String computerIndicator = i18n.getString("computerIndicator");
    
    boolean isComputer = seek.isSeekerComputer();
    String name = seek.getSeekerName();
    String title = seek.getSeekerTitle();
    int rating = seek.getSeekerRating();
    boolean isProvisional = seek.isSeekerProvisional()&&seek.isSeekerRegistered();
    String timeControlString = seek.getTimeControl().getLocalizedMediumDescription();
    boolean isRated = seek.isRated();
    WildVariant variant = seek.getVariant();
    Player color = seek.getSoughtColor();
    boolean isManualAccept = seek.isManualAccept();
    boolean isFormula = seek.isFormula();
    
    StringBuffer buf = new StringBuffer();
    
    buf.append(name);
    if (!isComputer)
      buf.append(title);
    
    if (seek.isSeekerRated() && !(isComputer && !isRated)){
      buf.append("(");
      buf.append(rating);
      if (isProvisional){
        buf.append(" ");
        buf.append(provisional);
      }
      buf.append(")");
    }
    
    if (isComputer)
      buf.append("(").append(computerIndicator).append(")");
    
    buf.append(" ").append(timeControlString);
    
    buf.append(" ").append(isRated ? rated : unrated);
    
    if (!(variant instanceof Chess))
      buf.append(" ").append(variant.getName());
    
    if (color != null)
      buf.append(" ").append(color.isWhite() ? white : black);
    
    if (!isComputer){
      if (isManualAccept)
        buf.append(" ").append(manualAcceptIndicator);
      
      if (isFormula)
        buf.append(" ").append(formulaIndicator);
    }
    
    return buf.toString();
  }
  
  
  
  /**
   * Returns the seek at the given pixel coordinates, or null if none.
   */
  
  protected Seek seekAtLocation(int x, int y){
    int width = getWidth();
    int height = getHeight();
    
    int graphX = (int)(width*(1-GRAPH_WIDTH_PERCENTAGE));
    int graphY = 0;
    int graphWidth = (int)(width*GRAPH_WIDTH_PERCENTAGE);
    int graphHeight = (int)(height*GRAPH_HEIGHT_PERCENTAGE);
    
    double slotWidth = ((double)graphWidth)/(BULLET_SLOTS+BLITZ_SLOTS+STANDARD_SLOTS);
    double slotHeight = ((double)graphHeight)/RATING_SLOTS;
    
    if (x < graphX)
      return null;
    if (graphY + graphHeight < y)
      return null;
    
    int i = (int)((x-graphX)/slotWidth);
    int j = (int)((graphY+graphHeight-y)/slotHeight);
    
    if ((i < 0) || (i >= seekMatrix.length) || (j < 0) || (j >= seekMatrix[i].length))
      return null;
    
    return seekMatrix[i][j];
  }
  
  
  
  /**
   * Returns the tooltip text to be displayed at the specified coordinate.
   */
  
  public String getToolTipText(MouseEvent evt){
    Seek seek = seekAtLocation(evt.getX(), evt.getY());
    return seek == null ? null : getSeekString(seek);
  }
  
  
  
  /**
   * Possibly changes the current seek under mouse and repaints if necessary.
   * The given coordinates should be the coordinates of the mouse.
   */
  
  protected void updateCurrentSeek(int x, int y){
    Seek newSeek = seekAtLocation(x, y);
    if (newSeek != curSeek){
      curSeek = newSeek;
      repaint(0, (int)(getHeight()*GRAPH_HEIGHT_PERCENTAGE), getWidth(), (int)(getHeight()*(1-GRAPH_HEIGHT_PERCENTAGE)));
      
      if (newSeek != null)
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      else
        setCursor(Cursor.getDefaultCursor());
    }
  }
  
  
  
  /**
   * Changes the current seek under mouse and repaints if necessary.
   */
  
  protected void processMouseMotionEvent(MouseEvent evt){
    super.processMouseMotionEvent(evt);
    
    if (evt.getID() == MouseEvent.MOUSE_MOVED){
      updateCurrentSeek(evt.getX(), evt.getY());
      curMouseLocation = evt.getPoint();
    }
  }
  
  
  
  /**
   * If a seek is clicked, fires a SeekSelectionEvent.
   * Also, if the mouse leaves the component, nulls the current seek and repaints.
   */
  
  protected void processMouseEvent(MouseEvent evt){
    super.processMouseEvent(evt);
    
    if (evt.getID() == MouseEvent.MOUSE_EXITED){
      curSeek = null;
      curMouseLocation = null;
      repaint(0, (int)(getHeight()*GRAPH_HEIGHT_PERCENTAGE), getWidth(), (int)(getHeight()*(1-GRAPH_HEIGHT_PERCENTAGE)));
    }
    if (evt.getID() == MouseEvent.MOUSE_ENTERED){
      curMouseLocation = evt.getPoint();
      setCursor(Cursor.getDefaultCursor());
    }
    if ((evt.getID() == MouseEvent.MOUSE_CLICKED) && (evt.getModifiers() == MouseEvent.BUTTON1_MASK)){
      Seek pressedSeek = seekAtLocation(evt.getX(), evt.getY());
      if (pressedSeek!=null)
        fireSeekSelectionEvent(new SeekSelectionEvent(this, pressedSeek));
    }
  }
  
  
  
  /**
   * Adds the given SeekSelectionListener to the list of listeners receiving
   * notifications when the user selects seeks.
   */
  
  public void addSeekSelectionListener(SeekSelectionListener listener){
    listenerList.add(SeekSelectionListener.class, listener);
  }
  
  
  
  /**
   * Removes the given SeekSelectionListener from the list of listeners receiving
   * notifications when the user selects seeks.
   */
  
  public void removeSeekSelectionListener(SeekSelectionListener listener){
    listenerList.remove(SeekSelectionListener.class, listener);
  }
  
  
  
  /**
   * Fires the given SeekSelectionEvent to all interested SeekSelectionListeners.
   */
  
  protected void fireSeekSelectionEvent(SeekSelectionEvent evt){
    Object [] listenerList = this.listenerList.getListenerList();
    for (int i=0;i<listenerList.length;i+=2){
      if (listenerList[i]==SeekSelectionListener.class){
        SeekSelectionListener listener = (SeekSelectionListener)listenerList[i+1];
        listener.seekSelected(evt);
      }
    }
  }
  
  
  
  /**
   * Returns the minimum size of the sought graph.
   */
  
  public Dimension getMinimumSize(){
    int width = (int)(minSeekImageSize * (BULLET_SLOTS + BLITZ_SLOTS + STANDARD_SLOTS) / GRAPH_WIDTH_PERCENTAGE);
    int height = (int)(minSeekImageSize * RATING_SLOTS / GRAPH_HEIGHT_PERCENTAGE);
    return new Dimension(width, height);
  }
  
  
  
  /**
   * Returns the preferred size of the sought graph.
   */
  
  public Dimension getPreferredSize(){
    int width = (int)(maxSeekImageSize * (BULLET_SLOTS + BLITZ_SLOTS + STANDARD_SLOTS) / GRAPH_WIDTH_PERCENTAGE);
    int height = (int)(maxSeekImageSize * RATING_SLOTS / GRAPH_HEIGHT_PERCENTAGE);
    return new Dimension(width, height);
  }
  
  
  
  /**
   * Returns the maximum size of the sought graph.
   */
  
  public Dimension getMaximumSize(){
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }
  
  
  
}
