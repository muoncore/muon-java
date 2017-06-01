package io.muoncore.protocol;

import io.muoncore.Muon;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.exception.MuonException;
import io.muoncore.exception.MuonProtocolException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.client.TransportClient;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.extern.slf4j.Slf4j;

import javax.script.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public abstract class JSProtocol {

  private final ScriptEngine engine;
  private final String protocol;
  private HashMap<String, Function> converters = new HashMap<>();
  private HashMap<Class, Function> decodedecorator = new HashMap<>();
  private HashMap<String, Type> codeclookup = new HashMap<>();
  private Optional<ChannelConnection> api = Optional.empty();
  private Optional<ChannelConnection> transport = Optional.empty();

  public JSProtocol(
    Muon muon,
    String protocolName) throws MuonException {
    protocol = protocolName;
    Scheduler scheduler = new Scheduler();

    NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
    engine = factory.getScriptEngine("-scripting");
    engine.put("currentProtocol", protocolName);
    engine.put("log", log);
    engine.put("scheduler", scheduler);
    engine.put("muon", muon);
    engine.put("typeconverter", (BiFunction<String, Map, Object>) (typeName, map) -> {
      Function convert = converters.getOrDefault(typeName, (val) -> val);
      return convert.apply(map);
    });
    engine.put("decoder", (BiFunction<String, MuonInboundMessage, Object>) (typeName, msg) -> {
      Type type = codeclookup.getOrDefault(typeName, Map.class);
      Object obj = muon.getCodecs().decode(msg.getPayload(), msg.getContentType(), type);
      return decodedecorator.getOrDefault(type, o -> o).apply(obj);
    });
    engine.put("encode", (BiFunction<Object, String, Codecs.EncodingResult>) (msg, service) -> {
      return muon.getCodecs().encode(msg, muon.getDiscovery().getCodecsForService(service));
    });

    engine.put("muonMessageCreator", (Function<Map, MuonOutboundMessage>) map -> {

      if (map == null) {
        return null;
      }
      String target = (String) map.get("target");
      String step = (String) map.get("step");
      Object payload = map.get("payload");
      byte[] encodedPayload = new byte[0];
      String contentType = (String) map.get("contentType");

      if (payload instanceof byte[]) {
        encodedPayload = (byte[]) payload;
      } else if (contentType == null || !(payload instanceof byte[])) {
          Codecs.EncodingResult encode = muon.getCodecs().encode(payload, muon.getDiscovery().getCodecsForService(target));
          log.debug("Encoding payload {} as the contentType is not set, or it is not a byte array (ie, already encoded!", payload);
          encodedPayload = encode.getPayload();
          contentType = encode.getContentType();
      }

      return MuonMessageBuilder.fromService(muon.getConfiguration().getServiceName())
        .step(step)
        .protocol(protocolName)
        .operation(MuonMessage.ChannelOperation.normal)
        .toService(target)
        .contentType(contentType)
        .payload(encodedPayload).build();
    });

    try {
      engine.eval(new InputStreamReader(getClass().getResourceAsStream("/protocol-api.js")));
    } catch (ScriptException e) {
      throw new MuonProtocolException(protocol, e.getMessage(), e);
    }
  }

  public void setState(String name, Object state) {
    try {
      Invocable invocable = (Invocable) engine;
      invocable.invokeFunction("setState", name, state);
    } catch (ScriptException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public void setTransportChannel(ChannelConnection transport) {
    this.transport = Optional.of(transport);
    engine.put("transportchannel", transport);
    transport.receive(this::executeMessageRight);
  }

  public void setApiChannel(ChannelConnection leftChannelConnection) {
    this.api = Optional.of(leftChannelConnection);
    engine.put("apichannel", leftChannelConnection);
    leftChannelConnection.receive(this::executeMessageLeft);
  }

  public void addTypeForCoercion(String name, Function<Map, Object> func) {
    converters.put(name, func);
  }

  public void addTypeForDecoding(String name, Type type) {
    codeclookup.put(name, type);
  }

  public <T> void addPostDecodingDecorator(Class<T> type, Function<T, T> types) {
    decodedecorator.put(type, types);
  }

  public void start(String js) {
    try {
      engine.eval(js);
      init();
    } catch (ScriptException e) {
      throw new MuonProtocolException(protocol, e.getMessage(), e);
    }
  }

  public void start(InputStream is) {
    try {
      engine.eval(new InputStreamReader(is));
      init();
    } catch (ScriptException e) {
      throw new MuonProtocolException(protocol, e.getMessage(), e);
    }
  }

  private void init() {
    try {
      Invocable invocable = (Invocable) engine;
      invocable.invokeFunction("startProto");
    } catch (ScriptException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public void executeMessageLeft(Object msg) {
    if (msg == null) {
      log.debug("JSProtocol has received null from the API and will shutdown");
      transport.ifPresent(channelConnection -> channelConnection.send(null));
      return;
    }
    try {
      Invocable invocable = (Invocable) engine;
      invocable.invokeFunction("invokeLeft", msg);
    } catch (ScriptException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }
  public void executeMessageRight(Object msg) {
    if (msg == null) {
      log.debug("JSProtocol has received null from the transport and will shutdown");
      api.ifPresent(channelConnection -> channelConnection.send(null));
      return;
    }
    try {
      Invocable invocable = (Invocable) engine;
      invocable.invokeFunction("invokeRight", msg);
    } catch (ScriptException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }
}

//TODO, this should all operate on a single spinning thread to avoid threading issues....
