/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import free.util.AWTUtilities;


/**
 * A panel allowing the user to select a font from a given list of fonts.
 */

public class FontSelectorPanel extends JPanel{



  /**
   * The textfield showing the currently selected font name.
   */

  private final JTextField fontNameField = new free.workarounds.FixedJTextField();




  /**
   * The list displaying possible font names.
   */

  private final JList fontNamesList;




  /**
   * The textfield showing the currently selected font size.
   */

  private final JTextField fontSizeField = new free.workarounds.FixedJTextField(new IntegerStrictPlainDocument(1, 72), "", 2);




  /**
   * The list displaying possible font sizes.
   */

  private final JList fontSizesList = new JList(new String[]{"5", "6", "7", "8", "9",
    "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"});




  /**
   * The checkbox for choosing whether the font is bold or not.
   */

  private final JCheckBox boldCheckBox;




  /**
   * The checkbox for choosing whether the font is italic or not.
   */

  private final JCheckBox italicCheckBox;





  /**
   * The preview panel.
   */

  private JComponent previewPanel;





  /**
   * The preview panel holder;
   */

  private final JPanel previewPanelHolder;





  /**
   * The sole ChangeEvent we need.
   */

  private final ChangeEvent changeEvent = new ChangeEvent(this);






  /**
   * Creates a new FontSelectorPanel which will allow choosing from the list
   * of fonts available to the default Toolkit.
   */

  public FontSelectorPanel(){
    this(AWTUtilities.getAvailableFontNames());
  }


  

  /**
   * Creates a new FontSelectorPanel which will allow choosing from the
   * specified list of fonts.
   */

