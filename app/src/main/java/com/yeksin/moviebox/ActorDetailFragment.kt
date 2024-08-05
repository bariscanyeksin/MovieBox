package com.yeksin.moviebox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException

class ActorDetailFragment : Fragment() {

    private lateinit var recyclerViewActorMovies: RecyclerView
    private lateinit var recyclerViewActorSeries: RecyclerView
    private lateinit var actorMoviesAdapter: ActorMovieCreditsAdapter
    private lateinit var actorSeriesAdapter: ActorSeriesCreditsAdapter

    companion object {
        private const val ARG_ACTOR_ID = "actor_id"

        fun newInstance(actorId: Int): ActorDetailFragment {
            val fragment = ActorDetailFragment()
            val args = Bundle()
            args.putInt(ARG_ACTOR_ID, actorId)
            fragment.arguments = args
            return fragment
        }
    }

    private var actorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            actorId = it.getInt(ARG_ACTOR_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().setTheme(R.style.AppTheme)
        return inflater.inflate(R.layout.fragment_actor_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)

        // Toolbar ve geri butonunu ayarla
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val backButton: ImageButton = toolbar.findViewById(R.id.toolbar_back_button)

        recyclerViewActorMovies = view.findViewById(R.id.recyclerViewActorMovies)
        recyclerViewActorMovies.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorMoviesAdapter = ActorMovieCreditsAdapter(emptyList()) { movieCredit ->
            val fragment = MovieDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("movie_id", movieCredit.id) // veya ilgili başka bir veri
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerViewActorMovies.adapter = actorMoviesAdapter

        recyclerViewActorSeries = view.findViewById(R.id.recyclerViewActorSeries)
        recyclerViewActorSeries.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorSeriesAdapter = ActorSeriesCreditsAdapter(emptyList()) { seriesCredit ->
            val fragment = SeriesDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("series_id", seriesCredit.id) // veya ilgili başka bir veri
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerViewActorSeries.adapter = actorSeriesAdapter

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        actorId?.let { id ->
            // Burada API çağrısını başlatabilir ve gelen verileri UI'ya bağlayabilirsiniz
            fetchActorDetails(id)
        }
        fetchActorMovieCredits()
        fetchActorSeriesCredits()
    }

    private fun fetchActorDetails(actorId: Int) {
        val request = ApiService.getRequestBuilder("person/$actorId?language=en-US").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error fetching actor details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Unexpected response code $response", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val responseData = response.body()?.string()
                    if (responseData != null) {
                        try {
                            val gson = Gson()
                            val actorDetails = gson.fromJson(responseData, ActorDetails::class.java)
                            requireActivity().runOnUiThread {
                                val titleTextView: TextView = view?.findViewById(R.id.toolbar_title) ?: return@runOnUiThread
                                titleTextView.text = actorDetails.name // Aktörün ismini burada günceller
                                updateUI(actorDetails)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Error parsing actor details", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun fetchActorMovieCredits() {
        val actorId = arguments?.getInt("actor_id") ?: return
        val request = ApiService.getRequestBuilder("person/$actorId/movie_credits?language=en-US").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error fetching actor's movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body()?.string()
                    if (responseData != null) {
                        val gson = Gson()
                        val actorCreditsResponse = gson.fromJson(responseData, ActorMovieCreditsResponse::class.java)

                        // Filmleri popülerliğine göre sıralayın
                        val sortedMovies = actorCreditsResponse.cast.sortedByDescending { it.popularity }

                        activity?.runOnUiThread {
                            actorMoviesAdapter = ActorMovieCreditsAdapter(sortedMovies) { movieCredit ->
                                val fragment = MovieDetailFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("movie_id", movieCredit.id)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            recyclerViewActorMovies.adapter = actorMoviesAdapter
                        }
                    }
                }
            }
        })
    }

    private fun fetchActorSeriesCredits() {
        val actorId = arguments?.getInt("actor_id") ?: return
        val request = ApiService.getRequestBuilder("person/$actorId/tv_credits?language=en-US").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error fetching actor's series", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body()?.string()
                    if (responseData != null) {
                        val gson = Gson()
                        val actorSeriesResponse = gson.fromJson(responseData, ActorSeriesCreditsResponse::class.java)

                        // Dizileri popülerliğine göre sıralayın
                        val sortedSeries = actorSeriesResponse.cast.sortedByDescending { it.popularity }

                        activity?.runOnUiThread {
                            actorSeriesAdapter = ActorSeriesCreditsAdapter(sortedSeries) { seriesCredit ->
                                val fragment = SeriesDetailFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("series_id", seriesCredit.id)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            recyclerViewActorSeries.adapter = actorSeriesAdapter
                        }
                    }
                }
            }
        })
    }


    private fun updateUI(actorDetails: ActorDetails) {
        view?.let { rootView ->
            val actorNameTextView: TextView = rootView.findViewById(R.id.actorNameTextView)
            val actorImageView: ImageView = rootView.findViewById(R.id.actorImageView)
            val actorBiographyTextView: TextView = rootView.findViewById(R.id.actorBiographyTextView)

            val fullBiographyText = actorDetails.biography
            val truncatedBiographyText = truncateText(fullBiographyText, 3)

            actorBiographyTextView.text = truncatedBiographyText
            var isExpanded = false

            // Tıklama ile genişleyip daralma işlevi
            actorBiographyTextView.setOnClickListener {
                isExpanded = !isExpanded
                actorBiographyTextView.text = if (isExpanded) {
                    actorBiographyTextView.maxLines = Integer.MAX_VALUE // Remove max lines limitation
                    fullBiographyText
                } else {
                    actorBiographyTextView.maxLines = 3 // Reset max lines for truncated text
                    truncatedBiographyText
                }
                actorBiographyTextView.invalidate() // Force the view to redraw
            }

            // Populate other fields
            actorNameTextView.text = actorDetails.name

            // Load images
            Picasso.get().load("https://image.tmdb.org/t/p/w500${actorDetails.profile_path}").into(actorImageView)
        }
    }

    private fun truncateText(text: String, maxLines: Int): String {
        val lines = text.split("\n")
        return if (lines.size <= maxLines) {
            text
        } else {
            lines.take(maxLines).joinToString("\n") + "..."
        }
    }
}