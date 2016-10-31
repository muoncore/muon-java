"use strict";

var nodeUrl = require("url");
//var RpcProtocol = require('../protocol/rpc-protocol.js');
var channel = require('../infrastructure/channel.js');
var uuid = require('node-uuid');
var RSVP = require('rsvp');
require('sexylog');
var Handler = require('../infrastructure/handler-class.js');
var messages = require('../domain/messages.js');


var handlerMappings = {};
var serviceName;
var protocolName = 'rpc';
exports.getApi = function (name, infrastructure) {
    serviceName = name;

    var api = {
        name: function () {
            return protocolName;
        },
        endpoints: function () {
            var endpoints = [];

            for (var key in handlerMappings) {
                // ; = handlerMappings[key].toString();
                endpoints.push(key);
            }
            return endpoints;
        },
        request: function (remoteServiceUrl, data, clientCallback) {

            var parsedUrl = nodeUrl.parse(remoteServiceUrl, true);

            var promise = new RSVP.Promise(function (resolve, reject) {

                var transportPromise = infrastructure.getTransport();
                transportPromise.then(function (transport) {
                    var transChannel = transport.openChannel(parsedUrl.hostname, protocolName);
                    var clientChannel = channel.create("client-api");
                    var rpcProtocolClientHandler = clientHandler(remoteServiceUrl);
                    clientChannel.rightHandler(rpcProtocolClientHandler);
                    transChannel.handler(rpcProtocolClientHandler);

                    var callback = function (event) {
                        if (!event) {
                            logger.warn('client-api promise failed check! calling promise.reject()');
                            reject(event);
                        } else {
                            logger.trace('promise calling promise.resolve() event.id=' + event.id);
                            logger.debug("RPC Incoming message is " + JSON.stringify(event))
                            resolve(event);
                        }
                    };
                    if (clientCallback) callback = clientCallback;
                    clientChannel.leftConnection().listen(callback);
                    clientChannel.leftConnection().send(data);
                });
                /*
                 setTimeout(function() { //TODO <-- dirty hack to work around lack of promises in ...
                 // muon stack and so transport can be set in infrastructure before this executes :-(
                 // must convert api to promises all the way through
                 var transChannel = infrastructure.transport.openChannel(parsedUrl.hostname, protocolName);
                 var clientChannel = channel.create("client-api");
                 var rpcProtocolClientHandler = clientHandler(remoteServiceUrl);
                 clientChannel.rightHandler(rpcProtocolClientHandler);
                 transChannel.handler(rpcProtocolClientHandler);

                 var callback = function(event) {
                 if (! event) {
                 logger.warn('client-api promise failed check! calling promise.reject()');
                 reject(event);
                 } else {
                 logger.trace('promise calling promise.resolve() event.id=' + event.id);
                 resolve(event);
                 }
                 };
                 if (clientCallback) callback = clientCallback;
                 clientChannel.leftConnection().listen(callback);
                 clientChannel.leftConnection().send(data);

                 }, 150);
                 */
            });


            return promise;

        },
        handle: function (endpoint, callback) {
            logger.debug('[*** API ***] registering handler endpoint: ' + endpoint);
            handlerMappings[endpoint] = callback;
            logger.trace('handlermappings=' + JSON.stringify(handlerMappings));
            //console.dir(handlerMappings);
        },
        protocolHandler: function () {
            return {
                server: function () {
                    return serverHandler();
                },
                client: function (remoteServiceUrl) {
                    return clientHandler(remoteServiceUrl);
                }
            }
        }
    }
    return api;
}


function serverHandler() {

    var incomingMuonMessage;

    class RpcProtocolHandler extends Handler {

        outgoingFunction(message, forward, back, route, close) {
            logger.debug("[*** PROTOCOL:SERVER:RPC ***] server rpc protocol outgoing message=%s", JSON.stringify(message));
            var serverResponse = {
                status: 200,
                body: messages.encode(message),
                content_type: "application/json"
            };
            var outboundMuonMessage = messages.muonMessage(serverResponse, serviceName, incomingMuonMessage.origin_service, protocolName, "request.response");
            logger.trace("[*** PROTOCOL:SERVER:RPC ***] rpc protocol outgoing muonMessage=" + JSON.stringify(outboundMuonMessage));
            forward(outboundMuonMessage);
            close('server_outgoing');

        }

        incomingFunction(message, forward, back, route, close) {

            if (!message) {
                logger.warn('received empty message');
                back({});
            }
            incomingMuonMessage = message;
            logger.debug("[*** PROTOCOL:SERVER:RPC ***] rpc protocol incoming message=%s", JSON.stringify(incomingMuonMessage));
            logger.trace("[*** PROTOCOL:SERVER:RPC ***] rpc protocol incoming message type=%s", (typeof incomingMuonMessage));
            if (message.protocol == 'muon') {
                logger.error(JSON.stringify(message))
                // forward(message);
                // shutdown();
                return;
            }
            try {
                var payload = messages.decode(incomingMuonMessage.payload, incomingMuonMessage.content_type);
                logger.info("[*** PROTOCOL:SERVER:RPC ***] RPC payload =%s", JSON.stringify(payload));
                logger.trace('handlermappings=' + JSON.stringify(handlerMappings));
                var endpoint = payload.url;
                payload.body = messages.decode(payload.body, payload.content_type)
                var path = '/' + endpoint.split('/')[3];
                var handler = handlerMappings[path];
                if (!handler) {
                    logger.warn('[*** PROTOCOL:SERVER:RPC ***] NO HANDLER FOUND FOR ENDPOINT: "' + path + '" RETURN 404! event.id=' + incomingMuonMessage.id);
                    payload.status = 404
                    var return404msg = messages.resource404(incomingMuonMessage, payload);
                    back(return404msg);
                } else {
                    logger.info('[*** PROTOCOL:SERVER:RPC ***] Handler found for endpoint "' + path + '" event.id=' + incomingMuonMessage.id);
                    route(payload, path);
                }
            } catch (err) {
                logger.warn('[*** PROTOCOL:SERVER:RPC ***] error thrown during protocol message decoding and handling');
                logger.warn(err);
            }

            if (message.channel_op == 'closed') {
                shutdown();
                close('server_incoming');
                return;
            }
        }
    }
    ;

    var rpcProtocolHandler = new RpcProtocolHandler('server-rpc', handlerMappings);
    return rpcProtocolHandler;
}


