/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin.scripter;

import bsh.Interpreter;
import bsh.EvalError;
import free.jin.event.JinEvent;


/**
 * A <code>JinEventListener</code> implementation which runs the specified
 * piece of BeanShell code.
 */

public class BeanShellScript extends Script{



  /**
   * The code.
   */

  private final String code;



  /**
   * The <code>Interpreter</code> that will run the code.
   */

  private final Interpreter bsh;




  /**
   * Creates a new <code>CodeExecutable</code> which will run the specified
   * code.
   */

  public BeanShellScript(Scripter scripter, String name, String eventType, String [] eventSubtypes,
      String code) throws EvalError{
    super(scripter, name, eventType, eventSubtypes);

    this.code = code;

    bsh = new Interpreter();
    addImports(bsh);
    addVariables(bsh, scripter);
    addMethods(bsh, scripter);
    
    bsh.eval("void runScript(){"+code+"}");
  }



  /**
   * Evaluates all the imports needed by the scripts in the specified
   * <code>Interpreter</code>.
   */

  private static void addImports(Interpreter bsh) throws EvalError{
    bsh.eval("import free.jin.*;");
    bsh.eval("import free.jin.event.*");
    bsh.eval("import free.chess.*");
  }



  /**
   * Adds all the "global" variables to the specified <code>Interpreter's</code>
   * environment.
   */

  private static void addVariables(Interpreter bsh, Scripter scripter) throws EvalError{
    bsh.set("scripter", scripter);
    bsh.set("prefs", scripter.getPrefs());
    bsh.set("connection", scripter.getConn());
  }
  
  
  
  /**
   * This is a method which is never called whose purpose is to ensure that all
   * the classes and methods used in this script via BeanShell actually exist.
   * If they are removed by mistake, a compile-time error should occur.
   */
   
  public static void ensureCorrectness(Scripter scripter) throws java.io.IOException{
    if ("".length() == 0)
      throw new Error("ensureCorrectness must never be invoked");
    
    scripter.getConn().sendCommand("Test");
    new free.util.audio.AudioClip(free.util.IOUtilities.fileToURL(new java.io.File("Test"))).play();
    ((free.jin.console.ConsoleManager)scripter.getPlugin("console")).addSpecialLine("Test");
  }




  /**
   * Adds all the "built-in" methods to the specified <code>Interpreter's</code>
   * environment.
   */

  private static void addMethods(Interpreter bsh, Scripter scripter) throws EvalError{
    bsh.eval("void sendCommand(String command){connection.sendCommand(command);}");

    bsh.eval("void playSound(String filename){\n"+
             "  File file = new java.io.File(filename);\n"+
             "  URL url = free.util.IOUtilities.fileToURL(file);\n"+
             "  if (url != null)\n"+
             "    new free.util.audio.AudioClip(url).play();\n"+
             "}");

    bsh.eval("void exec(String command){\n"+
             "  Runtime.getRuntime().exec(command);\n"+
             "}");

    bsh.eval("void appendLine(String line){\n"+
             "  free.jin.console.ConsoleManager plugin = scripter.getPlugin(\"console\");\n"+
             "  plugin.addSpecialLine(line);\n"+
             "}");
             
    // *************************************************************************
    // IMPORTANT: Whenever modifying this method, modify ensureCorrectness too *
    // *************************************************************************
  }




  /**
   * Returns the string "beanshell".
   */

  public String getType(){
    return "beanshell";
  }



  /**
   * Returns the code.
   */

  public String getCode(){
    return code;
  }



  /**
   * Runs the code.
   */

  public void run(JinEvent event, String eventSubtype, Object [][] vars){
    try{
      bsh.set("event", event);
      bsh.set("eventSubtype", eventSubtype);

      // Set the variables
      for (int i = 0; i < vars.length; i++){
        Object [] var = vars[i];
        String varName = (String)(var[0]);
        Object varValue = var[1];

        if (varValue instanceof Integer)
          bsh.set(varName, ((Integer)varValue).intValue());
        else if (varValue instanceof Long)
          bsh.set(varName, ((Long)varValue).longValue());
        else if (varValue instanceof Double)
          bsh.set(varName, ((Double)varValue).doubleValue());
        else if (varValue instanceof Float)
          bsh.set(varName, ((Float)varValue).floatValue());
        else if (varValue instanceof Boolean)
          bsh.set(varName, ((Boolean)varValue).booleanValue());
        else
          bsh.set(varName, varValue);
      }

      bsh.eval("runScript();");

      // Unset the variables so that they're not there the next time the script is run.
      for (int i = 0; i < vars.length; i++){
        Object [] var = vars[i];
        String varName = (String)(var[0]);
        bsh.unset(varName);
      }

    } catch (EvalError e){
        // Shouldn't happen
        e.printStackTrace();
      }
  }



  /**
   * Returns a copy of this Script.
   */

  public Script createCopy(){
    try{
      BeanShellScript script = new BeanShellScript(scripter, getName(), getEventType(),
        getEventSubtypes(), getCode());
      script.setEnabled(isEnabled());

      return script;
    } catch (EvalError e){
        e.printStackTrace();
        throw new IllegalStateException("EvalError while cloning an existing BeanShellScript!!!");
      }
  }


}
