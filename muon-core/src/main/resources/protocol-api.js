/**
 * THe mininal Muon javascript protocol runtime, implemented for Nashorn
 */

var theproto;

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
  sendApi: function (msg) {
    apichannel.send(msg)
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
    api.sendTransport(null)
    api.sendApi(null)
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
