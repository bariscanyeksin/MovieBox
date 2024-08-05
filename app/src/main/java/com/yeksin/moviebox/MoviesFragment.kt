package com.yeksin.moviebox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MoviesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().setTheme(R.style.AppTheme)

        return inflater.inflate(R.layout.fragment_movies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)

        // Toolbar ve geri butonunu ayarla
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val titleTextView: TextView = toolbar.findViewById(R.id.toolbar_title)
        titleTextView.text = "Movies"

        recyclerView = view.findViewById(R.id.recyclerViewMovies)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        movieAdapter = MovieAdapter(emptyList()) { movie ->
            val fragment = MovieDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("movie_id", movie.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = movieAdapter

        fetchMovies()
    }

    private fun fetchMovies() {
        val request = ApiService.getRequestBuilder("movie/popular?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error fetching movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body()?.string()
                    if (responseData != null) {
                        val gson = Gson()
                        val movieResponse = gson.fromJson(responseData, MovieResponse::class.java)
                        activity?.runOnUiThread {
                            movieAdapter = MovieAdapter(movieResponse.results) { movie ->
                                val fragment = MovieDetailFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("movie_id", movie.id)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            recyclerView.adapter = movieAdapter
                        }
                    }
                }
            }
        })
    }
}