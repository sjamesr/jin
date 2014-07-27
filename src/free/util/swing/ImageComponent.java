/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import free.util.AWTUtilities;



/**
 * A component which draws an image.
 */

public class ImageComponent extends JComponent{
  
  
  
  /**
   * The image we're currently displaying.
   */
  
  private Image image;
  
  
  
  /**
   * Whether we're displaying a fullsize version of the image in a popup on
   * mouseover.
   */
  
  private boolean isImagePopup = false;
  
  
  
  /**
   * The <code>JPopupMenu</code> in which we're displaying the fullsize image on
   * mouseover.
   */
  
  private JPopupMenu popup = null;
  
  
  
  /**
   * Creates a new <code>ImageComponent</code> with the specified image (may be
   * <code>null</code>).
   */
  
  public ImageComponent(Image image){
    setImage(image);
    
    addMouseListener(new MouseAdapter(){
      
      private Point popupLocation = new Point();
      
      private boolean popupUnderCursor = false;
      private Point mouseLocationInPopup;
      
      private final Timer popupTimer = new Timer(ToolTipManager.sharedInstance().getInitialDelay(), new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
          popup.show(ImageComponent.this, popupLocation.x, popupLocation.y);
        }
      });
      
      @Override
      public void mouseEntered(MouseEvent e){
        if (!isPopupImage() || !isImageSizeKnown()) 
          return;
        
        if (popup == null){
          JToolTip tip = new JToolTip();
          tip.setLayout(WrapLayout.getInstance());
          ImageComponent ic = new ImageComponent(getImage());
          tip.add(ic);
          
          popup = new JPopupMenu();
          popup.setLayout(WrapLayout.getInstance());
          popup.add(tip);
          
          ic.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseMoved(MouseEvent evt){
              if (!evt.getPoint().equals(mouseLocationInPopup))
                popup.setVisible(false);
            }
          });
          
          ic.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent evt){
              mouseLocationInPopup = new Point(evt.getPoint());
              popupUnderCursor = true;
            }
            @Override
            public void mouseExited(MouseEvent evt){
              popup.setVisible(false);
            }
          });
          
          popup.setPreferredSize(AWTUtilities.augmentSize(ic.getPreferredSize(), tip.getInsets()));
        }
        
        popupUnderCursor = false;
        popupLocation.setLocation(e.getX() + 5, e.getY() + 5);
        popupTimer.setRepeats(false);
        popupTimer.start();
      }
      
      @Override
      public void mouseExited(MouseEvent e){
        if (popupTimer.isRunning())
          popupTimer.stop();
        if (popup != null){
          // By doing this in invokeLater, we can make sure that the mouseExit
          // wasn't because the popup showed underneath the cursor (in which
          // case we don't want to close it).
          SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
              if (!popupUnderCursor)
                popup.setVisible(false);
            }
          });
        }
      }
      
    });
  }
  
  
  
  /**
   * Sets the image.
   */
  
  public void setImage(Image image){
    this.image = image;
    
    if (popup != null)
      popup.setVisible(false);
    popup = null;
    
    if (image != null)
      getToolkit().prepareImage(image, -1, -1, this);
    
    revalidate();
    repaint();
  }
  
  
  
  /**
   * Returns the image.
   */
  
  public Image getImage(){
    return image;
  }
  
  
  
  /**
   * Returns whether we're displaying the full-size image in a popup on
   * mouseover.
   */
  
  public boolean isPopupImage(){
    return isImagePopup;
  }
  
  
  
  /**
   * Sets whether we're displaying the full-size image in a popup on mouseover.
   */
  
  public void setImageToolTip(boolean isImagePopup){
    this.isImagePopup = isImagePopup;
  }
  
  
  
  /**
   * Returns whether the image is not <code>null</code> and its size is fully
   * known.
   */
  
  public boolean isImageSizeKnown(){
    return (image != null) && (image.getWidth(this) > -1) && (image.getHeight(this) > -1);
  }
  
  
  
  /**
   * Returns whether the image is not <code>null</code> and is fully loaded.
   */
  
  public boolean isImageLoaded(){
    return (image != null) && ((getToolkit().checkImage(image, -1, -1, this) & ImageObserver.ALLBITS) != 0);
  }
  
  
  
  /**
   * Paints the component.
   */
  
  @Override
  public void paintComponent(Graphics g){
    if (image == null)
      return;
    
    int width = getWidth();
    int height = getHeight();
    
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);
    
    if ((imageWidth == -1) || (imageHeight == -1))
      g.drawImage(image, 0, 0, width, height, this);
    else{
      double ratio = ((double)width)/height;
      double imageRatio = ((double)imageWidth)/imageHeight;
      
      if (ratio > imageRatio)
        g.drawImage(image, 0, 0, (int)(height*imageRatio), height, this);
      else
        g.drawImage(image, 0, 0, width, (int)(width/imageRatio), this);
    }
  }
  
  
  
  /**
   * Invoked when the image loading process retrieves more data.
   */
  
  @Override
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height){
    revalidate();
    
    return super.imageUpdate(img, infoflags, x, y, width, height);
  }
  
  
  
  /**
   * Returns our preferred size.
   */
  
  @Override
  public Dimension getPreferredSize(){
    if (!isImageSizeKnown())
      return new Dimension(0, 0);
    
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);
    
    return new Dimension(imageWidth, imageHeight);
  }
  
  
  
  /**
   * Returns the preferred width when displayed at the specified height.
   */
  
  public int getPreferredWidth(int height){
    if (!isImageSizeKnown())
      return 0;
    
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);
    
    double imageRatio = ((double)imageWidth)/imageHeight;
    return (int)(height*imageRatio);
  }
  
  
  
  /**
   * Returns the preferred height when displayed at the specified width.
   */
  
  public int getPreferredHeight(int width){
    if (!isImageSizeKnown())
      return 0;
    
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);
    
    double imageRatio = ((double)imageWidth)/imageHeight;
    return (int)(width/imageRatio);
  }
  
  
  
  /**
   * Returns our minimum size.
   */
  
  @Override
  public Dimension getMinimumSize(){
    return new Dimension(0, 0);
  }
  
  
  
}
