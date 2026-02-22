package webcrawler.plugins

import io.ktor.server.application.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

private lateinit var kafkaProducerInstance: KafkaProducer<String, String>

val Application.kafkaProducer: KafkaProducer<String, String>
    get() = kafkaProducerInstance

fun Application.configureKafka() {
    val bootstrapServers = environment.config.propertyOrNull("kafka.bootstrap-servers")?.getString()
        ?: "localhost:9092"

    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRIES_CONFIG, 3)
        put(ProducerConfig.LINGER_MS_CONFIG, 1)
    }

    kafkaProducerInstance = KafkaProducer(props)

    log.info("Kafka producer configured with bootstrap servers: $bootstrapServers")

    environment.monitor.subscribe(ApplicationStopped) {
        kafkaProducerInstance.close()
        log.info("Kafka producer closed")
    }
}
