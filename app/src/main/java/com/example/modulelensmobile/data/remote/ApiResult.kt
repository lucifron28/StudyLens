package com.example.modulelensmobile.data.remote

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

fun <Dto, Domain> Response<Dto>.toResult(
    label: String,
    map: (Dto) -> Domain
): Result<Domain> {
    if (!isSuccessful) {
        return Result.failure(Exception("$label failed (${code()}). Please try again."))
    }

    val body = body()
        ?: return Result.failure(Exception("$label failed: empty server response."))

    return Result.success(map(body))
}

fun <T> networkFailure(e: Exception): Result<T> {
    return Result.failure(Exception("Network error: ${e.message}"))
}
