package webcrawler.domain.urldiscovery

import org.slf4j.LoggerFactory

class UrlDiscoveryService(
    private val producer: UrlDiscoveryProducer
) {
    private val log = LoggerFactory.getLogger(UrlDiscoveryService::class.java)

    suspend fun createUrlDiscovery(requestDto: UrlDiscoveryRequestDto): UrlDiscoveryResponseDto {
        return try {
            producer.publishUrlDiscoveryCreate(requestDto)
        } catch (e: Exception) {
            log.error("[UrlDiscoveryService] createUrlDiscovery failed: url={}", requestDto.startUrl, e)
            throw e
        }
    }

    suspend fun createUrlDiscoveries(requestDtos: List<UrlDiscoveryRequestDto>): UrlDiscoveryBatchResponseDto {
        val successResults = mutableListOf<UrlDiscoveryResponseDto>()
        val errors = mutableListOf<UrlDiscoveryErrorDto>()

        for (requestDto in requestDtos) {
            try {
                val response = producer.publishUrlDiscoveryCreate(requestDto)
                successResults.add(response)
            } catch (e: Exception) {
                log.error("[UrlDiscoveryService] createUrlDiscoveries failed: url={}", requestDto.startUrl, e)
                errors.add(UrlDiscoveryErrorDto.create(requestDto.startUrl, e.message ?: "Unknown error"))
            }
        }

        if (successResults.isEmpty()) {
            throw RuntimeException("All URL discovery requests failed")
        }

        return UrlDiscoveryBatchResponseDto.create(successResults, errors)
    }
}
