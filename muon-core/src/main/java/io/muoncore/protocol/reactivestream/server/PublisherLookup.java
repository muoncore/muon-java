package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;

public interface PublisherLookup {
    List<PublisherRecord> getPublishers();
    Optional<PublisherRecord> lookupPublisher(String name);
    void addPublisher(PublisherRecord publisherRecord);

    class PublisherRecord {
        private String name;
        private PublisherType publisherType;
        private Publisher publisher;

        public PublisherRecord(String name, PublisherType publisherType, Publisher publisher) {
            this.name = name;
            this.publisherType = publisherType;
            this.publisher = publisher;
        }

        public String getName() {
            return name;
        }

        public PublisherType getPublisherType() {
            return publisherType;
        }

        public Publisher getPublisher() {
            return publisher;
        }
    }

    enum PublisherType {
        HOT, COLD, HOT_COLD
    }
}

