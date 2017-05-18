package io.muoncore.protocol.rpc.client

import spock.lang.Specification

class JSProtocolSpec extends Specification {

  def "can run stuff"() {

    def js = JSProtocol.getClass().getResourceAsStream("/client.js")

    def proto = new JSProtocol(js)

    when:
    proto.execute()

    then:
    1==2

  }

}
