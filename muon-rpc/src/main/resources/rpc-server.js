

module.exports = function(api) {

  function response(response) {
    return api.type("Response", response)
  }

  return {
    fromApi: function (request) {

      timeoutcontrol = setTimeout(function () {
        print("SENDING A TIMEOUT RESPONSE!")
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

      api.clearTimeout(timeoutcontrol)


      switch (msg.step) {
        case "request.response":
          api.sendApi(api.decode("Response", msg))
          break;
        case "request.failed":
          api.sendApi(api.decode("Response", msg))
          break
        case "ServiceNotFound":
          print("Got a servicenotfound")
          api.sendApi(api.response({
            status: 404
          }))
          break
        case "ChannelFailure":
          api.sendApi(api.response({
            status: 408
          }))
          break
        default:
          api.sendApi(api.response({
            status: 408
          }))
      }
      api.shutdown()
    }
  };
}


