
/**
 * Simple api for the reactive stream proto
 * subscriber. This gives a very simplified view on top of the streaming protocol
 *
 *
 * {
 *   onSubscribe: function( subscription { 
 *      request: function(n)
 *      cancel: function()
 *   })
 *   onNext: function(data)
 *   onError: function(error)
 *   onComplete: function()
 * }
 *
 */

module.exports.subscriber = function(dataCallback, errorCallback, completeCallback) {

    var requestSize = 10
    var dataSeen = 0
    var sub

    return {
        onSubscribe: function(subscription) {
            sub = subscription
            sub.request(requestSize)
        },
        onNext: function(data) {
            dataSeen++
            if (dataSeen == requestSize) {
                sub.request(requestSize)
                dataSeen = 0
            }
            dataCallback(data)
        },
        onError: function(error) {
            errorCallback(error)
        },
        onComplete: function() {
            completeCallback()
        },
        // passed to the end user to allow them to control the subscription without needing to be concerned
        // with the full api/ protocol details.
        control: {
            cancel: function() {
                if (sub) {
                    sub.cancel()
                } else {
                    logger.warn("Called subscriber.cancel(), no subscriber set yet, ignoring")
                }
            }
        }
    }
}