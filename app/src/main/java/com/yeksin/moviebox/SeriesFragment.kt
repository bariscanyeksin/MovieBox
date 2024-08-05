package com.yeksin.moviebox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class SeriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var seriesAdapter: SeriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().setTheme(R.style.AppTheme)

        return inflater.inflate(R.layout.fragment_series, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)

        // Toolbar ve geri butonunu ayarla
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val titleTextView: TextView = toolbar.findViewById(R.id.toolbar_title)
        titleTextView.text = "TV Series"

        recyclerView = view.findViewById(R.id.recyclerViewSeries)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        seriesAdapter = SeriesAdapter(emptyList()) { series ->
            val fragment = SeriesDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("series_id", series.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = seriesAdapter

        fetchSeries()
    }

    private fun fetchSeries() {
        val request = ApiService.getRequestBuilder("tv/popular?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error fetching series", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body()?.string()
                    if (responseData != null) {
                        val gson = Gson()
                        val seriesResponse = gson.fromJson(responseData, SeriesResponse::class.java)
                        activity?.runOnUiThread {
                            seriesAdapter = SeriesAdapter(seriesResponse.results) { series ->
                                val fragment = SeriesDetailFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("series_id", series.id)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            recyclerView.adapter = seriesAdapter
                        }
                    }
                }
            }
        })
    }
}