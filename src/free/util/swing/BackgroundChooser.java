/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.util.AWTUtilities;
import free.util.Localization;
import free.util.PlatformUtils;


/**
 * A dialog which lets the user choose a background/wallpaper for a given
 * AdvancedJDesktopPane. To use a BackgroundChooser, simply instantiate
 * it and invoke <code>setVisible(true)</code> on it. The chosen settings are
 * applied to the specified desktop pane immediately upon selection.
 */

public class BackgroundChooser extends JDialog{
  
  
  
  /**
   * The localization for this class, loaded lazily.
   */
  
  private static Localization l10n;
  
  
  
  /**
   * The AdvancedJDesktopPane we're working with.
   */

  private final AdvancedJDesktopPane desktop;


  
  /**
   * The default wallpaper color.
   */
   
  private final Color defaultColor;


  /**
   * The default wallpaper image file.
   */

  private final File defaultImageFile;


  
  /**
   * The default wallpaper layout style.
   */

  private final int defaultImageLayoutStyle;



  /**
   * The currently chosen wallpaper color.
   */

  private Color color;
  
  
  
  /**
   * The currently chosen wallpaper image file.
   */
   
  private File imageFile;



  /**
   * The chosen background image layout style.
   */

  private int imageLayoutStyle;



  /**
   * Creates a new BackgroundChooser dialog which will allow selecting a
   * background/wallpaper for the given AdvancedJDesktopPane.
   *
   * @param parent The parent frame for the dialog.
   * @param desktop The <code>AdvancedJDesktopPane</code> to which the choices
   *   are applied. May be <code>null</code>, in which case the choices won't be
   *   applied.
   * @param defaultColor The background color used when the "Use defaults"
   *   button is pressed. Use <code>null</code> for the color specified by the
   *   UIManager.
   * @param defaultImageFile The image file used when the "Use defaults" button
   *   is pressed. Use <code>null</code> for no image.
   * @param defaultImageLayoutStyle The image layout style used when the
   *   "Use defaults" button is pressed.
   * @param color The current background color of the desktop pane.
   * @param imageFile The current image file.
   * @param imageLayoutStyle The current image layout style.
   */

  public BackgroundChooser(Frame parent, AdvancedJDesktopPane desktop,
      Color defaultColor, File defaultImageFile, int defaultImageLayoutStyle,
      Color color, File imageFile, int imageLayoutStyle){
    super(parent, getL10n().getString("title"), true); //$NON-NLS-1$

    this.desktop = desktop;
    
    this.defaultColor = defaultColor;
    this.defaultImageFile = defaultImageFile;
    this.defaultImageLayoutStyle = defaultImageLayoutStyle;

    this.color = color;
    this.imageFile = imageFile;
    this.imageLayoutStyle = imageLayoutStyle;

    createUI();

    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    SwingUtils.registerEscapeCloser(this);
  }
  
  
  
  /**
   * Returns the localization for this class.
   */
  
  private static synchronized Localization getL10n(){
    if (l10n == null)
      l10n = LocalizationService.getForClass(BackgroundChooser.class);
    return l10n;
  }



  /**
   * Adds the UI components to the dialog.
   */
  
