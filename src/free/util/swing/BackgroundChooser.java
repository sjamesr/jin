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

package free.util.swing;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * A dialog which lets the user choose a background/wallpaper for a given
 * AdvancedJDesktopPane. To use a BackgroundChooser, simply instantiate
 * it and invoke <code>setVisible(true)</code> on it.
 */

public class BackgroundChooser extends JDialog{



  /**
   * The AdvancedJDesktopPane we're working with.
   */

  private final AdvancedJDesktopPane desktop;




  /**
   * The default wallpaper image.
   */

  private final Image defaultImage;




  /**
   * The default wallpaper layout style.
   */

  private final int defaultImageLayoutStyle;




  /**
   * The default background color.
   */

  private final Color defaultColor;




  /**
   * The file chosen for the wallpaper, or null if it's the default.
   */

  private File chosenImageFile = null;




  /**
   * The chosen background image layout style, or -1 if it's the default.
   */

  private int chosenImageLayoutStyle = -1;



  
  /**
   * The chosen background color, or null if it's the default.
   */

  private Color chosenColor = null;




  /**
   * Creates a new BackgroundChooser dialog which will allow selecting a
   * background/wallpaper for the given AdvancedJDesktopPane.
   */

  public BackgroundChooser(Frame parent, AdvancedJDesktopPane desktop, Image defaultImage,
      int defaultImageLayoutStyle, Color defaultColor){
    super(parent, "Pick background", true);

    this.desktop = desktop;
    this.defaultImage = defaultImage;
    this.defaultImageLayoutStyle = defaultImageLayoutStyle;
    this.defaultColor = defaultColor;

    createUI();

    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }





  /**
   * Adds the UI components to the dialog.
   */
  
