var Joi = require('joi');
var uuid = require('node-uuid');
require('sexylog');
var jsonutil = require('jsonutil');
var stackTrace = require('stack-trace');



var schema = Joi.object().keys({
   id: Joi.string().guid().required(),
   created: Joi.date().timestamp('javascript'),
   target_service: Joi.string().optional(),//.min(3).required(),
   origin_service: Joi.string().min(3).required(),
   // url: Joi.string().uri().required(),
   protocol: Joi.string().min(3).optional(),//.required(),
   step: Joi.string().min(3).required(),
   provenance_id: Joi.string().guid().optional(),
   content_type: Joi.string().min(3).required(),
   status: Joi.string().optional(),
   payload: Joi.any().required(),
   channel_op: Joi.string().min(3).regex(/(normal|closed|shutdown)/).required(),
   event_source: Joi.string().optional()
});

exports.validate = function(message) {
    return validateSchema(message);
}



function validateSchema(message) {
    var validatedMessage = Joi.validate(message, schema);
    if (validatedMessage.error) {
        logger.warn('invalid message: \n', message);
        logger.info('invalid joi schema for message! details: ' + JSON.stringify(validatedMessage.error.details));
        var error = new Error('Error! problem validating muon message schema: ' + JSON.stringify(validatedMessage.error))
        logger.error(error.stack);
        throw error;
    }
    return message;
}

exports.createMessage = function(payload, headers, source) {
        return createMessage(payload, headers, source);
}

exports.copy = function(json) {
    return jsonutil.deepCopy(json);
}


exports.rpcServer404 = function(rpcMessage) {
    var copy =  jsonutil.deepCopy(rpcMessage);
    var resource = url.format(rpcMessage.url).pathname;
    copy.target_service = 'unknown';
    copy.origin_service = 'unknown';
    copy.status = "failure";
    copy.step = 'request.invalid';
    copy.payload = {status: '404', body: 'no matching resource for url ' + url};
    return copy;
}


exports.resource404 = function(message, rpcpayload) {
    var copy =  jsonutil.deepCopy(message);
    logger.debug("Creating RPC response for MuonMessage " + JSON.stringify(message))
    logger.debug("Creating RPC response for RPC Request " + JSON.stringify(rpcpayload))
    copy.target_service = message.origin_service;
    copy.origin_service = message.target_service;
    copy.status = "failure";
    copy.step = 'request.invalid';
    copy.provenance_id = message.id;
    copy.payload = encode({status: '404', message: 'no matching resource for url ' + rpcpayload.url});
    return copy;
}

exports.serverFailure = function(msg, protocol, status, text) {
    var copy =  jsonutil.deepCopy(msg);
    copy.target_service = msg.origin_service;
    copy.origin_service = msg.target_service;
    copy.status = "failure";
    copy.step = protocol  + '.' + status;
    copy.provenance_id = msg.id;
    copy.payload = {status: status, message: text};
    copy.channel_op = "closed"
    return copy;
}

exports.clientFailure = function(rpcMsg, protocol, status, text) {
    var copy =  jsonutil.deepCopy(rpcMsg);
    copy.status = "failure";
    copy.step = protocol  + '.' + status;
    copy.provenance_id = rpcMsg.id;
    copy.payload = {status: status, message: text};
    return copy;
}

exports.failure = function(protocol, status, text) {
    var payload = {status: status, message: text};
    var headers = {

    };
    var msg =  createMessage(payload, headers);
    msg.status = "failure";
    msg.step = protocol  + '.' + status;
    msg.channel_op="closed"
    return msg;
}

exports.shutdownMessage = function() {

  var messageid = uuid.v4();

  var headers = {
        step: 'ChannelShutdown',
        protocol: 'n/a',
        event_source: callingObject(),
        target_service: 'n/a',
        origin_service: 'n/a',
        content_type: 'application/json',
        channel_op: 'closed'
  };

 var message = createMessage({}, headers);
 return validateSchema(message);
}


exports.pingMessage = function() {

  var messageid = uuid.v4();

  var headers = {
        step: 'keep-alive',
        protocol: 'muon',
        event_source: callingObject(),
        target_service: 'n/a',
        origin_service: 'n/a',
        content_type: 'application/json'
  };

 var message = createMessage({}, headers);
 return validateSchema(message);
}

