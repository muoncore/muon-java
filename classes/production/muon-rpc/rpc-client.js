

module.exports = function(api) {

  var timeoutcontrol

  function response(response) {
    return api.type("Response", response)
  }

  return {
    fromApi: function (request) {

      timeoutcontrol = setTimeout(function () {
        api.sendApi(response({
          status: 408
        }))
        api.shutdown()
      }, 10000)

      var encodedBody = api.encodeFor(request, request.targetService)

      api.sendTransport({
        step: "request.made",
        target: request.targetService,
        payload: {
          body: encodedBody.payload,
          content_type: encodedBody.contentType,
          url: request.url
        }
      })
    },
    fromTransport: function (msg) {

      clearTimeout(timeoutcontrol)

      switch (msg.step) {
        case "request.response":
          api.sendApi(api.decode("Response", msg))
          break;
        case "request.failed":
          api.sendApi(api.decode("Response", msg))
          break
        case "ServiceNotFound":
          api.sendApi(response({
            status: 404
          }))
          break
        case "ChannelFailure":
          api.sendApi(response({
            status: 409
          }))
          break
        default:
          log.warn("Unexpected step type " + msg.step)
          log.warn("Msg is " + msg)
          api.sendApi(response({
            status: 410
          }))
      }
      api.shutdown()
    }
  };
}