  private void createUI(){
    boolean allowImageSelection = hasAccessToFileSystem();
    
    JPanel contentPane = new JPanel(new BorderLayout(10, 10));
    setContentPane(contentPane);
    contentPane.setBorder(new javax.swing.border.EmptyBorder(15, 10, 15, 10));

    JPanel mainPanel = new JPanel(new GridLayout(allowImageSelection ? 4 : 2, 1, 10, 10));
    mainPanel.setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));

    JButton pickColor = new JButton();
    JButton pickImage = new JButton();
    JButton useDefault = new JButton();
    
    SwingUtils.applyLabelSpec(pickColor, getL10n().getString("pickColorButton.text")); //$NON-NLS-1$
    SwingUtils.applyLabelSpec(pickImage, getL10n().getString("pickImageButton.text")); //$NON-NLS-1$
    SwingUtils.applyLabelSpec(useDefault, getL10n().getString("useDefaultsButton.text")); //$NON-NLS-1$

    pickColor.setDefaultCapable(false);
    pickImage.setDefaultCapable(false);
    useDefault.setDefaultCapable(false);

    final JRadioButton tileButton = new JRadioButton();
    final JRadioButton scaleButton = new JRadioButton();
    final JRadioButton centerButton = new JRadioButton();
    
    SwingUtils.applyLabelSpec(tileButton, getL10n().getString("tileRadioButton.text")); //$NON-NLS-1$
    SwingUtils.applyLabelSpec(scaleButton, getL10n().getString("scaleRadioButton.text")); //$NON-NLS-1$
    SwingUtils.applyLabelSpec(centerButton, getL10n().getString("centerRadioButton.text")); //$NON-NLS-1$
    
    switch (imageLayoutStyle){
      case AdvancedJDesktopPane.TILE: tileButton.setSelected(true); break;
      case AdvancedJDesktopPane.SCALE: scaleButton.setSelected(true); break;
      case AdvancedJDesktopPane.CENTER: centerButton.setSelected(true); break;
    }

    ButtonGroup group = new ButtonGroup();
    group.add(tileButton);
    group.add(scaleButton);
    group.add(centerButton);

    JPanel boxesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    boxesPanel.add(tileButton);
    boxesPanel.add(scaleButton);
    boxesPanel.add(centerButton);

    mainPanel.add(pickColor);
    if (allowImageSelection){
      mainPanel.add(pickImage);
      mainPanel.add(boxesPanel);
    }
    mainPanel.add(useDefault);

    JButton closeButton = new JButton(getL10n().getString("closeButton.text")); //$NON-NLS-1$
    JPanel closeButtonPanel = new JPanel(new GridLayout(1,1));
    closeButtonPanel.setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));
    closeButtonPanel.add(closeButton);

    contentPane.add(mainPanel, BorderLayout.NORTH);
    contentPane.add(new JSeparator(), BorderLayout.CENTER);
    contentPane.add(closeButtonPanel, BorderLayout.SOUTH);

    pickColor.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        final JColorChooser colorChooser = createColorChooser();

        ActionListener okListener = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
            Color clr = colorChooser.getColor();
            
            if (clr != null)
              color = clr;
           
            if (color == null)
              desktop.setBackground(UIManager.getColor("desktop")); //$NON-NLS-1$
            else
              desktop.setBackground(color);
          }
        };
        
        ActionListener cancelListener = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
            if (color == null)
              desktop.setBackground(UIManager.getColor("desktop")); //$NON-NLS-1$
            else
              desktop.setBackground(color);
          }
        };

        JDialog dialog = JColorChooser.createDialog(BackgroundChooser.this,
            getL10n().getString("bgColorChooser.title"), //$NON-NLS-1$
          true, colorChooser, okListener, cancelListener);
        dialog.setVisible(true);
      }

    });
    
    if (allowImageSelection){
      final JFileChooser fileChooser = createImageFileChooser();
  
      pickImage.addActionListener(new ActionListener(){
  
        public void actionPerformed(ActionEvent evt){
          int val = fileChooser.showOpenDialog(BackgroundChooser.this);
          File pic = (val == JFileChooser.APPROVE_OPTION) ?
            fileChooser.getSelectedFile() : null;
            
          if ((pic != null) && pic.exists())
            imageFile = pic;
          
          if (imageFile == null)
            desktop.setWallpaper(null);
          else
            desktop.setWallpaper(desktop.getToolkit().getImage(imageFile.getAbsolutePath()));
        }
  
      });
  
  
      useDefault.addActionListener(new ActionListener(){
  
        public void actionPerformed(ActionEvent evt){
          color = defaultColor;
          imageFile = defaultImageFile;
          imageLayoutStyle = defaultImageLayoutStyle;
  
          if (color == null)
            desktop.setBackground(UIManager.getColor("desktop")); //$NON-NLS-1$
          else
            desktop.setBackground(color);
          
            if (imageFile == null)
              desktop.setWallpaper(null);
            else
              desktop.setWallpaper(desktop.getToolkit().getImage(imageFile.getAbsolutePath()));
            
          desktop.setWallpaperLayoutStyle(imageLayoutStyle);
          
          switch(imageLayoutStyle){
            case AdvancedJDesktopPane.CENTER: centerButton.setSelected(true); break; 
            case AdvancedJDesktopPane.TILE: tileButton.setSelected(true); break; 
            case AdvancedJDesktopPane.SCALE: scaleButton.setSelected(true); break; 
          }
        }
  
      });
  
  
      ActionListener imageLayoutListener = new ActionListener(){
  
        public void actionPerformed(ActionEvent evt){
          Object src = evt.getSource();
          if (src == tileButton)
            imageLayoutStyle = AdvancedJDesktopPane.TILE;
          else if (src == scaleButton)
            imageLayoutStyle = AdvancedJDesktopPane.SCALE;
          else if (src == centerButton)
            imageLayoutStyle = AdvancedJDesktopPane.CENTER;
  
          desktop.setWallpaperLayoutStyle(imageLayoutStyle);
        }
  
      };
  
      tileButton.addActionListener(imageLayoutListener);
      scaleButton.addActionListener(imageLayoutListener);
      centerButton.addActionListener(imageLayoutListener);
    }

    closeButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        dispose();
      }

    });


    getRootPane().setDefaultButton(closeButton);
  }
  
  
  
  /**
   * Determines whether we have access to the filesystem.
   */
   
  private boolean hasAccessToFileSystem(){
    try{
      SecurityManager securityManager = System.getSecurityManager();
      if (securityManager != null)
        securityManager.checkRead(System.getProperty("user.home")); //$NON-NLS-1$
    } catch (SecurityException e){return false;}
    
    return true;
  }



  /**
   * Creates a JColorChooser.
   */

  private JColorChooser createColorChooser(){
    final JColorChooser chooser = new JColorChooser(desktop.getBackground());

    chooser.setPreviewPanel(new JPanel());
    chooser.getSelectionModel().addChangeListener(new ChangeListener(){

      public void stateChanged(ChangeEvent evt){
        Color color = chooser.getColor();
        if (color != null)
          desktop.setBackground(color);
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
    if (PlatformUtils.isJavaBetterThan("1.3")) //$NON-NLS-1$
      supportedImageTypes = new String[]{".gif", ".jpg", ".jpeg", ".png"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    else
      supportedImageTypes = new String[]{".gif", ".jpg", ".jpeg"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    fileChooser.setFileFilter(new ExtensionFileFilter(
        getL10n().getString("fileFilterName"), supportedImageTypes, false)); //$NON-NLS-1$
    fileChooser.setFileHidingEnabled(true);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    
    fileChooser.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        String prop = evt.getPropertyName();
        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)){
          File file = (File)evt.getNewValue();
          if (file != null){
            Image img = desktop.getToolkit().getImage(file.getAbsolutePath());
            desktop.setWallpaper(img);
          }
        }        
      }
    });

    return fileChooser;
  }



  /**
   * Overrides setVisible(boolean) to center the dialog before showing it.
   */

  public void setVisible(boolean visible){
    if (visible)
      AWTUtilities.centerWindow(this, getParent());

    super.setVisible(visible);
  }



  /**
   * Returns the chosen background color.
   */

  public Color getColor(){
    return color;
  }



  /**
   * Returns the chosen background image file.
   */

  public File getImageFile(){
    return imageFile;
  }



  /**
   * Returns the chosen background image layout style.
   */

  public int getImageLayoutStyle(){
    return imageLayoutStyle;
  }
  


}
