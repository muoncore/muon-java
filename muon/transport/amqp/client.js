var RSVP = require('rsvp');
require('sexylog');
var helper = require('./transport-helper.js');
var bichannel = require('../../../muon/infrastructure/channel.js');
var uuid = require('node-uuid');
var messages = require('../../domain/messages.js');

var errCallback;

exports.connect = function (serviceName, protocol, api, discovery) {
    var clientChannel = bichannel.create(serviceName + "-amqp-transport-client");

    //logger.trace('[*** TRANSPORT:CLIENT:BOOTSTRAP ***] connected to amqp-api');
    var handshakeId = uuid.v4();
    var serviceQueueName = helper.serviceNegotiationQueueName(serviceName);
    var serverListenQueueName = serviceName + ".listen." + handshakeId;
    var replyQueueName = serviceName + ".reply." + handshakeId;
    var handshakeHeaders = helper.handshakeRequestHeaders(protocol, serverListenQueueName, replyQueueName);
    logger.trace('[*** TRANSPORT:CLIENT:BOOTSTRAP ***] preparing to handshake...');
    RSVP.resolve()
        .then(findService(serviceName, discovery))
        .then(readyInboundSocket(replyQueueName, api, clientChannel.rightConnection(), serverListenQueueName, replyQueueName, serviceQueueName, handshakeHeaders))
        .then(readyOutboundSocket(serverListenQueueName, protocol, api, clientChannel.rightConnection(), serverListenQueueName, replyQueueName))
        .catch(function (err) {
            logger.warn('client error: ' + err.message);
            if (err.message.indexOf('unable to find muon service') > -1) {
                try {
                    // return muon socket messages unable to find server
                    var failureMsg = messages.failure(protocol, 'noserver', 'transport cannot find service "' + serviceName + '"');
                    clientChannel.rightConnection().send(failureMsg);
                } catch (err2) {
                    logger.error(err2.stack);
                    throw new Error(err2);
                }
            } else {
                var negotiationErr = new Error(err);
                logger.error(err.stack);
                errCallback(err);
            }
        });

    logger.trace('[*** TRANSPORT:CLIENT:BOOTSTRAP ***] returning channel handle');
    return clientChannel.leftConnection();
};

exports.onError = function (callback) {
    errCallback = callback;
}


var findService = function (serviceName, discovery) {
    logger.debug("[*** TRANSPORT:CLIENT:DISCOVERY ***] finding service '" + serviceName + "'");
    return function (prevResult) {
        var promise = new RSVP.Promise(function (resolve, reject) {
            var attempts = 0;
            var maxattempts = 5;

            var serviceFinder = setInterval(function () {
                find();
            }, 1000);

            var find = function () {
                attempts++;

                var serviceFound = false;
                logger.debug("[*** TRANSPORT:CLIENT:DISCOVERY ***] searching for muon service '" + serviceName + "' attempt " + attempts);
                discovery.discoverServices(function (services) {
                    var service = services.find(serviceName);
                    logger.trace("[*** TRANSPORT:CLIENT:DISCOVERY ***] discovered services: '" + JSON.stringify(services) + "'");
                    if (service) {
                        logger.debug("[*** TRANSPORT:CLIENT:DISCOVERY ***] found service: '" + JSON.stringify(service) + "'");
                        serviceFound = true;
                        resolve(service);
                    } else if (attempts > maxattempts) {
                        logger.warn("[*** TRANSPORT:CLIENT:DISCOVERY ***] unable to find service '" + serviceName + "' after " + attempts + " attempts. aborting. reject()");
                        reject(new Error('unable to find muon service ' + serviceName));
                    } else {
                        //logger.trace("[*** TRANSPORT:CLIENT:DISCOVERY ***] finding service '" + serviceName + "'");
                    }

                    if (attempts > maxattempts || serviceFound) {
                        clearInterval(serviceFinder);
                    }
                });
            }

            find();


        });
        return promise;
    }
}

var readyInboundSocket = function (recvQueueName, amqpApi, clientChannel, serverListenQueueName, replyQueueName, serviceQueueName, handshakeHeaders) {
    return function (prevResult) {
        var promise = new RSVP.Promise(function (resolve, reject) {
            logger.trace("[*** TRANSPORT:CLIENT:INBOUND ***] waiting for muon replies on queue '" + recvQueueName + "'");

            amqpApi.inbound(recvQueueName, function () {
                var channel = amqpApi.outbound(serviceQueueName).send({data: {}, headers: handshakeHeaders});
                logger.debug("[*** TRANSPORT:CLIENT:HANDSHAKE ***] handshake message sent on queue '" + serviceQueueName + "'" + JSON.stringify(handshakeHeaders));
            }).listen(function (message) {
                if (helper.isHandshakeAccept(message)) {
                    // we're got a handshake confirmation and are now connected to the remote service
                    logger.trace("[*** TRANSPORT:CLIENT:HANDSHAKE ***]  client received negotiation response message %s", JSON.stringify(message));
                    logger.debug("[*** TRANSPORT:CLIENT:HANDSHAKE ***] client/server handshake protocol completed successfully " + JSON.stringify(message));
                    resolve();
                } else {
                    logger.debug("[*** TRANSPORT:CLIENT:INBOUND ***]  client received muon event %s", JSON.stringify(message));

                    if (message.data && message.data.channel_op == 'closed') {
                        logger.warn("[*** TRANSPORT:SERVER:INBOUND ***]  handling inbound received message channel_op=closed. deleteing queues for socket ");
                        amqpApi.delete(serverListenQueueName);
                        amqpApi.delete(replyQueueName);
                    }
                    var muonMessage = message.data;
                    clientChannel.send(muonMessage);


                }
            });
        });
        return promise;
    }
}


var readyOutboundSocket = function (serviceQueueName, protocol, amqpApi, clientChannel, serverListenQueueName, replyQueueName) {
    var muonSocketOpen = true;
    return function (prevResult) {
        var promise = new RSVP.Promise(function (resolve, reject) {
            clientChannel.listen(function (message) {

                if (muonSocketOpen) {
                    if (message.channel_op == 'closed') {
                        // close muon socklet
                        amqpApi.outbound(serviceQueueName).send({
                            headers: {
                                protocol: protocol,
                                content_type: "application/json"
                            }, data: message
                        });
                        setTimeout(function () {
                            logger.debug("[*** TRANSPORT:SERVER:OUTBOUND ***]  handling outbound received message channel_op=closed. deleteing queues for socket ");
                            amqpApi.delete(serverListenQueueName);
                            amqpApi.delete(replyQueueName);
                            muonSocketOpen = false;
                        }, 100)
                        return;
                    }

                    messages.validate(message);
                    logger.debug("[*** TRANSPORT:CLIENT:OUTBOUND ***] send on queue " + serviceQueueName + "  message=", JSON.stringify(message));
                    amqpApi.outbound(serviceQueueName).send({
                        headers: {
                            protocol: protocol,
                            content_type: "application/json"
                        }, data: message
                    });
                } else {
                    logger.trace('cannot send message as muon socket is not open');
                    logger.debug("Muon socket has been closed " + protocol + " " + serviceQueueName);
                }
            });
            //logger.trace('[*** TRANSPORT:CLIENT:HANDSHAKE ***] readyOutboundSocket success');
            resolve();
        });
        logger.trace("[*** TRANSPORT:CLIENT:OUTBOUND ***] outbound socket ready on amqp queue  '%s'", serviceQueueName);
        return promise;
    }
}
