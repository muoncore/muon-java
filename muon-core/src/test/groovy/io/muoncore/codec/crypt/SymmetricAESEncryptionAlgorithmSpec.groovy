package io.muoncore.codec.crypt

import spock.lang.Specification

class SymmetricAESEncryptionAlgorithmSpec extends Specification {

    def "can encrypt and decrypt data"() {

        def algo = new SymmetricAESEncryptionAlgorithm("0123456789abcdef")

        def encrypted = algo.encrypt(data)
        def decrypted = algo.decrypt(encrypted)

        expect:
        encrypted != decrypted
        decrypted == data

        where:
        data || val
        "hello world".getBytes("UTF-8") | true
        [0,1,0,2,4,1,2] as byte[]       | true
    }

}
