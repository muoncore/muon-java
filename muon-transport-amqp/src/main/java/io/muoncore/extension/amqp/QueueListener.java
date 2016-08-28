package io.muoncore.extension.amqp;

import java.util.Map;

import static io.muoncore.extension.amqp.QueueMessageBuilder.*;

public interface QueueListener {

    void cancel();

    interface QueueFunction {
        void exec(QueueMessage message);
    }

    class QueueMessage {
        private String queueName;
        private byte[] body;
        private Map<String, String> headers;

        public QueueMessage(String queueName, byte[] body, Map<String, String> headers) {

            assert queueName != null;
            assert body != null;
            assert headers != null;
            this.queueName = queueName;
            this.body = body;
            this.headers= headers;
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

        public String getProtocol() {
            return headers.get(HEADER_PROTOCOL);
        }

        public String getServerReplyTo() {
            return headers.get(HEADER_REPLY_TO);
        }

        public String getRecieveQueue() {
            return headers.get(HEADER_RECEIVE_QUEUE);
        }

        public String getContentType() {
            return headers.get(HEADER_CONTENT_TYPE);
        }

        public String getHandshakeMessage() {
            return headers.get(HEADER_HANDSHAKE);
        }

        @Override
        public String toString() {
            return "QueueMessage{" +
                    "queueName='" + queueName + '\'' +
                    ", headers=" + headers +
                    '}';
        }
    }

}
