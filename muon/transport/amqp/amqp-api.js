
var RSVP = require('rsvp');
var bichannel = require('../../../muon/infrastructure/channel.js');
require('sexylog');
var RSVP = require('rsvp');
var messages = require('../../domain/messages.js');
var nodeUrl = require('url');
var helper = require('./transport-helper.js');


var queueSettings = {
     durable: false,
     type: "direct",
     autoDelete: true,
     confirm: true
};




var amqpConnectionOk = false;
var amqpChannelOk = false;
var connectionUrl;

var amqpConnection;

function validateUrl(url) {
        try {
            var parsedUrl = nodeUrl.parse(url);
        } catch (err) {
            return new Error('invalid ampq url: ' + url);
        }

       if (parsedUrl.protocol != 'amqp:') return new Error('invalid ampq url: ' + url);
       if (! parsedUrl.slashes) return new Error('invalid ampq url: ' + url);
       if (! parsedUrl.hostname) return new Error('invalid ampq url: ' + url);
       return;
}



exports.connect = function(url) {
        connectionUrl = url;
       var channelValidator = function(msg) {
            helper.validateMessage(msg);
       }

       var promise = new RSVP.Promise(function(resolve, reject) {
           function callback(err, amqpConnection, amqpChannel) {
                if (err) {
                    logger.error('error connecting to amqp' + err);
                    reject(err);
                } else {
                    var api = {
                          outbound: function(queueName) {
                            var clientChannel = bichannel.create("amqp-api-outbound-" + queueName, channelValidator);
                            clientChannel.rightConnection().listen(function(msg) {
                                 logger.trace('[*** TRANSPORT:AMQP-API:OUTBOUND ***] received outbound message: ' + JSON.stringify(msg));
                                 try {
                                    publish(amqpChannel, queueName, msg);
                                 } catch (err) {
                                   logger.warn('problem publishing message to amqp queue ""' + queueName + '""');
                                   logger.warn('problem publishing message: ' + JSON.stringify(msg));
                                   logger.warn(err.stack);
                                 }

                            });
                            return clientChannel.leftConnection();
                          },
                          inbound: function(queueName, onReady) {
                            var clientChannel = bichannel.create("amqp-api-inbound-" + queueName, channelValidator);
                            consume(amqpChannel, queueName, function(msg) {
                                    //logger.trace('[*** TRANSPORT:AMQP-API:INBOUND ***] send message up stream: ' + JSON.stringify(msg));
                                    clientChannel.rightConnection().send(msg);
                            }, onReady);
                            return clientChannel.leftConnection();
                          },
                          shutdown: function() {
                              amqpConnection.close();
                          },
                          delete: function(queueName) {
                            logger.debug("[*** TRANSPORT:AMQP-API:OUTBOUND ***] deleting amqp queue '" + queueName + "'");
                            amqpChannel.deleteQueue(queueName);
                          },
                          url: function() {
                            return url;
                          }
                    }
                    resolve(api);
                }
           }
           var invalid = validateUrl(url);
           if (invalid) reject(invalid);
           amqpConnect(url, callback);
        });
        return promise;
}


function reconnect(rerunFunction) {
    amqpConnect(connectionUrl, function() {
      rerunFunction();
    });
}

function amqpConnect(url, callback) {
    // cache the connection pikey style...
    /*
    if (amqpConnection && amqpChannel) {
      callback(null, amqpConnection, amqpChannel);
      return;
    }
    */
    var amqp = require('amqplib/callback_api');
    logger.trace("[*** TRANSPORT:AMQP-API:BOOTSTRAP ***] connecting to amqp " + url);
    amqp.connect(url, function(err, amqpConn) {
        if (err) {
            logger.error("[*** TRANSPORT:AMQP-API:BOOTSTRAP ***] error connecting to amqp: " + err);
            logger.error(err.stack);
            callback(err);
        } else {
          amqpConnectionOk = true;
          amqpConnection = amqpConn;
          logger.debug("[*** TRANSPORT:AMQP-API:BOOTSTRAP ***] amqp connected.");
          handleConnectionEvents(amqpConn);
          amqpConnection.createChannel(onChannel);
          function onChannel(err, amqpChan) {
            if (err != null) {
                logger.error("[*** TRANSPORT:AMQP-API:BOOTSTRAP ***] error creating amqp channel: " + err);
                callback(err);
            } else {
                amqpChannelOk = true;
                logger.trace("[*** TRANSPORT:AMQP-API:BOOTSTRAP ***] amqp comms channel to " + url + " created successfully");
                handleChannelEvents(amqpChan);
                // amqpChannel = amqpChan;
                callback(null, amqpConn, amqpChan);
            }

          }
        }
    });
}

function handleConnectionEvents(amqpConnection) {
      amqpConnection.on('close', function(err) {
            logger.info('amqp connection closed ' + err);
            amqpConnectionOk = false;
      });

      amqpConnection.on('error', function(err) {
             logger.error('amqp connection error ' + err);
             logger.error(err.stack);
             amqpConnectionOk = false;
      });

      amqpConnection.on('blocked', function(err) {
            logger.warn('amqp connection blocked ' + err);
      });

      amqpConnection.on('unblocked', function(err) {
            logger.warn('amqp connection unblocked ' + err);
      });
}

function handleChannelEvents(amqpChannel) {
      amqpChannel.on('close', function() {
            logger.info('amqp channel event fired on close');
            amqpChannelOk = false;
      });

      amqpChannel.on('error', function(err) {
            logger.error('amqp channel event fired on error. error=' + err);
            logger.error(err.stack);
            amqpChannelOk = false;
      });

      amqpChannel.on('return', function() {
            logger.warn('amqp channel event fired on return');
      });

      amqpChannel.on('drain', function() {
            logger.warn('amqp channel event fired on drain');
      });
}

function publish(amqpChannel, queueName, message) {
    var data = message.data;
    var headers = message.headers;
    //logger.trace("[*** TRANSPORT:AMQP-API:OUTBOUND ***] publish on queue '" + queueName + "' data: ", data);
    //logger.trace("[*** TRANSPORT:AMQP-API:OUTBOUND ***] publish on queue '" + queueName + "' headers: ", headers);
    //if (! amqpConnectionOk || ! amqpChannelOk) return;
    amqpChannel.assertQueue(queueName, queueSettings);
    var buffer = helper.encode(data);
    amqpChannel.sendToQueue(queueName, new Buffer(buffer), {persistent: false, headers: headers});

}


function consume(amqpChannel, queueName, callback, onReadyCallback) {
  //if (! amqpConnectionOk || ! amqpChannelOk) return;
   amqpChannel.assertQueue(queueName, queueSettings);
   amqpChannel.consume(queueName, function(amqpMsg) {
       logger.trace("[*** TRANSPORT:AMQP-API:INBOUND ***] consumed AMQP message on queue " + queueName + " message: ", JSON.stringify(amqpMsg));
       if (amqpMsg == undefined || amqpMsg == null) {
           logger.warn("Received a null message from amqp queue")
           // amqpChannel.ack(amqpMsg);
            return;
       }
       var message = helper.fromWire(amqpMsg);
       amqpChannel.ack(amqpMsg);
       callback(message);
   }, {noAck: false}, onReadyCallback);

}
