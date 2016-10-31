var bichannel = require('../../muon/infrastructure/channel.js');
var handler = require('../../muon/infrastructure/handler.js');
var assert = require('assert');
var expect = require('expect.js');
require('sexylog');
var csp = require("js-csp");

describe("Bi directional channel test", function () {

    this.timeout(4000);


      after(function() {
            //bi-channel.closeAll();
      });

    it("channel sends and receives messages via callbacks", function (done) {

         var client1 = function(connection) {
            connection.send("sent by client 1");
            connection.listen(function(response) {
                    assert.equal(response, "sent by client 2");
                    done();
            });
         };

         var client2 = function(connection) {
            connection.listen(function(response) {
                     connection.send("sent by client 2");
            });
         }

         var channel = bichannel.create("test-1");
         logger.trace("channel: " + JSON.stringify(channel));
         client1(channel.leftConnection());
         client2(channel.rightConnection());

    });


    it("channel sends and receives messages via go synchronous looking calls", function (done) {

         var client1 = function(connection) {
            connection.send("sent by client 1");

            csp.go(function*() {
              var response = yield csp.take(connection.listen());
               assert.equal(response, "sent by client 2");
               done();
            });
         }

         var client2 = function(connection) {
                csp.go(function*() {
                  var response = yield csp.take(connection.listen());
                   assert.equal(response, "sent by client 1");
                   connection.send("sent by client 2");
                });
         }

         var channel = bichannel.create("test-2");
         client1(channel.leftConnection());
         client2(channel.rightConnection());

    });

    it("compose two channels joined via handler", function (done) {

         var reqResHandler = handler.create();
         reqResHandler.outgoing(function(event, accept, reject){
                if (! event) {
                    throw new Error('reqResHandler: event is null');
                }
                accept(event);
         });
         reqResHandler.incoming(function(event, accept, reject){
                 if (! event) {
                     throw new Error('reqResHandler: event is null');
                 }
                 accept(event);
          });



         var channelA = bichannel.create("test3A");
         var channelB = bichannel.create("test3B");

         channelA.rightHandler(reqResHandler);
         channelB.leftHandler(reqResHandler);


         var event = {id: 3, payload: "test payload 3"};

         channelA.leftConnection().listen(function(e) {
                assert(e.payload == event.payload);
                done();
         });

         channelB.rightConnection().listen(function(e) {
            channelB.rightConnection().send(e);
         });

         channelA.leftConnection().send(event);
    });


    it("null event triggers handler to throw error", function (done) {

         var reqResHandler = handler.create();
         reqResHandler.outgoing(function(event, accept, reject){
                if (! event.id) {
                   reject({status: 'error'});
                }
                accept(event);
         });
         reqResHandler.incoming(function(event, accept, reject){
                 if (! event.id) {
                     reject({status: 'error'});
                 }
                 accept(event);
          });

         var channelA = bichannel.create("test3A");
         var channelB = bichannel.create("test3B");

         channelA.rightHandler(reqResHandler);
         channelB.leftHandler(reqResHandler);

         channelA.leftConnection().listen(function(e) {
                assert(e.status == 'error');
                done();
         });

         var event = {};
         channelA.leftConnection().send(event);
    });



        it("test right channel connection endpoint adapter for io", function (done) {

                var channel = bichannel.create('test');

                var object =  {
                    send: function(o, f) {
                            f('PONG');
                    }
                }

                channel.rightEndpoint(object, 'send');
                channel.leftConnection().send('PING');

                channel.leftConnection().listen(function(reply) {
                    assert.equal('PONG', reply);
                    done();
                });
        });


        it("test left channel connection endpoint adapter for io", function (done) {

                var channel = bichannel.create('test');

                var object =  {
                    send: function(o, f) {
                            f('PONG');
                    }
                }

                channel.leftEndpoint(object, 'send');
                channel.rightConnection().send('PING');

                channel.rightConnection().listen(function(reply) {
                    assert.equal('PONG', reply);
                    done();
                });
        });



    it("left to right channel sends and receives error via onError function", function (done) {
        var err = new Error('some random boring exceptional problem');

         var leftClient = function(connection) {
            connection.send(err);

            connection.listen(function(response) {
                     done(new Error('left connection listener should not recieve message'));
            });
            connection.onError(function(error){
                    done(new Error('left connection error listener should not recieve message'));
            });
         };

         var rightClient = function(connection) {
            connection.listen(function(response) {
                     done(new Error('right connection listener should not recieve message'));
            });
            connection.onError(function(error){
                    assert.ok(error);
                    done();
            });

         }

         var channel = bichannel.create("error-test");
         leftClient(channel.leftConnection());
         rightClient(channel.rightConnection());

    });

    it("right to left channel sends and receives error via onError function", function (done) {
        var err = new Error('some random boring exceptional problem');

         var rightClient = function(connection) {
            connection.send(err);

            connection.listen(function(response) {
                     done(new Error('right connection listener should not recieve message'));
            });
            connection.onError(function(error){
                    done(new Error('right connection error listener should not recieve message'));
            });
         };

         var leftClient = function(connection) {
            connection.listen(function(response) {
                     done(new Error('left conection listener should not recieve message'));
            });
            connection.onError(function(error){
                    assert.ok(error);
                    done();
            });

         }

         var channel = bichannel.create("error-test");
         leftClient(channel.leftConnection());
         rightClient(channel.rightConnection());

    });

    it("left to right channel throws and receives error via onError function", function (done) {
        var err = new Error('some random boring exceptional problem');

         var leftClient = function(connection) {
            connection.throwErr(err);

            connection.listen(function(response) {
                     done(new Error('left connection listener should not recieve message'));
            });
            connection.onError(function(error){
                    done(new Error('left connection error listener should not recieve message'));
            });
         };

         var rightClient = function(connection) {
            connection.listen(function(response) {
                     done(new Error('right connection listener should not recieve message'));
            });
            connection.onError(function(error){
                    assert.ok(error);
                    done();
            });

         }

         var channel = bichannel.create("error-test");
         leftClient(channel.leftConnection());
         rightClient(channel.rightConnection());

    });

    it("right to left channel throws and receives error via onError function", function (done) {
        var err = new Error('some random boring exceptional problem');

         var rightClient = function(connection) {
            connection.throwErr(err);

            connection.listen(function(response) {
                     done(new Error('right connection listener should not recieve message'));
            });
            connection.onError(function(error){
                    done(new Error('right connection error listener should not recieve message'));
            });
         };

         var leftClient = function(connection) {
            connection.listen(function(response) {
                     done(new Error('left conection listener should not recieve message'));
            });
            connection.onError(function(error){
                    assert.ok(error);
                    done();
            });

         }

         var channel = bichannel.create("error-test");
         leftClient(channel.leftConnection());
         rightClient(channel.rightConnection());

    });


    it("channel validates messages", function (done) {

         var leftClient = function(connection) {

            connection.onError(function(err) {
                    assert.ok(err);
                    assert.ok(err instanceof Error);
                    done();
            });
            connection.listen(function(response) {
                    done(new Error('left listener should not have received message'));
            });
         };

         var rightClient = function(connection) {
            connection.listen(function(response) {
                     done(new Error('right listener should not have received message'));
            });
            connection.onError(function(response) {
                     done(new Error('right client should not have received message'));
            });
         }

         var validiator = {
            validate: function(msg) {
                if (! msg.id) {
                    throw new Error('invalid message');
                }
            }
         }

         var channel = bichannel.create("test-1", validiator);
         leftClient(channel.leftConnection());
         rightClient(channel.rightConnection());
         channel.leftConnection().send("invalid message");
    });



    it("channel does not validate error messages", function (done) {

         var leftClient = function(connection) {
            connection.onError(function(err) {
                    done(new Error('left client error listener should not have received message'));
            });
            connection.listen(function(response) {
                    done(new Error('left listener should not have received message'));
            });
         };

         var rightClient = function(connection) {
            connection.listen(function(response) {
                     done(new Error('right listener should not have received message'));
            });
            connection.onError(function(err) {
                     assert.ok(err);
                     assert.ok(err instanceof Error);
                     done();
            });
         }

         var validiator = {
            validate: function(msg) {
                if (! msg.id) {
                    throw new Error('invalid message');
                }
            }
         }

         var channel = bichannel.create("test-1", validiator);
         leftClient(channel.leftConnection());
         rightClient(channel.rightConnection());
         channel.leftConnection().send(new Error('non valdiating errror'));
    });


      it("unable to send messages on closed channel", function (done) {
          var channel = bichannel.create("test-a-kimbo", function() {return true}, 50);
          channel.close();

          setTimeout(function(){
            expect(
              channel.leftConnection().send('blah')
            ).to.be(false);
            done();
          }, 200);
      });

      it("close() call sends channel_op=shutodwn message downstream", function (done) {
          var channel = bichannel.create("test-a-kimbo", function() {return true}, 50);

          channel.rightConnection().listen(function(msg) {
              expect(msg.channel_op).to.be('closed');
              done();
          });

          channel.close();

          setTimeout(function() {


          }, 200);

      });

      it("unable to listen on closed channel", function (done) {
          var channel = bichannel.create("test-a-kimbo", function() {return true}, 50);
          channel.close();

          setTimeout(function(){
            expect(function() {
              channel.leftConnection().listen(function() {});
            }).to.throwException(/csp channel closed/);
            done();
          }, 200);

      });

      it("unable to send messages on closed left connection", function (done) {
          var channel = bichannel.create("test-a-kimbo", function() {return true}, 50);
          channel.leftConnection().close();

          setTimeout(function(){
            expect(
              channel.leftConnection().send('blah')
            ).to.be(false);
            done();
          }, 200);
      });


      it("unable to send messages on closed right connection", function (done) {
          var channel = bichannel.create("test-a-kimbo", function() {return true}, 50);
          channel.rightConnection().close();

          setTimeout(function(){
            expect(
              channel.leftConnection().send('blah')
            ).to.be(false);
            done();
          }, 200);
      });

});
