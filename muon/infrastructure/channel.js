var csp = require("js-csp");
require('sexylog');
var messages = require('../domain/messages.js');

var SHUTDOWN_DELAY = 1000;

/**
 * Muon-node bi-directional channel
 *
 * Bi directional channel with two endpoints (connections) named right & left
 * Each endpoint has an inbound and bound unidrectional channel to send arbitrary messages along to each other
 *
 * var bichannel = require('./bi-channel.js');
 * var channel = bichannel.create("cleint-api");
 * client1(channel.left());
 * client2(channel.right());
 *
 */

module.exports.create = function(name, validiator, delay) {
    if (!name) throw new Error("This is screwed")
    if (delay) SHUTDOWN_DELAY = delay;
    return new Channel(name, validiator);
}

module.exports.wrap = function(connection) {

  var wrapperChannel = bichannel.create(connection.name() + "-wrapper");

  wrapperChannel.rightConnection().listen(function(msg) {
         connection.send(msg);
  });

  connection.listen(function(msg) {
     wrapperChannel.rightConnection().send(msg);
  });

  return wrapperChannel;
}


function LeftConnection(name, inbound, outbound, validator, channel) {
    name = name + '-left-connection';
    var self = this;
    var handler;
    var listener;
    var errCallback;
    var connectionObject = {
        onError: function(callback) {
            errCallback = callback;
        },
        throwErr: function(err) {
            if (! err instanceof Error) {
               err = new Error(err);
            }
            logger.warn('error message being thrown on channel "' + name + '". sending downstream.');
            csp.putAsync(outbound, err);
        },
        send: function(msg) {
            if (outbound.closed) return false;
            if (! msg) throw new Error('cannot send empty message on channel ' + name);
            //logger.trace("[***** CSP-CHANNEL *****] " + name + ".listen() msg=" + typeof msg);
            var id = msg.id || "unknown";
            //logger.trace("[***** CSP-CHANNEL *****] " + name + ".send() msg.id='" + id + "'");
            //logger.trace("[***** CSP-CHANNEL *****] " + name + ".send() msg='" + JSON.stringify(msg));
            // validate message
            try {
               if (validator && ! (msg instanceof Error)) {
                    //logger.trace("[***** CSP-CHANNEL *****] " + name + ".send() validating msg");
                    validator(msg);
               }
            } catch(err) {
                logger.warn('invalid message received on channel "' + name + '" sending to error listener');
                if (errCallback) {
                    errCallback(err); // invalid send it back
                }
                return false;

            }
            var result = csp.putAsync(outbound, msg);
        },
        listen: function(callback) {
            if (handler) throw new Error(name + ': cannot set LHS listener as handler already set');
            if (inbound.closed) throw new Error('csp channel closed');
            listener = callback;
            //logger.trace(name + " ChannelConnection.send() callback: " + callback);
            return csp.go(function*() {
                while(! inbound.closed) {
                    var msg = yield csp.take(inbound);
                    if (inbound.closed || ! msg) return {id: 'n/a', protocol: 'muon', step: 'csp_channel_closed'};
                    if (! msg) throw new Error('empty message is invalid');
                    //logger.trace("[***** CSP-CHANNEL *****] " + name + ".listen() msg=" + JSON.stringify(msg));
                    var id = msg.id || "unknown";
                    // deal with errors
                    if (msg instanceof Error && errCallback) {
                        logger.warn('error message received on channel ' + name);
                        if (errCallback) errCallback(msg);
                        return;
                    }
                    if (callback) {
                        callback(msg);
                    } else {
                        return msg;
                    }
                }
            });
        },
        handler: function(h) {
            if (listener) throw new Error('cannot set handler as listener already set');
            if (handler) throw new Error('left handler already set on channel "' + name + '"');
            handler = h;
            handler.setDownstreamConnection(this);


            return csp.go(function*() {
                while(! inbound.closed) {
                    var msg = yield csp.take(inbound);
                     logger.trace("[***** CSP-CHANNEL *****] " + name + ".handler() msg recevied: " + JSON.stringify(msg));
                     if (! msg) return;

                    var accept = function(result) {
                        if (! result) return;
                        handler.otherConnection(name).send(result);
                    };

                    var reject = function(result) {
                        if (! result) return;
                        handler.thisConnection(name).send(result);
                    };

                    handler.sendUpstream(msg, accept, reject);
                }
            });
        },
        name: function() {
            return name;
        },
        close: function() {
            channel.close();
        }
    };
    //logger.trace('[***** CSP-CHANNEL *****] returning left connection '+ name);
    return connectionObject;
}






