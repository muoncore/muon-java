package io.muoncore.protocol.rpc.client;

import lombok.extern.slf4j.Slf4j;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class JSProtocol {

  private final ScriptEngine engine;

  public JSProtocol(InputStream is) throws FileNotFoundException, ScriptException {
    engine = new ScriptEngineManager().getEngineByName("nashorn");

    engine.eval(new InputStreamReader(getClass().getResourceAsStream("/protocol-api.js")));
    engine.eval(new InputStreamReader(is));
  }

  public void execute() throws ScriptException, NoSuchMethodException {
    Invocable invocable = (Invocable) engine;

    Object result = invocable.invokeFunction("fun1", "Peter Parker");
    log.info(String.valueOf(result));
    log.info(String.valueOf(result.getClass()));
  }
}