  private void createUI(){
    JPanel contentPane = new JPanel(new BorderLayout(10, 10));
    setContentPane(contentPane);
    contentPane.setBorder(new javax.swing.border.EmptyBorder(15, 10, 15, 10));

    JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
    mainPanel.setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));

    JButton pickColor = new JButton("Pick color");
    JButton pickImage = new JButton("Pick image");
    JButton useDefault = new JButton("Use default");

    int curStyle = desktop.getWallpaperLayoutStyle();
    final JRadioButton tileButton = new JRadioButton("tile", curStyle == AdvancedJDesktopPane.TILE);
    final JRadioButton stretchButton = new JRadioButton("stretch", curStyle == AdvancedJDesktopPane.STRETCH);
    final JRadioButton centerButton = new JRadioButton("center", curStyle == AdvancedJDesktopPane.CENTER);

    ButtonGroup group = new ButtonGroup();
    group.add(tileButton);
    group.add(stretchButton);
    group.add(centerButton);

    JPanel boxesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    boxesPanel.add(tileButton);
    boxesPanel.add(stretchButton);
    boxesPanel.add(centerButton);

    JButton closeButton = new JButton("Close");

    mainPanel.add(pickColor);
    mainPanel.add(pickImage);
    mainPanel.add(boxesPanel);
    mainPanel.add(useDefault);

    JPanel closeButtonPanel = new JPanel(new GridLayout(1,1));
    closeButtonPanel.setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));
    closeButtonPanel.add(closeButton);

    contentPane.add(BorderLayout.NORTH, mainPanel);
    contentPane.add(BorderLayout.CENTER, new JSeparator());
    contentPane.add(BorderLayout.SOUTH, closeButtonPanel);

    pickColor.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        final JColorChooser colorChooser = createColorChooser();

        ActionListener okListener = new ActionListener(){

          public void actionPerformed(ActionEvent evt){
            desktop.setBackground(colorChooser.getColor());
            chosenColor = colorChooser.getColor();
          }

        };

        JDialog dialog = JColorChooser.createDialog(BackgroundChooser.this, "Choose background color", true, colorChooser, okListener, null);
        dialog.show();
      }

    });

    final JFileChooser fileChooser = createImageFileChooser();

    pickImage.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        fileChooser.showOpenDialog(BackgroundChooser.this);
        File pic = fileChooser.getSelectedFile();
        if (pic != null){
          chosenImageFile = pic;
          desktop.setWallpaper(Toolkit.getDefaultToolkit().getImage(pic.getAbsolutePath()));
        }
      }

    });


    useDefault.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        chosenColor = null;
        chosenImageFile = null;
        chosenImageLayoutStyle = -1;

        desktop.setBackground(defaultColor);
        desktop.setWallpaper(defaultImage);
        desktop.setWallpaperLayoutStyle(defaultImageLayoutStyle);
      }

    });


    ActionListener imageLayoutListener = new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        Object src = evt.getSource();
        int style = -1;
        if (src == tileButton)
          style = AdvancedJDesktopPane.TILE;
        else if (src == stretchButton)
          style = AdvancedJDesktopPane.STRETCH;
        else if (src == centerButton)
          style = AdvancedJDesktopPane.CENTER;

        desktop.setWallpaperLayoutStyle(style);
        chosenImageLayoutStyle = style;
      }

    };

    tileButton.addActionListener(imageLayoutListener);
    stretchButton.addActionListener(imageLayoutListener);
    centerButton.addActionListener(imageLayoutListener);

    closeButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        dispose();
      }

    });
  }






  /**
   * Creates a JColorChooser.
   */

  private JColorChooser createColorChooser(){
    final JColorChooser chooser = new JColorChooser(desktop.getBackground());

    JPanel colorPreview = new JPanel(new FlowLayout(FlowLayout.CENTER));
    final JPanel colorBox = new JPanel();
    colorBox.setOpaque(true);
    colorBox.setPreferredSize(new Dimension(100, 100));
    colorBox.setBorder(new javax.swing.border.LineBorder(Color.black, 1));
    colorPreview.add(colorBox);

    chooser.setPreviewPanel(colorPreview);
    chooser.getSelectionModel().addChangeListener(new ChangeListener(){

      public void stateChanged(ChangeEvent evt){
        colorBox.setBackground(chooser.getColor());
      }

    });

    return chooser;
  }




  /**
   * Creates a file chooser for choosing the background image.
   */

  private JFileChooser createImageFileChooser(){
    JFileChooser fileChooser = new JFileChooser();

    String [] supportedImageTypes;
    if (System.getProperty("java.version").compareTo("1.3") >= 0)
      supportedImageTypes = new String[]{".gif", ".jpg", ".jpeg", ".png"};
    else
      supportedImageTypes = new String[]{".gif", ".jpg", ".jpeg"};

    fileChooser.setFileFilter(new ExtensionFileFilter("Image files", supportedImageTypes));
    fileChooser.setFileHidingEnabled(true);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);

    return fileChooser;
  }






  /**
   * Overrides setVisible(boolean) to center the dialog before showing it.
   */

  public void setVisible(boolean visible){
    if (visible){
      center();
      setResizable(false);
    }

    super.setVisible(visible);
  }





  /**
   * Returns the chosen background color, or null if the user chose the default.
   */

  public Color getChosenColor(){
    return chosenColor;
  }





  /**
   * Returns the chosen background image file, or null if the user chose the
   * default.
   */

  public File getChosenImageFile(){
    return chosenImageFile;
  }




  /**
   * Returns the chosen background image layout style, or -1 if the user
   * chose the default (no, this does not really make sense, it's just for
   * consistency).
   */

  public int getChosenImageLayoutStyle(){
    return chosenImageLayoutStyle;
  }




  /**
   * Centers the dialog over its parent.
   */

  private void center(){
    pack();
    Dimension size = getSize();
    Rectangle parentBounds = getParent().getBounds();
    setLocation(parentBounds.x + (parentBounds.width - size.width)/2, parentBounds.y + (parentBounds.height - size.height)/2);
  }


}