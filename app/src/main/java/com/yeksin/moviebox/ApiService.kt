package com.yeksin.moviebox

import okhttp3.OkHttpClient
import okhttp3.Request

object ApiService {

    private var API_KEY: String? = null
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val client = OkHttpClient()

    fun updateApiKey(newApiKey: String?) {
        API_KEY = newApiKey
    }

    fun getRequestBuilder(endpoint: String): Request.Builder {
        return Request.Builder()
            .url(BASE_URL + endpoint)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $API_KEY")
    }

    fun getClient(): OkHttpClient {
        return client
    }
}

// Movie.kt
data class Movie(
    val id: Int,
    val title: String,
    val poster_path: String
)

// MovieResponse.kt
data class MovieResponse(
    val results: List<Movie>
)

// Series.kt
data class Series(
    val id: Int,
    val original_name: String,
    val poster_path: String
)

// MovieResponse.kt
data class SeriesResponse(
    val results: List<Series>
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val original_title: String,
    val overview: String,
    val release_date: String,
    val runtime: Int,
    val genres: List<Genre>,
    val tagline: String,
    val poster_path: String,
    val backdrop_path: String,
    val budget: Int,
    val revenue: Int,
    val homepage: String,
    val vote_average: Double,
    val vote_count: Int,
    val spoken_languages: List<SpokenLanguage>,
    val production_companies: List<ProductionCompany>,
    val belongs_to_collection: Collection?
)

data class SeriesDetail(
    val id: Int,
    val original_name: String,
    val original_title: String,
    val overview: String,
    val first_air_date: String,
    val runtime: Int,
    val genres: List<Genre>,
    val tagline: String,
    val poster_path: String,
    val backdrop_path: String,
    val budget: Int,
    val revenue: Int,
    val homepage: String,
    val vote_average: Double,
    val vote_count: Int,
    val spoken_languages: List<SpokenLanguage>,
    val production_companies: List<ProductionCompany>,
    val belongs_to_collection: Collection?
)

data class Genre(
    val id: Int,
    val name: String
)

data class SpokenLanguage(
    val english_name: String,
    val iso_639_1: String,
    val name: String
)

data class ProductionCompany(
    val id: Int,
    val logo_path: String?,
    val name: String,
    val origin_country: String
)

data class Collection(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?
)

// CastMember.kt
data class CastMember(
    val profile_path: String,
    val name: String,
    val character: String,
    val id: Int
)

// CastResponse.kt
data class CastResponse(
    val cast: List<CastMember>
)

data class ActorDetails(
    val adult: Boolean,
    val also_known_as: List<String>,
    val biography: String,
    val birthday: String?,
    val deathday: String?,
    val gender: Int,
    val homepage: String?,
    val id: Int,
    val imdb_id: String,
    val known_for_department: String,
    val name: String,
    val place_of_birth: String?,
    val popularity: Double,
    val profile_path: String?
)

data class ActorMovieCredit(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int,
    val character: String,
    val credit_id: String,
    val order: Int
)

data class ActorMovieCreditsResponse(
    val cast: List<ActorMovieCredit>
)

data class ActorSeriesCredit(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>,
    val id: Int,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val first_air_date: String,
    val name: String,
    val vote_average: Double,
    val vote_count: Int,
    val character: String,
    val credit_id: String,
    val episode_count: Int
)

data class ActorSeriesCreditsResponse(
    val cast: List<ActorSeriesCredit>
)