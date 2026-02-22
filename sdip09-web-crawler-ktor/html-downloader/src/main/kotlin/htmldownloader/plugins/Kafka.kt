package htmldownloader.plugins

import io.ktor.server.application.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

private lateinit var kafkaProducerInstance: KafkaProducer<String, String>
private lateinit var kafkaConsumerInstance: KafkaConsumer<String, String>

val Application.kafkaProducer: KafkaProducer<String, String>
    get() = kafkaProducerInstance

val Application.kafkaConsumer: KafkaConsumer<String, String>
    get() = kafkaConsumerInstance

fun Application.configureKafka() {
    val bootstrapServers = environment.config.propertyOrNull("kafka.bootstrap-servers")?.getString()
        ?: "localhost:9092"
    val groupId = environment.config.propertyOrNull("kafka.group-id")?.getString()
        ?: "html-downloader-group"
    val topic = environment.config.propertyOrNull("kafka.topic.url-discovery")?.getString()
        ?: "fru1tworld-webcrawling-url-discovery-v1"

    // Producer configuration
    val producerProps = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRIES_CONFIG, 3)
        put(ProducerConfig.LINGER_MS_CONFIG, 1)
    }
    kafkaProducerInstance = KafkaProducer(producerProps)

    // Consumer configuration
    val consumerProps = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    }
    kafkaConsumerInstance = KafkaConsumer(consumerProps)
    kafkaConsumerInstance.subscribe(listOf(topic))

    log.info("Kafka configured: bootstrap=$bootstrapServers, groupId=$groupId, topic=$topic")

    environment.monitor.subscribe(ApplicationStopped) {
        kafkaProducerInstance.close()
        kafkaConsumerInstance.close()
        log.info("Kafka producer and consumer closed")
    }
}
