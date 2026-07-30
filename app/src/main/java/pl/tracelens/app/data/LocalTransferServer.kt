package pl.tracelens.app.data

import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.UUID
import kotlin.concurrent.thread

class LocalTransferServer(private val archive: File) : AutoCloseable {
    private val token = UUID.randomUUID().toString().replace("-", "")
    private val server = ServerSocket(0)
    @Volatile private var running = true
    val url: String = "http://${localAddress()}:${server.localPort}/$token/${archive.name}"

    init {
        thread(name = "TraceLens-transfer", isDaemon = true) {
            while (running) {
                runCatching {
                    server.accept().use { socket ->
                        val reader = socket.getInputStream().bufferedReader()
                        val request = reader.readLine().orEmpty()
                        while (!reader.readLine().isNullOrEmpty()) Unit
                        val valid = request.startsWith("GET /$token/")
                        val output = socket.getOutputStream().buffered()
                        if (valid) {
                            output.write(
                                ("HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: application/vnd.tracelens.project\r\n" +
                                    "Content-Disposition: attachment; filename=\"${archive.name}\"\r\n" +
                                    "Content-Length: ${archive.length()}\r\n" +
                                    "Connection: close\r\n\r\n").toByteArray()
                            )
                            archive.inputStream().use { it.copyTo(output) }
                        } else {
                            output.write("HTTP/1.1 404 Not Found\r\nConnection: close\r\n\r\n".toByteArray())
                        }
                        output.flush()
                    }
                }
            }
        }
    }

    override fun close() {
        running = false
        runCatching { server.close() }
    }

    private fun localAddress(): String =
        NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
            ?.hostAddress ?: error("Połącz telefon z siecią Wi‑Fi.")
}
