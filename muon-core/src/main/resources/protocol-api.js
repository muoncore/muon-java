/**
 * THe mininal Muon javascript protocol runtime, implemented for Nashorn
 * @param msg
 */

function sendApi(msg) {
  apichannel.send(msg)
}

function serviceName() {
  return muon.configuration.getServiceName()
}

function configString(name) {
  return muon.configuration.getStringConfig(name)
}

function sendTransport(msg) {
  var outbound = muonMessageCreator.apply(msg)
  transportchannel.send(outbound)
}

function shutdown() {
  sendTransport(null)
  sendApi(null)
}

function setTimeout(func, millis) {
  var run = Java.type("java.lang.Runnable")
  var myRun = Java.extend(run, {
    run: func
  })

  return scheduler.executeIn(millis, Java.type("java.util.concurrent.TimeUnit").MILLISECONDS, new myRun())
}

function clearTimeout(exec) {
  exec.cancel()
}

function type(typename, payload) {
  //TODO, convert this into the concrete type
  return typeconverter.apply(typename, payload)
}



function decode(typename, message) {
  print ("Trying to decode " + message)
  return decoder.apply(typename, message)
}

function encodeFor(message, service) {
  return encode.apply(message, service)
}
