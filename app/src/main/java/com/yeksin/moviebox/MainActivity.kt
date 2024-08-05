package com.yeksin.moviebox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val moviesFragment = MoviesFragment()
    private val seriesFragment = SeriesFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Durum çubuğunu saydam yapmak için aşağıdaki kodu ekleyin
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val fabRandom = findViewById<FloatingActionButton>(R.id.fab_button)

        if (savedInstanceState == null) {
            // Load default fragment
            loadFragment(moviesFragment)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_movies -> loadFragment(moviesFragment)
                R.id.navigation_series -> loadFragment(seriesFragment)
            }
            true
        }

        fabRandom.setOnClickListener {
            // Rastgele film veya dizi seçimi yap
            fetchRandomMovieOrTvShow()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun fetchRandomMovieOrTvShow() {

        val movieUrl = "movie/top_rated?language=en-US&page=1"
        val tvUrl = "tv/top_rated?language=en-US&page=1"

        // Film ve dizi verilerini paralel olarak çekiyoruz
        val movieRequest = ApiService.getRequestBuilder(movieUrl).build()
        val tvRequest = ApiService.getRequestBuilder(tvUrl).build()

        ApiService.getClient().newCall(movieRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body()?.string()
                val movieList = JSONObject(responseData).getJSONArray("results")
                val randomMovie = movieList.getJSONObject(Random.nextInt(movieList.length()))
                val movieId = randomMovie.getInt("id")

                // Diğer API çağrısı için
                ApiService.getClient().newCall(tvRequest).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val responseData = response.body()?.string()
                        val tvList = JSONObject(responseData).getJSONArray("results")
                        val randomTvShow = tvList.getJSONObject(Random.nextInt(tvList.length()))
                        val tvShowId = randomTvShow.getInt("id")

                        // Rastgele seçim yapalım (Film veya Dizi)
                        val isMovie = Random.nextBoolean() // Rastgele olarak film veya dizi seç
                        val idToUse = if (isMovie) movieId else tvShowId
                        val fragment = if (isMovie) {
                            MovieDetailFragment().apply {
                                arguments = Bundle().apply {
                                    putInt("movie_id", idToUse)
                                }
                            }
                        } else {
                            SeriesDetailFragment().apply {
                                arguments = Bundle().apply {
                                    putInt("series_id", idToUse)
                                }
                            }
                        }
                        runOnUiThread {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                })
            }
        })
    }
}