
var callbacks = {};

module.exports.create = function(n, handlers) {
    logger.warn('***** DEPRICATED!!! ********************************************************************************');
    logger.warn('handler.js DEPRICATED!!! Use handler-class.js instead (@see handler-class-test.js)');
    logger.warn('****************************************************************************************************');


    if (! n) n = 'default';
    var name = n + '-handler';

    if (handlers) callbacks = handlers;

    var outgoingFunction;
    var incomingFunction;

    var upstreamConnection;
    var downstreamConnection;



    return {
        outgoing: function(f) {
            outgoingFunction = f;
        },
        incoming: function(f) {
            incomingFunction = f;
        },
        register(callback, key) {
            callbacks[key] = callback;
        },
        sendDownstream: function(msg, accept, reject) {
            logger.debug('[*** CSP-CHANNEL:HANDLER ***] ' + name + ' sending message via handler downstream msg: ' +  JSON.stringify(msg));
            if (! msg) throw new Error('empty message is invalid');
            var route = createRoute(upstreamConnection, incomingFunction);
            outgoingFunction(msg, accept, reject, route);
        },
        sendUpstream: function(msg, accept, reject) {
            logger.debug('[*** CSP-CHANNEL:HANDLER ***] ' + name + ' sending message via handler upstream event.id=' + JSON.stringify(msg));
            if (! msg) throw new Error('empty message is invalid');
            var route = createRoute(downstreamConnection, outgoingFunction);
            incomingFunction(msg, accept, reject, route);
        },
        getUpstreamConnection: function() {
            return upstreamConnection;
        },
        getDownstreamConnection: function() {
            return downstreamConnection;
        },
        setUpstreamConnection: function(c) {
            upstreamConnection = c;
        },
        setDownstreamConnection: function(c) {
            downstreamConnection = c;
        },
        otherConnection(conn) {
            if (conn === upstreamConnection.name()) {
                logger.trace('[*** CSP-CHANNEL:HANDLER ***]  ' + name + ' other connection is downstream: ' + downstreamConnection.name());
                return downstreamConnection;
            } else {
                logger.trace('[*** CSP-CHANNEL:HANDLER ***] ' + name + ' other connection is upstream: ' + upstreamConnection.name());
                return upstreamConnection;
            }
        },
        thisConnection(conn) {
            if (upstreamConnection && conn === upstreamConnection.name()) {
                logger.trace('[*** CSP-CHANNEL:HANDLER ***]  ' + name + ' other connection is downstream: ' + downstreamConnection.name());
                return upstreamConnection;
            } else {
                var upstreamConnectionName = 'unset';
                if (upstreamConnection) upstreamConnectionName = upstreamConnection.name();
                logger.trace('[*** CSP-CHANNEL:HANDLER ***] ' + name + ' other connection is upstream: ' + upstreamConnectionName);
                return downstreamConnection;
            }
        }
    };

}


function createRoute(otherConnection, handlerFunction) {


    var route = function(message, key) {
        var callbackHandler = callbacks[key];

        if (! callbackHandler) throw new Error('unable to find callback handler for key: ' + key);

        var tempCallback = function(response) {
            logger.trace('[*** CSP-CHANNEL:HANDLER ***] callback handler returned response for key: ' + key);
            var accept = function(result) {
                otherConnection.send(result);
            };

            var reject = function(result) {
                callbackHandler({}, error);
            };
            logger.trace('[*** CSP-CHANNEL:HANDLER ***] calling onward function for key: ' + key);
            handlerFunction(response, accept, reject);
        }
        logger.trace('[*** CSP-CHANNEL:HANDLER ***]  executing routed callback handler for key: ' + key);
        callbackHandler(message, tempCallback);
    };

    return route;

}
