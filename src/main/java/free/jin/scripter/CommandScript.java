/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.scripter;

import bsh.EvalError;
import bsh.Interpreter;
import free.jin.Connection;
import free.jin.event.JinEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <code>Script</code> which sends a list of specified commands to the server if a specified
 * BeanShell expression evaluates to <code>true</code>.
 */
public class CommandScript extends Script {

  /** The condition on which the commands are executed. */
  private final String condition;

  /** The commands to be executed if the condition evaluates to <code>true</code>. */
  private final String[] commands;

  /** The <code>Interpreter</code> that will run the code. */
  private final Interpreter bsh;

  /**
   * Creates a new <code>CommandScript</code> which will send the specified list of commands to the
   * server if the specified BeanShell expression evaluates to <code>true</code>.
   *
   * @throws EvalError if the specified condition isn't a valid BeanShell expression.
   */
  public CommandScript(
      Scripter scripter,
      String name,
      String eventType,
      String[] eventSubtypes,
      String condition,
      String[] commands)
      throws EvalError {
    super(scripter, name, eventType, eventSubtypes);

    this.condition = condition;
    this.commands = commands.clone();

    bsh = new Interpreter();

    bsh.set("scripter", scripter);
    bsh.set("prefs", scripter.getPrefs());
    bsh.set("connection", scripter.getConn());

    addImports(bsh);
  }

  /** Evaluates all the imports needed by the scripts in the specified <code>Interpreter</code>. */
  private static void addImports(Interpreter bsh) throws EvalError {
    bsh.eval("import free.jin.*;");
    bsh.eval("import free.jin.event.*");
    bsh.eval("import free.chess.*");
  }

  /** Returns the string "commands". */
  @Override
  public String getType() {
    return "commands";
  }

  /** Returns the condition on which the commands are executed. */
  public String getCondition() {
    return condition;
  }

  /**
   * Returns the list of commands to be executed if the condition evaluates to <code>true</code>.
   * Note that the returned array is a copy.
   */
  public String[] getCommands() {
    return (commands.clone());
  }

  /** Preprocesses the specified server command, replacing any variable names with their values. */
  private String preprocess(String code, Object[][] vars) {
    // Sort by length and replace longer strings first.
    // Without this, something like $gameType will get recognized as ($game)Type
    Collections.sort(
        Arrays.asList(vars),
        new Comparator() {
          @Override
          public int compare(Object v1, Object v2) {
            Object[] var1 = (Object[]) v1;
            Object[] var2 = (Object[]) v2;
            String varName1 = (String) var1[0];
            String varName2 = (String) var2[0];

            return varName2.length() - varName1.length();
          }
        });

    for (int i = 0; i < vars.length; i++) {
      Object var[] = vars[i];
      String varName = (String) var[0];
      String varValue = String.valueOf(var[1]);

      Pattern pattern = Pattern.compile("\\$" + varName);
      Matcher replacer = pattern.matcher(code);
      code = replacer.replaceAll(varValue);
    }

    return code;
  }

  /** Runs the script. */
  @Override
  public void run(JinEvent evt, String eventSubtype, Object[][] vars) {
    try {
      bsh.set("event", evt);
      bsh.set("eventSubtype", eventSubtype);

      for (int i = 0; i < vars.length; i++) {
        Object[] var = vars[i];
        String varName = (String) (var[0]);
        Object varValue = var[1];
        bsh.set(varName, varValue);
      }
      boolean result = ((Boolean) bsh.eval(condition)).booleanValue();
      if (!result) return;

      Connection conn = scripter.getConn();
      for (int i = 0; i < commands.length; i++) {
        String line = preprocess(commands[i], vars);
        conn.sendCommand(line);
      }

    } catch (EvalError e) {
      // Shouldn't happen
      e.printStackTrace();
    }
  }

  /** Returns a copy of this Script. */
  @Override
  public Script createCopy() {
    try {
      CommandScript script =
          new CommandScript(
              scripter,
              getName(),
              getEventType(),
              getEventSubtypes(),
              getCondition(),
              getCommands());
      script.setEnabled(isEnabled());
      return script;
    } catch (EvalError e) {
      e.printStackTrace();
      throw new IllegalStateException("EvalError while cloning an existing CommandScript!!!");
    }
  }
}
