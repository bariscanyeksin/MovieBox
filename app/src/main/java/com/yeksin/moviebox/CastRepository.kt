package com.yeksin.moviebox

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class CastRepository {
    private val gson = Gson()
    var movieId: Int? = null
    var seriesId: Int? = null

    // movieId'yi ayarlamak için bir yöntem
    fun setMovieId(id: Int) {
        movieId = id
    }

    fun setSeriesId(id: Int) {
        seriesId = id
    }

    fun fetchMoviesCast(callback: (CastResponse?) -> Unit) {
        // movieId'nin null olmadığını kontrol edin
        val id = movieId ?: run {
            callback(null)
            return
        }

        val request = ApiService.getRequestBuilder("movie/$id/credits?language=en-US").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                val castResponse = gson.fromJson(responseData, CastResponse::class.java)
                callback(castResponse)
            }
        })
    }

    fun fetchSeriesCast(callback: (CastResponse?) -> Unit) {
        // movieId'nin null olmadığını kontrol edin
        val id = seriesId ?: run {
            callback(null)
            return
        }

        val request = ApiService.getRequestBuilder("tv/$id/credits?language=en-US").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                val castResponse = gson.fromJson(responseData, CastResponse::class.java)
                callback(castResponse)
            }
        })
    }
}