"use strict";

var bichannel = require('./channel.js');

class Pipeline {

  constructor(n) {
      this.name = n;
      this.handlers = [];
  }

  add(handler) {
    this.handlers.push(handler);
    if (! this.left) {
      this.left = bichannel.create(this.name + '-left-' + this.handlers.length);
      this.right = bichannel.create(this.name + '-right-' + this.handlers.length);
      this.left.rightHandler(handler);
      this.right.leftHandler(handler);
    } else {
      var newRight = bichannel.create(this.name + '-right-' + this.handlers.length);
      this.right.rightHandler(handler);
      newRight.leftHandler(handler);
      this.right  = newRight;
    }
    return this;
  }


  getLeft() {
    return this.left;
  }

  getRight() {
    return this.right;
  }

}


module.exports = Pipeline;
