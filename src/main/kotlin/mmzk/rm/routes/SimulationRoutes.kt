package mmzk.rm.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mmzk.rm.models.SimulateRequest
import mmzk.rm.models.SimulateResponse
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlin.io.path.writeText

fun Route.simulationRouting() {
    route("/simulate") {
        put {
            if (MMZKRM.path == null) {
                return@put call.respond(
                    status = HttpStatusCode.InternalServerError,
                    SimulateResponse(hasError = true, errors = listOf("Unsupported Server OS!"))
                )
            }
            try {
                val rm = try {
                    call.receive<SimulateRequest>()
                } catch (e: Exception) {
                    return@put call.respond(
                        status = HttpStatusCode.BadRequest,
                        SimulateResponse(hasError = true, errors = listOf("$e"))
                    )
                }

                val output = run {
                    val file = kotlin.io.path.createTempFile(suffix = ".mmzk")
                    file.writeText(rm.code)
                    var args = listOf("-j")
                    if (rm.startFromR0) {
                        args = args + listOf("-i")
                    }
                    if (rm.showSteps != null) {
                        args = args + listOf("-s${rm.showSteps}")
                    }
                    args = args + listOf(file.pathString) + rm.args
                    val output = try {
                        MMZKRM.run(args)
                    } catch (e: Exception) {
                        return@put call.respond(
                            status = HttpStatusCode.InternalServerError,
                            SimulateResponse(hasError = true, errors = listOf("Internal Error: $e"))
                        )
                    } ?: return@put call.respond(
                        status = HttpStatusCode.RequestTimeout,
                        SimulateResponse(hasError = true, errors = listOf("The request takes too long!"))
                    )
                    file.deleteIfExists()
                    output
                }

                call.respondText(
                    output,
                    status = if (Json.decodeFromString(
                            SimulateResponse.serializer(),
                            output
                        ).hasError
                    ) HttpStatusCode.BadRequest else HttpStatusCode.OK
                )
            } catch (e: Exception) {
                return@put call.respond(
                    status = HttpStatusCode.InternalServerError,
                    SimulateResponse(hasError = true, errors = listOf("$e"))
                )
            }
        }
    }
}
