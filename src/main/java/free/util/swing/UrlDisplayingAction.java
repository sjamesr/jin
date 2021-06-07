/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2003 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util.swing;

import free.util.BrowserControl;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;

/** An <code>Action</code>, which, when activated, opens a browser at a specified url. */
public class UrlDisplayingAction extends AbstractAction {

  /** The URL. */
  private String url;

  /** Creates a new <code>UrlDisplayingAction</code> with the specified URL. */
  public UrlDisplayingAction(String url) {
    this(url, null, null);
  }

  /** Creates a new <code>UrlDisplayingAction</code> with the specified URL and name. */
  public UrlDisplayingAction(String url, String name) {
    this(url, name, null);
  }

  /** Creates a new <code>UrlDisplayingAction</code> with the specified URL, name and icon. */
  public UrlDisplayingAction(String url, String name, Icon icon) {
    super(name, icon);

    setUrl(url);
  }

  /**
   * Sets the URL displayed when the action is invoked. If <code>url</code> is <code>null</code>,
   * nothing is done when the action is invoked.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /** Returns the URL. */
  public String getURL() {
    return url;
  }

  /**
   * Tries to show the url using <code>BrowserControl</code>. Displays an error message to the user
   * if fails.
   */
  @Override
  public void actionPerformed(ActionEvent evt) {
    if (url == null) return;

    if (!BrowserControl.displayURL(getURL())) {
      Object source = evt.getSource();
      Component parent = null;
      if (source instanceof Component) parent = SwingUtils.frameForComponent((Component) source);

      BrowserControl.showDisplayBrowserFailedDialog(getURL(), parent, true);
    }
  }
}
