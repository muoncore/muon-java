package io.muoncore.codec.crypt

import io.muoncore.codec.Codecs
import spock.lang.Specification

class EncryptingCodecsSpec extends Specification {

    def "uses encryptor before passing to codecs"() {
        given:
        def delegate = Mock(Codecs)
        def algo = Mock(EncryptionAlgorithm) {
            getAlgorithmName() >> "AWESOMEALGO"
        }

        def codecs = new EncryptingCodecs(delegate, algo)
        def encodedNotEncrypted = [1, 2, 3] as byte[]
        def encrypted = [1, 2, 1] as byte[]

        when:
        def ret = codecs.encode([:], ["application/json+AWESOMEALGO"] as String[])

        then:
        1 * algo.encrypt(encodedNotEncrypted) >> encrypted
        1 * delegate.encode(_, _) >> new Codecs.EncodingResult(encodedNotEncrypted, "application/json")
        ret.payload == encrypted
        ret.contentType == "application/json+AWESOMEALGO"
    }

    def "uses decryptor before passing to codecs"() {

        given:
        def delegate = Mock(Codecs)
        def algo = Mock(EncryptionAlgorithm) {
            getAlgorithmName() >> "AWESOMEALGO"
        }

        def codecs = new EncryptingCodecs(delegate, algo)
        def encodedNotEncrypted = [1, 2, 3] as byte[]
        def encrypted = [1, 2, 1] as byte[]

        when:
        def ret = codecs.decode(encrypted, "application/json+AWESOMEALGO", Map)

        then:
        1 * algo.decrypt(encrypted) >> encodedNotEncrypted
        1 * delegate.decode(encodedNotEncrypted, "application/json", Map) >> [message:"awesome"]
        ret.message == "awesome"
    }

    def "does not use decryptor if not an encrypted content type"() {

        given:
        def delegate = Mock(Codecs)
        def algo = Mock(EncryptionAlgorithm) {
            getAlgorithmName() >> "AWESOMEALGO"
        }

        def codecs = new EncryptingCodecs(delegate, algo)
        def encodedNotEncrypted = [1, 2, 3] as byte[]

        when:
        def ret = codecs.decode(encodedNotEncrypted, "application/json", Map)

        then:
        0 * algo._
        1 * delegate.decode(encodedNotEncrypted, "application/json", Map) >> [message:"awesome"]
        ret.message == "awesome"
    }

    def "does not use encryptor if not an encrypted content type"() {
        given:
        def delegate = Mock(Codecs)
        def algo = Mock(EncryptionAlgorithm) {
            getAlgorithmName() >> "AWESOMEALGO"
        }

        def codecs = new EncryptingCodecs(delegate, algo)
        def encodedNotEncrypted = [1, 2, 3] as byte[]

        when:
        def ret = codecs.encode([:], ["application/json"] as String[])

        then:
        0 * algo._
        1 * delegate.encode(_, _) >> new Codecs.EncodingResult(encodedNotEncrypted, "application/json")
        ret.payload == encodedNotEncrypted
        ret.contentType == "application/json"
    }

    /*

      updates content types to add a +algo for each registered encryption algo., should check it against current algo(s)?

     */
}
