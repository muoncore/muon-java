package io.muoncore.extension.amqp;

import java.util.Map;

public interface QueueListener {

    void cancel();

    interface QueueFunction {
        void exec(QueueMessage message);
    }

    class QueueMessage {
        private String queueName;
        private byte[] body;
        private Map<String, String> headers;
        private String contentType;
        private String eventType;

        public QueueMessage(String eventType, String queueName, byte[] body, Map<String, String> headers, String contentType) {

            assert queueName != null;
            assert body != null;
            assert headers != null;
            assert contentType != null;
            this.eventType = eventType;
            this.queueName = queueName;
            this.body = body;
            this.headers = headers;
            this.contentType = contentType;
        }

        public String getEventType() {
            return eventType;
        }

        public String getQueueName() {
            return queueName;
        }

        public byte[] getBody() {
            return body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
