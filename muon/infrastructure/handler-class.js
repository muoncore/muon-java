"use strict";

var messages = require('../domain/messages.js');
var stackTrace = require('stack-trace');

class Handler {




  constructor(n, handlers) {
    if (this.incomingFunction === undefined) {
      throw new TypeError("Must override method incomingFunction()");
    }

    if (this.outgoingFunction === undefined) {
      throw new TypeError("Must override method outgoingFunction()");
    }

    if (! n) n = 'default';
    this.name = n + '-handler';
    if (handlers) {
      this.callbacks = handlers;
    } else {
      this.callbacks = {};
    }

    //this.upstreamConnection = {};
    //this.downstreamConnection = {};



  }



  outgoing(f) {
      this.outgoingFunction = f;
  }

  incoming(f) {
      this.incomingFunction = f;
  }

  register(callback, key) {
      this.callbacks[key] = callback;
  }

  sendDownstream(msg, accept, reject) {
      logger.debug('[*** CSP-CHANNEL:HANDLER ***] ' + this.name + ' sending message via handler downstream msg: ' +  JSON.stringify(msg));
      if (! msg) {
        logger.warn('handler received empty message downstream');
        var err = new Error();
        logger.warn(err.stack);
        reject();
        return;
      }
      var route = this.createRoute(this.upstreamConnection, this.incomingFunction);
      var close = closeSocket(this.downstreamConnection);
      //logger.error('sendDownStream() outgoingFunction() close=' + close.toString());
      //console.dir(close);
      this.outgoingFunction(msg, accept, reject, route, close);
  }

  sendUpstream(msg, accept, reject) {
      logger.debug('[*** CSP-CHANNEL:HANDLER ***] ' + this.name + ' sending message via handler upstream event.id=' + JSON.stringify(msg));
      if (! msg) {
        logger.warn('handler received empty message upstream');
        var err = new Error();
        logger.warn(err.stack);
        reject();
        return;
      }
      var route = this.createRoute(this.downstreamConnection, this.outgoingFunction);
      var close = closeSocket(this.downstreamConnection);
      //logger.error('sendUpStream() incomingFunction() close=' + close.toString());
      //console.dir(close);
      this.incomingFunction(msg, accept, reject, route, close);
  }

  getUpstreamConnection() {
      return this.upstreamConnection;
  }

  getDownstreamConnection() {
      return this.downstreamConnection;
  }

  setUpstreamConnection(c) {
      //logger.error(this.name + ' setUpstreamConnection(c=' +  JSON.stringify(c) + ')');
      //console.dir(c);
      this.upstreamConnection = c;
  }

  setDownstreamConnection(c) {
      //logger.error(this.name + ' setDownstreamConnection(c=' +  JSON.stringify(c) + ')');
      //console.dir(c);
      this.downstreamConnection = c;
  }

  otherConnection(conn) {
      if (conn === this.upstreamConnection.name()) {
          logger.trace('[*** CSP-CHANNEL:HANDLER ***]  ' + this.name + ' other connection is downstream: ' + this.downstreamConnection.name());
          return this.downstreamConnection;
      } else {
          logger.trace('[*** CSP-CHANNEL:HANDLER ***] ' + this.name + ' other connection is upstream: ' + this.upstreamConnection.name());
          return this.upstreamConnection;
      }
  }

  thisConnection(conn) {
      //logger.error('this.upstreamConnection=' +  JSON.stringify(this.upstreamConnection));
      //logger.error('this.upstreamConnection=', this.upstreamConnection);
      //console.dir(this.upstreamConnection);
      //logger.error('this.downstreamConnection=' +  JSON.stringify(this.downstreamConnection));
      //console.dir(this.downstreamConnection);
      if (this.upstreamConnection && conn === this.upstreamConnection.name()) {
          logger.trace('[*** CSP-CHANNEL:HANDLER ***]  ' + this.name + ' other connection is downstream: ' + this.downstreamConnection.name());
          return this.upstreamConnection;
      } else {
          var upstreamConnectionName = 'unset';
          if (this.upstreamConnection) upstreamConnectionName = this.upstreamConnection.name();
          logger.trace('[*** CSP-CHANNEL:HANDLER ***] ' + this.name + ' other connection is upstream: ' + upstreamConnectionName);
          return this.downstreamConnection;
      }
  }


  createRoute(otherConnection, handlerFunction) {
      var _callbacks = this.callbacks;
      var _downstreamConnection = this.downstreamConnection;

      var route = function(message, key) {
          var callbackHandler = _callbacks[key];
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
              var close = closeSocket(_downstreamConnection);
              handlerFunction(response, accept, reject, route, close);
          }
          logger.trace('[*** CSP-CHANNEL:HANDLER ***]  executing routed callback handler for key: ' + key);
          callbackHandler(message, tempCallback);
      }.bind(this);

      return route;

  }




}


function closeSocket(downstreamConnection) {
  var func = function(source) {
    logger.debug('[*** CSP-CHANNEL:HANDLER ***] close() source=' + source);
    setTimeout(function() {
      logger.debug('[*** CSP-CHANNEL:HANDLER ***] handler.close() called sending shutdown message');
      downstreamConnection.close();
      //upstreamConnection.close();
    }, 5000);
  }
  //logger.error('func=' + func.toString());
  return func;
}






module.exports = Handler;