  public FontSelectorPanel(String [] fontNames){
    fontNamesList = new JList(fontNames);
    fontNamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontSizesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    boldCheckBox = new JCheckBox("Bold");
    italicCheckBox = new JCheckBox("Italic");

    boldCheckBox.setMnemonic('B');
    italicCheckBox.setMnemonic('I');

    previewPanelHolder = new JPanel(new BorderLayout());
    Border outsideBorder = new EtchedBorder(javax.swing.border.EtchedBorder.LOWERED);
    Border insideBorder = new EmptyBorder(10, 10, 10, 10);
    previewPanelHolder.setBorder(new CompoundBorder(outsideBorder, insideBorder));

    setPreviewPanel(new DefaultPreviewPanel(this));

    createUI();

    fontNamesList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        String selectedItem = (String)fontNamesList.getSelectedValue();
        fontNameField.setText(selectedItem);

        fireStateChanged();
      }
    });

    fontSizesList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt){
        String selectedItem = (String)fontSizesList.getSelectedValue();
        fontSizeField.setText(selectedItem);

        fireStateChanged();
      }
    });

    ChangeListener checkBoxChangeListener = new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        fireStateChanged();
      }
    };

    boldCheckBox.addChangeListener(checkBoxChangeListener);
    italicCheckBox.addChangeListener(checkBoxChangeListener);
  }





  /**
   * Creates the UI.
   */

  private void createUI(){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel topPanel = new JPanel(new BorderLayout(5, 5));

    JPanel fontNamePanel = new JPanel(new BorderLayout());
    JScrollPane fontNamesListScrollPane = new JScrollPane(fontNamesList);
    JPanel fontNameLabelAndField = new JPanel(new BorderLayout(2, 2));
    JLabel fontNameLabel = new JLabel("Font name", JLabel.CENTER);
    fontNameLabel.setDisplayedMnemonic('n');
    fontNameLabel.setLabelFor(fontNameField);
    fontNameLabelAndField.add(BorderLayout.NORTH, fontNameLabel);
    fontNameLabelAndField.add(BorderLayout.SOUTH, fontNameField);
    fontNamePanel.add(BorderLayout.NORTH, fontNameLabelAndField);
    fontNamePanel.add(BorderLayout.CENTER, fontNamesListScrollPane);

    JPanel fontSizePanel = new JPanel(new BorderLayout());
    JScrollPane fontSizesListScrollPane = new JScrollPane(fontSizesList);
    JPanel fontSizeLabelAndField = new JPanel(new BorderLayout(2, 2));
    JLabel fontSizeLabel = new JLabel("Font size", JLabel.CENTER);
    fontSizeLabel.setDisplayedMnemonic('s');
    fontSizeLabel.setLabelFor(fontSizeField);
    fontSizeLabelAndField.add(BorderLayout.NORTH, fontSizeLabel);
    fontSizeLabelAndField.add(BorderLayout.SOUTH, fontSizeField);
    fontSizePanel.add(BorderLayout.NORTH, fontSizeLabelAndField);
    fontSizePanel.add(BorderLayout.CENTER, fontSizesListScrollPane);

    topPanel.add(BorderLayout.CENTER, fontNamePanel);
    topPanel.add(BorderLayout.EAST, fontSizePanel);

    JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

    Box checkBoxesPanel = Box.createVerticalBox();
    checkBoxesPanel.add(boldCheckBox);
    checkBoxesPanel.add(italicCheckBox);

    bottomPanel.add(BorderLayout.WEST, checkBoxesPanel);
    bottomPanel.add(BorderLayout.CENTER, previewPanelHolder);

    add(topPanel);
    add(Box.createVerticalStrut(10));
    add(bottomPanel);
  }




  /**
   * Returns the currently selected font, or null if none.
   */

  public Font getSelectedFont(){
    String fontName = fontNameField.getText();
    String fontSizeString = fontSizeField.getText();

    if ((fontName == null) || fontName.equals(""))
      return null;

    if ((fontSizeString == null) || fontSizeString.equals(""))
      return null;

    int style = 0;
    if (boldCheckBox.isSelected())
      style |= Font.BOLD;
    if (italicCheckBox.isSelected())
      style |= Font.ITALIC;

    return new Font(fontName, style, Integer.parseInt(fontSizeString));
  }




  /**
   * Sets the currently selected font.
   */

  public void setSelectedFont(Font font){
    fontNameField.setText(font.getFamily());
    fontSizeField.setText(String.valueOf(font.getSize()));
    boldCheckBox.setSelected(font.isBold());
    italicCheckBox.setSelected(font.isItalic());

    fontNamesList.setSelectedValue(font.getFamily(), true);
    fontSizesList.setSelectedValue(String.valueOf(font.getSize()), true);

    fireStateChanged();
  }




  /**
   * Adds a ChangeListener to the list of listeners receiving notifications when
   * the selected font changes.
   */

  public void addChangeListener(ChangeListener listener){
    listenerList.add(ChangeListener.class, listener);
  }




  /**
   * Removes the given Changelistener from the list of listeners receiving
   * notifications when the selected font changes.
   */

  public void removeChangeListener(ChangeListener listener){
    listenerList.remove(ChangeListener.class, listener);
  }




  /**
   * Fires a ChangeEvent to all interested listeners.
   */

  protected void fireStateChanged(){
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ChangeListener.class){
        ChangeListener listener = (ChangeListener)listeners[i+1];
        listener.stateChanged(changeEvent);
      }
    }
  }





  /**
   * Sets the preview panel.
   */

  public void setPreviewPanel(JComponent component){
    if (previewPanel != null)
      previewPanelHolder.remove(previewPanel);

    previewPanel = component;
    if (previewPanel != null)
      previewPanelHolder.add(BorderLayout.CENTER, previewPanel);
  }




  /**
   * Returns the preview panel.
   */

  public JComponent getPreviewPanel(){
    return previewPanel;
  }




  /**
   * The default preview panel.
   */

  public static class DefaultPreviewPanel extends JLabel implements ChangeListener{



    /**
     * The FontSelectorPanel we're a part of.
     */

    private FontSelectorPanel parent;




    /**
     * Creates a new DefaultPreviewPanel with the specified FontSelectorPanel
     * as the parent. 
     */

    public DefaultPreviewPanel(FontSelectorPanel parent){
      super("AaBbYyZz123", JLabel.CENTER);

      this.parent = parent;

      parent.addChangeListener(this);

      setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }




    /**
     * ChangeListener implementation.
     */

    public void stateChanged(ChangeEvent evt){
      setFont(parent.getSelectedFont());
      repaint();
    }

  }


}
 