exports.muonMessage = function(payload, sourceService, targetService, protocol, step) {

   //logger.trace("messages.payload='" +  JSON.stringify(payload) + "', sourceService='" +  sourceService + "')");

    var messageid = uuid.v4();

    var headers = {
          step: step,
          protocol: protocol,
          event_source: callingObject(),
          target_service: targetService,
          origin_service: sourceService,
    };

   var message = createMessage(payload, headers);
   return validateSchema(message);

};





exports.responseMessage = function(payload, client, server) {

   logger.trace("messages.muonMessage(payload='" +  payload + "', server='" +  server + "')");

    var messageid = uuid.v4();

    var headers = {
          step: "request.response",
          protocol: "rpc",
          event_source: callingObject(),
          target_service: client,
          origin_service: server,
    };

   var message = createMessage(payload, headers);
   return validateSchema(message);

};


exports.decode = function(payload, contentType) {
    return decode(payload, contentType);

}

function decode(payload) {
      //logger.trace('message decode: ' + payload);
      //logger.trace('message decode: ' + JSON.stringify(payload));
       if (! payload) {
         throw new Error('cannot decode undefined payload!');
       }

       if (Object.keys(payload).length === 0 && payload.constructor === Object) {
         payload = [];
      }

       //logger.warn('decode() payload type: ' + (typeof payload));
       //logger.warn('decode() payload instanceof array: ' + (payload instanceof Array));
       //logger.warn('decode() payload array constructor: ' + (payload.constructor === Array));
       //logger.warn('decode() payload: ' + JSON.stringify(payload));

       if ( ! payload instanceof Array ) {
            logger.error('payload to decode is not of expected type: ' + JSON.stringify(payload));
            throw new Error('can only decode payloads as byte array of type Buffer');
       }

       var buffer = new Buffer(payload);
       var value;
       try {
            var value = JSON.parse(buffer.toString());
       } catch (err) {
            value = buffer.toString();
        }

       return value;

}


exports.encode = function(payload) {
return encode(payload);

}

function encode(payload) {
    var encoded;
     if (typeof payload === 'string') {
        encoded = new Buffer(payload, 'utf8').toJSON().data;
     } else if (typeof payload === 'object'){
        encoded = new Buffer(JSON.stringify(payload), 'utf8').toJSON().data;
     } else {
        encoded = new Buffer(payload.toString(), 'utf8').toJSON().data;
     }
    return encoded;
}


function createMessage(payload, headers, source) {
    //logger.trace('createMessage(payload='  + JSON.stringify(payload) + ', headers='  + JSON.stringify(headers) +  ')');
    if (! payload) payload = {};

    if (typeof payload == 'object') {
        headers.content_type = "application/json";
    } else if (typeof payload == 'string') {
        logger.debug("PAYLOAD IS A STRING, setting to text/plain")
        headers.content_type = "text/plain";
    } else if (! headers.content_type) {
        logger.debug("Content type is falsy ... , setting to text/plain")
        headers.content_type = "text/plain";
    } else {
        //do nothing?
    }



    if (! headers.channel_op) headers.channel_op = 'normal';
    if (source) headers.event_source = source;
    if (! headers.event_source) headers.event_source = callingObject();
    if (! headers.channel_op) headers.channel_op = 'normal';

     var message =  {
       id: uuid.v4(),
       created: new Date().getTime(),
       target_service:  headers.target_service,
       origin_service: headers.origin_service,
       protocol: headers.protocol,
       step:  headers.step,
       provenance_id: headers.provenance_id,
       content_type: headers.content_type,
       status: headers.status,
       payload: encode(payload),
       channel_op:  headers.channel_op,
       event_source: headers.event_source
     }

    // logger.trace('createMessage() return message='  + JSON.stringify(message));
    return message;
}


function callingObject() {

    if (typeof window != 'undefined' && typeof window.location != undefined) {
        logger.debug("Running in a browser context, not collecting calling object")
        return "web"
    }
    var err = new Error('something went wrong');
    var trace = stackTrace.parse(err);
    var stackCounter = 1;
     // <-- TODO to get correct file name you may need to tweak the call stack index

    var inThisObject = true;
    var object = 'messages.js';
    while(inThisObject) {
        var call = trace[stackCounter];
        var file = call.getFileName();
        var pathElements = file.split('/');
        var object = pathElements[pathElements.length - 1];
        //logger.trace('in stacktrace: object=' + object);
        if (object === 'messages.js') {
            stackCounter++;
        } else {
            inThisObject = false;
        }
        object = object + ':' + call.getLineNumber();
    }
	return object;
}
