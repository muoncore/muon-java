"use strict";

var nodeUrl = require("url");
var channel = require('../infrastructure/channel.js');
var uuid = require('node-uuid');
var RSVP = require('rsvp');
require('sexylog');
var messages = require('../domain/messages.js');


var handlerMappings = {};
var serviceName;
var protocolName = 'event';
exports.getApi = function (name, infrastructure) {
    serviceName = name;

    var api = {
        name: function () {
            return protocolName;
        },
        emit: function (event) {

            var promise = new RSVP.Promise(function (resolve, reject) {

                var transportPromise = infrastructure.getTransport();
                transportPromise.then(function (transport) {

                    infrastructure.discovery.discoverServices(function (services) {
                        var eventStore = services.findServiceWithTags(["eventstore"])

                        if (eventStore == undefined || eventStore == null) {
                            reject({
                                eventTime: null,
                                orderId: null,
                                status: "FAILED",
                                cause: "No event store could be found, is Photon running?"
                            })
                            return
                        }

                        var transChannel = transport.openChannel(eventStore.identifier, protocolName);

                        var callback = function (resp) {
                            if (!resp) {
                                logger.warn('client-api promise failed check! calling promise.reject()');
                                reject(resp);
                            } else {
                                logger.trace('promise calling promise.resolve() event.id=' + resp.id);
                                var payload = messages.decode(resp.payload)
                                logger.debug("EVENT Incoming message is " + JSON.stringify(payload))
                                resolve(payload);
                            }
                        };

                        var evMessage = messages.muonMessage(event, serviceName, eventStore.identifier, protocolName, "EventEmitted");

                        transChannel.listen(callback);
                        transChannel.send(evMessage);
                    });
                });
            });


            return promise;
        }
    }
    return api;
}