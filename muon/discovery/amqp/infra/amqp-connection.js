var AMQP = require('amqp');

var AmqpConnection = function (url) {
    this.url = url;
    this.implOpts = {
        defaultExchangeName: '',
        reconnect: true,
        reconnectBackoffStrategy: 'linear',
        reconnectBackoffTime: 500 // ms
    };
};

AmqpConnection.prototype.connect = function (callback) {
    var connection = AMQP.createConnection({url: this.url}, this.implOpts);
    var url = this.url;
    connection.on('error', function (msg, something) {
        logger.warn("Getting an error in the AMQP Connection with url: '" + url + "'", msg);
        var stack = new Error().stack;
        logger.warn(stack);
    });
    connection.on("ready", function() {
        logger.debug("AMQP Connection becomes ready");
        callback();
    });
    connection.on("close", function() {
        logger.debug("AMQP Connection has CLOSED");
    });
    connection.on("blocked", function(data) {
        logger.error("AMQP Connection is BLOCKED");
    });
    connection.on("unblocked", function(data) {
        logger.error("AMQP Connection is UNBLOCKED");
    });

    this.connection = connection;
};

AmqpConnection.prototype.close = function () {
    this.connection.disconnect();
};

AmqpConnection.prototype.queue = function (name, params, callback) {
    this.connection.queue(name, params, callback);
};

AmqpConnection.prototype.exchange = function (name, callback, params) {
    if (typeof params === 'undefined') {
        params = {
            durable: false,
            type: "direct",
            autoDelete: true,
            confirm: true
        };
    }
    if (typeof name === 'undefined' || name.length == 0) name = '';

    logger.debug('Setting up new exchange at ' + name);
    var exch = this.connection.exchange(name, params);
    if (typeof callback === 'function') {
        callback(exch);
    }
    return exch;
};

AmqpConnection.prototype.send = function (qObj, event, callback) {
    var queue, route;
    var _this = this;

    if (typeof qObj === 'object') {

        queue = qObj.queue;
        route = queue;

        if ('route' in qObj) route = qObj.route;

    } else {
        queue = route = qObj;
    }

    if (typeof event === 'object') {
        if (!'payload' in event) {
            // we should throw an error here? For now we fail silently
            event.payload = '';
        }

    } else {
        event = {
            payload: event
        };
    }

    var options = {
        //replyTo: route + '.reply',
        contentType: "text/plain"
    };

    if ('muon_headers' in event) options.muon_headers = event.muon_headers;

    this.connection.on('ready', function () {
        logger.debug('Connection is ready to send on ' + queue);

        var con = _this.connection;

        con.publish(route, event.payload, options, function (test) {

            if (typeof callback === 'function') {
                callback();
            }

        });
    });
};

AmqpConnection.prototype.listen = function (qObj, callback) {
    var queue, route, replyTo;
    var _this = this;

    if (typeof qObj === 'object') {

        queue = qObj.queue;
        route = queue;

        if ('route' in qObj) route = qObj.route;

    } else {
        queue = route = qObj;
    }

    var waitInterval = setInterval(function () {
        if (typeof this.resourceExchange == 'object') {
            clearInterval(waitInterval);

            logger.debug("Creating listening queue " + queue);

            var resqueue = _this.connection.queue(queue, {
                durable: false,
                exclusive: false,
                ack: true,
                autoDelete: true
            }, function (q) {

                q.bind(queue, function () {
                    logger.debug("Bound queue " + queue + " to route " + route);
                    q.subscribe(function (message, muon_headers, deliveryInfo, messageObject) {
                        logger.trace("Got a message ", messageObject);
                        callback({
                            payload: message.data
                        }, message.data, function (response) {
                            if ('replyTo' in messageObject) {
                                var replyTo = messageObject.replyTo;
                                _this.connection.publish(replyTo, JSON.stringify(response), {
                                    "contentType": "text/plain"
                                });
                            } else {
                                logger.warn("Received resource request with no reply-to header. This is incorrect and has been discarded", messageObject);
                            }
                        });
                    });
                });
            });
        }
    });
};

module.exports = AmqpConnection;