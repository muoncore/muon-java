"use strict";

var Pipeline = require('../../muon/infrastructure/pipeline.js');
var Handler = require('../../muon/infrastructure/handler-class.js');
var assert = require('assert');
var expect = require('expect.js');

describe("Pipeline test:", function () {

      after(function() {

      });

    it("build a pipline of channels and handlers", function (done) {


         var handler1 = new UppercaseTextHandler();
         var handler2 = new ReverseTextHandler();
        var pipeline = new Pipeline('cleint-stack');
         pipeline.add(handler1);
         pipeline.add(handler2);
         var left = pipeline.getLeft();
         var right = pipeline.getRight();

         left.leftConnection().send({text: "hi there"});
         right.rightConnection().listen(function(msg) {
           expect(msg.reverse).to.be('ereht ih');
           expect(msg.upper).to.be('HI THERE');
           expect(msg.text).to.be('hi there');
           done();
         });

    });


  });



  class ReverseTextHandler extends Handler {

    outgoingFunction(message, forward, back, route) {
      message.reverse = this.reverse(message.text);
      forward(message);
    }

    incomingFunction(message, forward, back, route) {
      message.reverse = this.reverse(message.text);
      forward(message);
    }

    reverse(s) {
       var o = '';
       for (var i = s.length - 1; i >= 0; i--)
         o += s[i];
       return o;
     }
  }



 class UppercaseTextHandler extends Handler {

   outgoingFunction(message, forward, back, route) {
     message.upper = message.text.toUpperCase();
     forward(message);
   }

   incomingFunction(message, forward, back, route) {
     message.upper = message.text.toUpperCase();
     forward(message);
   }
 }
