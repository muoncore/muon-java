
var nodeUrl = require("url");
var channel = require('../../infrastructure/channel');
var uuid = require('node-uuid');
var RSVP = require('rsvp');
require('sexylog');
var _ = require("underscore")
var proto = require("./client-protocol")
var simpleapi = require("./client-simple-api")

var serviceName;
var protocols = [];
var protocolName = 'reactive-stream';


/**
 *
 * need to break out the protocol object.
 *
 * break out the api object. Needs to be transformable to other streaming providers
 *
 * Identify the wiring section.
 *
 * The proto object :-
 *
 *  * incoming message handler (from the transport)
 *  * local state
 *  * outgoing message handlers (from the api)
 *  * what is the internal protocol API? document it!
 *
 */


exports.getApi = function (name, infra) {
    serviceName = name;

    var api = {
        name: function () {
            return protocolName;
        },

        replay: function(streamName, config, clientCallback, errorCallback, completeCallback) {
            var ret = {}
            var muon = this
            infra.discovery.discoverServices(function(services) {
                var store = _.find(services.serviceList, function (it) {
                    logger.info ("checking " + JSON.stringify(it))
                    return _.contains(it.tags, "eventstore")
                })

                config['stream-name'] = streamName

                logger.info("eventstore = " +JSON.stringify(store))

                var subscriber = muon.subscribe("stream://" + store.identifier + "/stream", config, clientCallback, errorCallback, completeCallback)

                ret.cancel = subscriber.cancel
            })
            return ret
        },
        subscribe: function (remoteServiceUrl, params, clientCallback, errorCallback, completeCallback) {

            infra.getTransport().then(function(transport) {
                try {
                    logger.debug("Subscribing to " + remoteServiceUrl + " with params " + JSON.stringify(params))
                    var serviceRequest = nodeUrl.parse(remoteServiceUrl, true);
                    var targetService = serviceRequest.hostname
                    var transChannel = transport.openChannel(targetService, protocolName);
                    var targetStream = serviceRequest.path;
                    var args = params;
                    var protocol = proto.create(
                        subscriber,
                        transChannel,
                        targetService,
                        serviceName,
                        targetStream,
                        args);
                    protocol.start();
                } catch (e) {
                    logger.error("Error in stream subscription initialisation ", e)
                }
            });

            var subscriber = simpleapi.subscriber(clientCallback, errorCallback, completeCallback);
            return subscriber.control;
        },
        protocols: function (ps) {
            protocols = ps;
        },
        protocolHandler: function () {
            return {
                // todo, are these needed externally?

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
