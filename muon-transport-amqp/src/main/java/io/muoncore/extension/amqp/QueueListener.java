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
        private Map<String, Object> headers;
        private String contentType;

        public QueueMessage(String queueName, byte[] body, Map<String, Object> headers, String contentType) {
            this.queueName = queueName;
            this.body = body;
            this.headers = headers;
            this.contentType = contentType;
        }

        public String getQueueName() {
            return queueName;
        }

        public byte[] getBody() {
            return body;
        }

        public Map<String, Object> getHeaders() {
            return headers;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
