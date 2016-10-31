var uuid = require('node-uuid');

var AmqpBroadcast = function (connection) {

    this.connection = connection;
    this.broadcastExchange = connection.exchange("muon-broadcast", function (something) {

    }, {
        durable: false,
        "type": "topic",
        autoDelete: false,
        confirm: true
    });
};

AmqpBroadcast.prototype.emit = function (event) {
    var _this = this;
    var waitInterval = setInterval(function () {

        if (typeof _this.broadcastExchange === 'object') {

            clearInterval(waitInterval);

            var headers = {};
            if (event.headers instanceof Object) {
                headers = event.headers;
            }

            headers["Content-Type"] = "application/json";

            var options = {
                contentType: "application/json",
                headers: headers
            };

            _this.broadcastExchange.publish(
                event.name,
                event.payload, options, function (resp) {
                    //wat do
                });
        } else {
            logger.warn("The broadcast exchange is not initialised");
        }
    }, 100);
};

AmqpBroadcast.prototype.listenOnBroadcast = function (event, callback) {
    var _this = this;
    var waitInterval = setInterval(function () {
        if (typeof _this.broadcastExchange == 'object') {
            clearInterval(waitInterval);
            var queue = "muon-node-broadcastlisten-" + uuid.v1();

            logger.debug("Creating broadcast listen queue " + queue);

            _this.connection.queue(queue, {
                durable: false,
                exclusive: true,
                ack: true,
                autoDelete: true
            }, function (q) {
                q.bind("muon-broadcast", event, function () {
                    logger.debug("Bound event queue " + queue);
                    q.subscribe(function (message, headers, deliveryInfo, messageObject) {

                        try {
                            var payload = message;

                            if (messageObject.contentType == null && typeof(message.data) != null) {
                                try {
                                    logger.trace("Parsing broadcast message as no effective content type");
                                    payload = JSON.parse(message.data.toString());
                                    logger.trace("Broadcast payload: " + payload);
                                } catch (error) {
                                    logger.error("Failed to understand broadcast message");
                                }
                            }

                            //todo, headers ...
                            try {
                                callback({
                                    headers: headers,
                                    payload: message
                                }, payload);
                            } catch (error) {
                                logger.error("Broadcast handler failed to process correctly for :" + event);
                                var stack = new Error().stack;
                                logger.error(stack);
                            }
                        } catch (error) {
                            logger.error("Bombed during broadcast handling ... ");
                            logger.error(error);
                        }
                    });
                });
            });
        }
    }, 100);
};

module.exports = AmqpBroadcast;