function clientHandler(remoteServiceUrl) {
    var TIMEOUT_MS = 10000;
    var responseReceived = false;
    var remoteService = nodeUrl.parse(remoteServiceUrl, true).hostname;

    class RpcProtocolHandler extends Handler {

        outgoingFunction(message, forward, back, route, close) {
            logger.debug("[*** PROTOCOL:CLIENT:RPC ***] client rpc protocol outgoing message=%s", JSON.stringify(message));
            var request = {
                url: remoteServiceUrl,
                body: messages.encode(message),
                content_type: "application/json"
            };
            var muonMessage = messages.muonMessage(request, serviceName, remoteService, protocolName, "request.made");
            logger.trace("[*** PROTOCOL:CLIENT:RPC ***] client rpc protocol outgoing muonMessage=%s", JSON.stringify(muonMessage));
            forward(muonMessage);

            setTimeout(function () {
                if (!responseReceived) {
                    logger.info('[*** PROTOCOL:CLIENT:RPC ***] timeout reached responding with timeout message');
                    var timeoutMsg = createRpcMessage("timeout", remoteServiceUrl, {}, {
                        status: 'timeout',
                        body: 'rpc response timeout exceeded'
                    });
                    back(timeoutMsg);
                    close('client_outgoing');
                }
            }, TIMEOUT_MS);
        }


        incomingFunction(message, forward, back, route, close) {

            logger.info("[*** PROTOCOL:CLIENT:RPC ***] rpc protocol incoming message id=" + message.id);
            logger.debug("[*** PROTOCOL:CLIENT:RPC ***] rpc protocol incoming message=%s", JSON.stringify(message));
            if (message.channel_op == 'closed') {
                shutdown();
                var msg;
                if (message.step.includes("noserver") ) {
                    msg = createRpcMessage("noserver", remoteServiceUrl, {}, {
                        status: 'closed',
                        body: 'server cannot be found'
                    });
                } else {
                    msg = createRpcMessage("closed", remoteServiceUrl, {}, {
                        status: 'closed',
                        body: 'rpc socket closed by muon'
                    });
                }
                forward(msg);
                return;
            }

            responseReceived = true;
            var rpcMessage = messages.decode(message.payload, message.content_type)
            if (rpcMessage.body != undefined) {
                rpcMessage.body = messages.decode(rpcMessage.body, rpcMessage.content_type)
            }
            logger.info("Sending the response payload " + JSON.stringify(rpcMessage));
            forward(rpcMessage);
            close('client_incoming');
        }

    }
    ; //RpcProtocolHandler


    var rpcProtocolHandler = new RpcProtocolHandler('client-rpc');
    return rpcProtocolHandler;

}


function shutdown() {
    logger.warn('rpc protocol shutdown() called');
}


function createRpcMessage(statusCode, url, body, error) {
    if (!body) body = {};
    if (!error) error = {};
    if (!statusCode) {
        var error = new Error('rpcMessage() invalid status: "' + statusCode + '"');
        logger.error(error);
        throw error;
    }
    var rpcMsg = {
        id: 'rpc-gen',
        body: body,
        status: statusCode,
        url: url,
        error: error,
        endpoint: function () {
            return nodeUrl.parse(url, true).path;
        }
    }
    return rpcMsg;
}

function rpcRequest(statusCode, url, body, error) {
    if (!body) body = {};
    if (!error) error = {};
    if (!statusCode) {
        var error = new Error('rpcMessage() invalid status: "' + statusCode + '"');
        logger.error(error);
        throw error;
    }
    var rpcMsg = {
        body: body,
        status: statusCode,
        url: url,
        error: error,
        endpoint: function () {
            return nodeUrl.parse(url, true).path;
        }
    }
    return rpcMsg;
}
