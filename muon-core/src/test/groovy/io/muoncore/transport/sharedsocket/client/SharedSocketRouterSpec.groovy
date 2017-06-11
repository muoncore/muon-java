package io.muoncore.transport.sharedsocket.client

import spock.lang.Specification

class SharedSocketRouterSpec extends Specification {

  def "when route asked for, route is created"() {

    def factory = Mock(SharedSocketRouteFactory)
    def router = new SharedSocketRouter(factory)

    when:
    router.openClientChannel("simples")

    then:
    1 * factory.createRoute("simples", _) >> Mock(SharedSocketRoute)
  }

  def "when route asked for many time, only one route is created"() {

    def factory = Mock(SharedSocketRouteFactory)
    def router = new SharedSocketRouter(factory)

    when:
    router.openClientChannel("simples")
    router.openClientChannel("simples")
    router.openClientChannel("simples")
    router.openClientChannel("simples")
    router.openClientChannel("simples")
    router.openClientChannel("simples")

    then:
    1 * factory.createRoute("simples", _) >> Mock(SharedSocketRoute)
  }

  def "when shared socket route fails, remove it from the list"() {

    expect:
    1==2
  }
}
