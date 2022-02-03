package com.example.rocketreserver

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.api.json.writeAny
import com.apollographql.apollo3.network.okHttpClient
import com.example.rocketreserver.type.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

private var instance: ApolloClient? = null

fun apolloClient(context: Context): ApolloClient {
    if (instance != null) {
        return instance!!
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    instance = ApolloClient.Builder()
        .serverUrl("https://graphql.anilist.co")
        .okHttpClient(okHttpClient)
        .addCustomScalarAdapter(Json.type, JsonAdapter)
        .build()

    return instance!!
}

private object JsonAdapter : Adapter<Any> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Any {
        return reader.readAny()!!
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: Any
    ) {
        writer.writeAny(value)
    }

    // Copied from https://github.com/apollographql/apollo-kotlin/blob/v3.0.0/apollo-api/src/commonMain/kotlin/com/apollographql/apollo3/api/json/JsonReaders.kt#L25
    private fun JsonReader.readAny(): Any? {
        return when (val token = peek()) {
            JsonReader.Token.NULL -> nextNull()
            JsonReader.Token.BOOLEAN -> nextBoolean()
            JsonReader.Token.LONG, JsonReader.Token.NUMBER -> guessNumber()
            JsonReader.Token.STRING -> nextString()
            JsonReader.Token.BEGIN_OBJECT -> {
                beginObject()
                val result = mutableMapOf<String, Any?>()
                while (hasNext()) {
                    result.put(nextName(), readAny())
                }
                endObject()
                result
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                beginArray()
                val result = mutableListOf<Any?>()
                while (hasNext()) {
                    result.add(readAny())
                }
                endArray()
                result
            }
            else -> error("unknown token $token")
        }
    }

    private fun JsonReader.guessNumber(): Any {
        val jsonNumber = nextNumber()
        try {
            return jsonNumber.value.toInt()
        } catch (_: Exception) {
        }
        try {
            return jsonNumber.value.toLong()
        } catch (_: Exception) {
        }
        try {
            return jsonNumber.value.toDouble()
        } catch (_: Exception) {
        }
        return jsonNumber
    }
}
