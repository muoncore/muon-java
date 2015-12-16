package io.muoncore.extension.amqp

class MyTestClass {
  String someValue
  int someOtherValue

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    MyTestClass that = (MyTestClass) o

    if (someOtherValue != that.someOtherValue) return false
    if (someValue != that.someValue) return false

    return true
  }

  int hashCode() {
    int result
    result = (someValue != null ? someValue.hashCode() : 0)
    result = 31 * result + someOtherValue
    return result
  }
}
