
var timeoutcontrol

function fromApi(request) {

  timeoutcontrol = setTimeout(function() {
    sendApi(response({
      status: 404
    }))
    shutdown()
  }, 10000)

  var encodedBody = encodeFor(request, request.targetService)

  sendTransport({
    step:"request.made",
    target: request.targetService,
    payload: {
      body: encodedBody.payload,
      content_type: encodedBody.contentType,
      url: request.url
    }
  })
}

function fromTransport(msg) {

  clearTimeout(timeoutcontrol)

  switch(msg.step) {
    case "request.response":
      sendApi(decode("Response", msg))
      break;
    case "request.failed":
      sendApi(decode("Response", msg))
      break
    case "ServiceNotFound":
      sendApi(response({
        status: 404
      }))
      break
    case "ChannelFailure":
      sendApi(response({
        status: 408
      }))
      break
    default:
      sendApi(response({
        status: 408
      }))
  }
  shutdown()
}

function response(response) {
  return type("Response", response)
}
