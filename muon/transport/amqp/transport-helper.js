var uuid = require('node-uuid');
var Joi = require('joi');
require('sexylog');
var messages = require('../../domain/messages.js');

// Regular expressions for transport message format:
var reply_queue_regex = /[a-z0-9\-]\.reply\.[a-zA-Z0-9\-]/;
var listen_queue_regex = /[a-z0-9\-]\.listen\.[a-zA-Z0-9\-]/;

var transportMessageSchema = Joi.object().keys({
   data: Joi.any().required(),
   properties: Joi.object().optional(),
   headers:  Joi.object({
       handshake: Joi.string().min(3).regex(/(initiated|accepted)/).optional(),
       protocol: Joi.alternatives().when('handshake', { is: 'initiated', then: Joi.string().required(), otherwise: Joi.string().optional() }),
       server_reply_q: Joi.alternatives().when('handshake', { is: 'initiated', then: Joi.string().required(), otherwise: Joi.forbidden() }),
       server_listen_q: Joi.alternatives().when('handshake', { is: 'initiated', then: Joi.string().required(), otherwise: Joi.forbidden() }),
       content_type: Joi.string().min(10).regex(/[a-z\.]\/[a-z\.]/).optional(),
   }).required()
});

exports.serviceNegotiationQueueName = function(serviceName) {

    var serviceQueueName = "service." + serviceName;
    return serviceQueueName;
}


exports.queueSettings = function() {
    var queueSettings = {
         durable: false,
         type: "direct",
         autoDelete: true,
         confirm: true
       };

       return queueSettings;
}


exports.handshakeRequestHeaders = function(protocol, listenQueue, replyQueue) {

  var headers = {
     handshake: "initiated",
     protocol: protocol,
     server_reply_q: replyQueue,
     server_listen_q: listenQueue,
    };
   return headers;

}

exports.isHandshakeAccept = function(msg) {
    return (msg.headers.handshake === 'accepted' || msg.headers.handshake === 'initiated');
}

exports.handshakeAcceptHeaders = function() {

  var headers = {
     handshake: "accepted",
   };
   return headers;

}


exports.handshakeRejectHeaders = function(message) {

  var headers = {
     handshake: "rejected",
     message: message
   };
   return headers;

}


exports.toWire = function(payload, headers) {
     logger.trace('message(payload='  + JSON.stringify(payload) + ', headers='  + JSON.stringify(headers) +  ')');
    if (! headers) headers = {};
    // if (! headers.content_type) headers["content_type"]=text/plain"
    var contents = messages.encode(payload);
    var message = {
        data: contents,
        headers: headers,
    }
    return message;
}



exports.encode = function(data) {
    if (typeof data === 'string') {
        return new Buffer(data);
    } else if(typeof data === 'object') {
         return new Buffer(JSON.stringify(data));
    } else {
        return new Buffer(data.toString());
    }
}

exports.decode = function(buffer) {
    return JSON.parse(buffer.toString());
}


exports.fromWire = function(msg) {
    try {
        var headers = msg.properties.headers;
        var contents = messages.decode(msg.content);
        //logger.trace('messages.fromWire(headers='  + JSON.stringify(headers) + ')');
        //logger.trace("messages.fromWire(contents=" + contents + ")");
        try {
            contents = JSON.parse(contents);
        } catch (err) {
            // do nothing, it's not an json object so can't be parsed
        }
        var message = {
            headers: headers,
            data: contents
        };
        //logger.trace('messages.fromWire() return message='  + JSON.stringify(message) );
       return message;
   } catch (err) {
        logger.error('error converting amqp wire format message to muon event message');
        logger.error(err);
        logger.error(err.stack);
        throw new Error(err);
   }
}


exports.validateMessage = function(msg) {
    return validate(msg);
}


function validate(message) {

     var validatedMessage = Joi.validate(message, transportMessageSchema);
        if (validatedMessage.error) {
            logger.info('invalid transport message: "' + JSON.stringify(message) + '"');
            logger.warn('invalid joi schema for transport message! details: ' + JSON.stringify(validatedMessage.error.details));
            logger.error(new Error(JSON.stringify(validatedMessage.error)).stack);
            throw new Error('Problem validating transport message schema: ' +JSON.stringify(validatedMessage.error));
        }
        return message;

}
