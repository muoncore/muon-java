/**
 * THe mininal Muon javascript protocol runtime, implemented for Nashorn
 */

var apichannel = null
var theproto;
var state = {}
function setState(name, val) {
  state[name] = val;
}

function setTimeout(func, millis) {
  var run = Java.type("java.lang.Runnable")
  var myRun = Java.extend(run, {
    run: func
  })

  return scheduler.executeIn(millis, Java.type("java.util.concurrent.TimeUnit").MILLISECONDS, new myRun())
}

function clearTimeout (exec) {
  exec.cancel()
}

function startProto() {
  theproto = module.exports(api)
}

function invokeRight(msg) {
  theproto.fromTransport(msg)
}

function invokeLeft(msg) {
  theproto.fromApi(msg)
}

var api = {
  state: function(name) {
    var localstate = state[name]
    if (!localstate) {
    }
    return state[name];
  },
  sendApi: function (msg) {
    if (apichannel) {
      apichannel.send(msg)
    }
  },

  serviceName: function () {
    return muon.configuration.getServiceName()
  },

  configString: function (name) {
    return muon.configuration.getStringConfig(name)
  },

  sendTransport: function (msg) {
    var outbound = muonMessageCreator.apply(msg)
    transportchannel.send(outbound)
  },

  shutdown: function () {
    apichannel.shutdown()
    transportchannel.shutdown()
  },

  type: function (typename, payload) {
    return typeconverter.apply(typename, payload)
  },

  decode: function (typename, message) {
    return decoder.apply(typename, message)
  },

  encodeFor: function (message, service) {
    return encode.apply(message, service)
  }
}

var module = {}
