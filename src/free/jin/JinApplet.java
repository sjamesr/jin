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

package free.jin;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import free.util.BrowserControl;
import free.util.PlatformUtils;
import free.util.audio.AppletContextAudioPlayer;



/**
 * An applet which runs Jin, via a <code>AppletJinContext</code>. This is just
 * a small class responsible for creating an <code>AppletJinContext</code> and
 * passing it the various applet events (start, stop, destroy).
 */

public class JinApplet extends JApplet implements ActionListener{
  
  
  
  /**
   * The <code>AppletJinContext</code> we're running Jin with.
   */
   
  private AppletJinContext context;
  
  
  
  /**
   * A button which starts and stops Jin.
   */
   
  private JButton startStopButton;
  
  
  
  /**
   * A label that informs the user that the applet is starting.
   */
   
  private JLabel startingLabel1;
  
  
  
  /**
   * Another label, telling the user that starting Jin may take a while :-)
   */
   
  private JLabel startingLabel2;
  
  
  
  /**
   * The text on the start/stop button when it starts Jin. 
   */
  
  private final String START_JIN_TEXT = "Start JinApplet";
  
  
  
  /**
   * The text on the start/stop button when it stops Jin. 
   */
  
  private final String STOP_JIN_TEXT = "Stop JinApplet";
  
  


  /**
   * Creates an AppletJinContext.
   */
   
  public void init(){
    super.init();
    
    BrowserControl.setAppletContext(getAppletContext());
    AppletContextAudioPlayer.setAppletContext(getAppletContext());
    
    startStopButton = new JButton(START_JIN_TEXT);
    startingLabel1 = new JLabel("");
    startingLabel2 = new JLabel("");
    createUI();
    
    startStopButton.addActionListener(this);
  }
  
  
  
  /**
   * <code>ActionListener</code> implementation for the start/stop button.
   */
   
  public void actionPerformed(ActionEvent evt){
    if (startStopButton.getText().equals(START_JIN_TEXT)){
      startStopButton.setText(STOP_JIN_TEXT);
      startingLabel1.setText("Starting Jin, please wait...");
      startingLabel2.setText("(this may take a few minutes)");
        
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          startJin();
        }
      });
    }
    else if (startStopButton.getText().equals(STOP_JIN_TEXT)){
      context.quit(false);
    }
    else
      throw new IllegalStateException("Wrong text on start/stop button");
  }
  
  
  
  /**
   * Creates and starts <code>AppletJinContext</code>.
   */
   
  private void startJin(){
    try{
      context = new AppletJinContext(this);
      context.start();
      startingLabel1.setText("");
      startingLabel2.setText("");
    } catch (Throwable t){
        t.printStackTrace();
        createErrorUI(t);
      }
  }
  
  
  
  /**
   * This method is called by AppletJinContext when Jin is closed.
   */
   
  void closed(){
    context = null;
    
    startStopButton.setEnabled(false);
   
    // Reload the webpage, avoiding cache.
    // try{
    //   String documentBase = getDocumentBase().toExternalForm();
    //   int qIndex = documentBase.lastIndexOf("?");  
    //   if ((qIndex != -1) && documentBase.substring(qIndex).startsWith("?rnd="))
    //     documentBase = documentBase.substring(0, qIndex);
      
    //   String url = documentBase + "?rnd=" + Math.random();
    //   getAppletContext().showDocument(new URL(url), "_self");
    // } catch (MalformedURLException e){e.printStackTrace();}
    
    getAppletContext().showDocument(getDocumentBase(), "_self");
    
    // We can't properly restart Jin because we're not using/loading the new
    // preferences.
    // startStopButton.setText(START_JIN_TEXT);
  }
   
  
  
  
  /**
   * Creates the user interface of the applet.
   */
   
  private void createUI(){
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    
    JLabel label1 = new JLabel("Do not leave this page or close the browser while");
    JLabel label2 = new JLabel("JinApplet is running - doing so will cause it to");
    JLabel label3 = new JLabel("be closed immediately, losing all information within.");
    
    label1.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    label2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    label3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    JPanel labelsBox = new JPanel();
    labelsBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    labelsBox.setLayout(new BoxLayout(labelsBox, BoxLayout.Y_AXIS));
    labelsBox.add(label1);
    labelsBox.add(label2);
    labelsBox.add(label3);
    
    contentPane.add(Box.createVerticalGlue());
    contentPane.add(labelsBox);
    contentPane.add(Box.createVerticalStrut(30));
    
    startStopButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    contentPane.add(startStopButton);
    
    startingLabel1.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    startingLabel2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    JPanel startingLabelBox = new JPanel();
    startingLabelBox.setLayout(new BoxLayout(startingLabelBox, BoxLayout.Y_AXIS));
    startingLabelBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    startingLabelBox.add(startingLabel1);
    startingLabelBox.add(startingLabel2);
    
    
    contentPane.add(Box.createVerticalStrut(30));
    contentPane.add(startingLabelBox);
    contentPane.add(Box.createVerticalGlue());
    
    setContentPane(contentPane);
  }
  
  
  
  /**
   * Creates UI which informs the user that the specified error has occurred.
   */
   
  private void createErrorUI(Throwable t){
    // The UI is AWT because we need native components so 
    // that the user can copy/paste the error text.
    setRootPaneCheckingEnabled(false);
    
    removeAll();
    
    setLayout(new BorderLayout());
    
    add(new Label("An error has occurred when running Jin:"), BorderLayout.NORTH);
    
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(buf));
    TextArea stackTraceArea = new TextArea(buf.toString());

    add(stackTraceArea, BorderLayout.CENTER);
    doLayout();
  }
  


  
  /**
   * Invokes the context's <code>applet_start</code> method.
   */
  
  public void start(){
    super.start();
    
    if (context != null)
      context.applet_start();
  }
  
  
  
  
  /**
   * Invokes the context's <code>applet_stop</code> method.
   */
   
  public void stop(){
    super.stop();
    
    if (context != null)
      context.applet_stop();
  }
  
  
  
}

