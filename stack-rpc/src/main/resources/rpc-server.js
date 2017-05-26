

module.exports = function(api) {

  return {
    fromTransport: function (msg) {

      var request = api.decode("ServerRequest", msg)

      var handler = api.state("getHandler")(request);

      handler(function(response) {
        log.info("Response generated " + response)
        var ret = api.encodeFor(response.payload, msg.sourceServiceName)
        api.sendTransport({
          step: "request.response",
          payload: {
            status: response.status,
            content_type: ret.contentType,
            body: ret.payload
          }
        })
        api.shutdown()
      });
    }
  };
}
