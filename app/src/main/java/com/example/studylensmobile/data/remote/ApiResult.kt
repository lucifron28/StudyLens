package com.example.studylensmobile.data.remote

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import retrofit2.Response

suspend fun <Dto, Domain> apiResult(
    label: String,
    call: suspend () -> Response<Dto>,
    map: (Dto) -> Domain
): Result<Domain> {
    return try {
        call().toResult(label, map)
    } catch (e: Exception) {
        networkFailure(e)
    }
}

suspend fun emptyApiResult(
    label: String,
    call: suspend () -> Response<Unit>
): Result<Unit> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            response.toFailure(label)
        }
    } catch (e: Exception) {
        networkFailure(e)
    }
}

fun <Dto, Domain> Response<Dto>.toResult(
    label: String,
    map: (Dto) -> Domain
): Result<Domain> {
    if (!isSuccessful) {
        return toFailure(label)
    }

    val body = body()
        ?: return Result.failure(Exception("$label failed: empty server response."))

    return Result.success(map(body))
}

fun <T> networkFailure(e: Exception): Result<T> {
    return Result.failure(Exception("Network error: ${e.message}"))
}

private fun <T> Response<*>.toFailure(label: String): Result<T> {
    val message = errorBody()
        ?.string()
        ?.toApiErrorMessage()
        ?: "$label failed (${code()}). Please try again."
    return Result.failure(Exception(message))
}

private fun String.toApiErrorMessage(): String? {
    val rawMessage = trim()
    if (rawMessage.isBlank()) return null

    return runCatching {
        JsonParser.parseString(rawMessage).toApiErrorMessage()
    }.getOrNull() ?: rawMessage
}

private fun JsonElement.toApiErrorMessage(): String? {
    return when {
        isJsonPrimitive -> asString.takeIf { it.isNotBlank() }
        isJsonArray -> asJsonArray
            .mapNotNull { it.toApiErrorMessage() }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
        isJsonObject -> {
            asJsonObject.get("detail")?.toApiErrorMessage()
                ?: asJsonObject.entrySet()
                    .mapNotNull { (field, value) ->
                        value.toApiErrorMessage()?.let { "$field: $it" }
                    }
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
        }
        else -> null
    }
}
