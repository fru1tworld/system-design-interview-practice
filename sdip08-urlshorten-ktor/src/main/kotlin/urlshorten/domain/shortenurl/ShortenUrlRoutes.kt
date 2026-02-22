package urlshorten.domain.shortenurl

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import urlshorten.plugins.database

fun Route.shortenUrlRoutes() {
    val repository = ShortenUrlRepository(application.database)
    val service = ShortenUrlService(repository)

    // Index page
    get("/") {
        call.respondHtml(HttpStatusCode.OK) {
            head {
                title { +"URL Shortener" }
                style {
                    +"""
                        body { font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px; }
                        h1 { color: #333; }
                        input[type="text"] { width: 100%; padding: 10px; margin: 10px 0; font-size: 16px; }
                        button { padding: 10px 20px; font-size: 16px; background: #007bff; color: white; border: none; cursor: pointer; }
                        button:hover { background: #0056b3; }
                        #result { margin-top: 20px; padding: 10px; background: #f0f0f0; display: none; }
                    """.trimIndent()
                }
            }
            body {
                h1 { +"URL Shortener" }
                div {
                    input(type = InputType.text, name = "url") {
                        id = "urlInput"
                        placeholder = "Enter URL to shorten..."
                    }
                    button {
                        onClick = "shortenUrl()"
                        +"Shorten"
                    }
                }
                div {
                    id = "result"
                }
                script {
                    unsafe {
                        +"""
                            async function shortenUrl() {
                                const url = document.getElementById('urlInput').value;
                                const response = await fetch('/shorten', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ originalUrl: url })
                                });
                                const data = await response.json();
                                const result = document.getElementById('result');
                                result.style.display = 'block';
                                result.innerHTML = '<strong>Short URL:</strong> <a href="' + data.shortUrl + '" target="_blank">' + data.shortUrl + '</a>';
                            }
                        """.trimIndent()
                    }
                }
            }
        }
    }

    // Create shorten URL
    post("/shorten") {
        val request = call.receive<ShortenUrlRequest>()

        if (!request.validate()) {
            throw IllegalArgumentException("잘못된 URL 형식입니다")
        }

        val shortCode = service.createShortenUrl(request.originalUrl)
        val fullShortUrl = "http://localhost:9999/$shortCode"

        call.respond(HttpStatusCode.OK, ShortenUrlResponse(fullShortUrl))
    }

    // Redirect to original URL
    get("/{shortenUrl}") {
        val shortenUrl = call.parameters["shortenUrl"]
            ?: throw IllegalArgumentException("단축 URL이 필요합니다")

        val originalUrl = service.readShortenUrl(shortenUrl)

        call.respondRedirect(originalUrl, permanent = true)
    }
}