function RightConnection(name, inbound, outbound, validator, channel) {
    name = name + '-right-connection';
    var self = this;
    var handler;
    var listener;
    var errCallback;
    //logger.trace('left validator: ', validator);
    var connectionObject = {
        onError: function(callback) {
            errCallback = callback;
        },
        throwErr: function(err) {
            if (! err instanceof Error) {
               err = new Error(err);
            }
            logger.warn('error message being thrown on channel "' + name + '". sending downstream.');
            csp.putAsync(outbound, err);
        },
        send: function(msg) {
            if (outbound.closed) return false;
            if (! msg) throw new Error('cannot send empty message on channel ' + name);
            //logger.trace("[***** CSP-CHANNEL *****] " + name + ".send() msg type=" + typeof msg);
            var id = msg.id || "unknown";
            logger.trace("[***** CSP-CHANNEL *****] " + name + ".send() msg=" + JSON.stringify(msg));
           // logger.debug("[***** CHANNEL *****] " + name + " ChannelConnection.send() listener: " + listener);
            try {
               if (validator && ! (msg instanceof Error)) {
                    //logger.trace("[***** CSP-CHANNEL *****] " + name + ".send() validating msg");
                    validator(msg);
               }
            } catch(err) {
               logger.warn('invalid message received on channel "' + name + '" sending to error listener');
                if (errCallback) {
                    errCallback(err); // invalid send it back
                    return;
                }
                return false;
            }
            var result = csp.putAsync(outbound, msg);
            //if (! result) throw new Error('csp channel closed');
        },
        listen: function(callback) {
            if (handler) throw new Error(name + ': cannot set RHS listener as handler already set');
            if (inbound.closed) throw new Error('csp channel closed');
            listener = callback;
            //logger.trace(name + " ChannelConnection.send() callback: " + callback);
            return csp.go(function*() {
              while(! inbound.closed) {
                    var msg = yield csp.take(inbound);
                    if (inbound.closed || ! msg) return {id: 'n/a', protocol: 'muon', step: 'csp_channel_closed'};
                    if (! msg) throw new Error('empty message is invalid');
                    var id = msg.id || "unknown";
                    //logger.trace("[***** CSP-CHANNEL *****] " + name + ".listen() msg=" + JSON.stringify(msg));
                    // deal with errors
                    if (msg instanceof Error && errCallback) {
                        logger.warn('error message received on channel ' + name);
                        errCallback(msg);
                        return;
                    }
                    //logger.trace("[***** CSP-CHANNEL *****] " + name + ".listen() msg.id=" + id);
                    if (callback) {
                        callback(msg);
                    } else {
                        return msg;
                    }
                }
            });
        },
        handler: function(h) {
            if (listener) throw new Error('cannot set handler as listener already set');
            if (handler) throw new Error('right handler already set on channel "' + name + '"');
            handler = h;
            handler.setUpstreamConnection(this);

            return csp.go(function*() {
                while(! inbound.closed) {
                    var msg = yield csp.take(inbound);
                    logger.trace("[***** CSP-CHANNEL *****] " + name + ".handler() msg recevied: " + JSON.stringify(msg));
                    var accept = function(result) {
                        if (! result) return;
                        handler.otherConnection(name).send(result);
                    };

                    var reject = function(result) {
                        if (! result) return;
                        handler.thisConnection(name).send(result);
                    };
                    handler.sendDownstream(msg, accept, reject);
                }
            });
        },
        name: function() {
            return name;
        },
        close: function() {
          channel.close();
        }
    }
    //logger.trace('[***** CSP-CHANNEL *****] returning right connection ' + name);
    return connectionObject;
}


function Channel(name, validator) {
    var name = name + '-csp-channel' || "unnamed-csp-channel";
    var _this = this;
    var inbound = csp.chan();
    var outbound = csp.chan();

    logger.trace('[***** CSP-CHANNEL *****] Created csp bi-channel with name="' + name + '"');
    var channelApi = {
        name: function() {
          return name;
        },
        leftEndpoint: function(object, ioFunctionName) {
            leftConnection.listen(function(args) {
                    var ioFunction = object[ioFunctionName];
                    var callback = function(reply) {
                        leftConnection.send(reply);
                    }
                    ioFunction(args, callback);
            });
        },
        rightEndpoint: function(object, ioFunctionName) {
               rightConnection.listen(function(args) {
                       var ioFunction = object[ioFunctionName];
                       var callback = function(reply) {
                           rightConnection.send(reply);
                       }
                       ioFunction(args, callback);
               });
           },
        leftHandler: function(handler) {
            leftConnection.handler(handler);
        },
        rightHandler: function(handler) {
            rightConnection.handler(handler);
        },
        leftConnection: function() {
            return leftConnection;
        },
        leftSend: function(msg) {
           leftConnection.send(msg);
         },
        rightConnection: function() {
            return rightConnection;
        },
        rightSend: function(msg) {
            rightConnection.send(msg);
        },
        close: function() {
          logger.warn('[***** CSP-CHANNEL *****] Sending channel_op=closed message downstream ' + name);
          var shutdownMsg = messages.shutdownMessage();
          csp.putAsync(outbound, shutdownMsg);
          setTimeout(function(){
            logger.warn('[***** CSP-CHANNEL *****] SHUTDOWN CHANNEL ' + name);

            inbound.close();
            outbound.close();
          }, SHUTDOWN_DELAY);
        }
    }

    var leftConnection = new LeftConnection(name, inbound, outbound, validator, channelApi);
    var rightConnection = new RightConnection(name, outbound, inbound, validator, channelApi);
    
    return channelApi;

}
