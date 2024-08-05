package com.yeksin.moviebox

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException

class MovieDetailFragment : Fragment() {

    private var movieId: Int? = null
    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Durum çubuğunu saydam yapmak için aşağıdaki kodu ekleyin
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        requireActivity().window.statusBarColor = android.graphics.Color.TRANSPARENT
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val backButton: ImageButton = toolbar.findViewById(R.id.toolbar_back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val imageView: ShapeableImageView = view.findViewById(R.id.backdropImageView)

        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(0f)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 96f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 96f)
            .build()

        imageView.shapeAppearanceModel = shapeAppearanceModel

        movieId = arguments?.getInt("movie_id") ?: return
        movieId?.let {
            fetchMovieDetail(it)
            fetchCast(it)
        }
    }

    private fun fetchMovieDetail(movieId: Int) {
        val request = ApiService.getRequestBuilder("movie/$movieId?language=en-US&append_to_response=credits").build()

        ApiService.getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error fetching movie details", Toast.LENGTH_SHORT).show()
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
                            val movieDetail = gson.fromJson(responseData, MovieDetail::class.java)
                            requireActivity().runOnUiThread {
                                updateUI(movieDetail)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Error parsing movie details", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun fetchCast(movieId: Int) {
        val castRepository = CastRepository()
        castRepository.movieId = movieId
        castRepository.fetchMoviesCast { castResponse ->
            castResponse?.let {
                requireActivity().runOnUiThread {
                    updateCastUI(it)
                }
            } ?: run {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error fetching cast details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(movieDetail: MovieDetail) {
        view?.let { rootView ->
            val titleTextView: TextView = rootView.findViewById(R.id.titleTextView)
            val posterImageView: ImageView = rootView.findViewById(R.id.posterImageView)
            val backdropImageView: ImageView = rootView.findViewById(R.id.backdropImageView)
            val releaseDateTextView: TextView = rootView.findViewById(R.id.releaseDateTextView)
            val runtimeTextView: TextView = rootView.findViewById(R.id.runtimeTextView)
            val taglineTextView: TextView = rootView.findViewById(R.id.taglineTextView)
            val genresTextView: TextView = rootView.findViewById(R.id.genresTextView)
            val titleCast: TextView = rootView.findViewById(R.id.titleCast)
            val overviewTextView: TextView = rootView.findViewById(R.id.overviewTextView)
            val fullOverviewText = movieDetail.overview
            val truncatedOverviewText = truncateText(fullOverviewText, 3)

            overviewTextView.text = truncatedOverviewText
            isExpanded = false

            // Tıklama ile genişleyip daralma işlevi
            overviewTextView.setOnClickListener {
                isExpanded = !isExpanded
                overviewTextView.text = if (isExpanded) {
                    overviewTextView.maxLines = Integer.MAX_VALUE // Remove max lines limitation
                    fullOverviewText
                } else {
                    overviewTextView.maxLines = 3 // Reset max lines for truncated text
                    truncatedOverviewText
                }
                overviewTextView.invalidate() // Force the view to redraw
            }

            // Populate other fields
            titleTextView.text = movieDetail.title
            releaseDateTextView.text = movieDetail.release_date.split("-")[0]
            runtimeTextView.text = "${movieDetail.runtime} min"
            if (movieDetail.tagline.isNullOrEmpty()) {taglineTextView.visibility = View.GONE} else {taglineTextView.text = movieDetail.tagline}
            genresTextView.text = movieDetail.genres.joinToString(", ") {it.name}
            titleCast.text = "Cast"

            // Load images
            Picasso.get().load("https://image.tmdb.org/t/p/w500${movieDetail.poster_path}").into(posterImageView)
            Picasso.get().load("https://image.tmdb.org/t/p/w1280${movieDetail.backdrop_path}").into(backdropImageView)
        }
    }

    private fun updateCastUI(castResponse: CastResponse) {
        view?.let { rootView ->
            val recyclerView: RecyclerView = rootView.findViewById(R.id.castRecyclerView)

            // LayoutManager'ı ayarla
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager

            // Adapter'ı oluştur ve RecyclerView'a ata
            val castList = castResponse.cast
            val adapter = CastAdapter(castList) { castMember ->
                // Tıklanan oyuncunun sayfasına git
                val fragment = ActorDetailFragment.newInstance(castMember.id)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            recyclerView.adapter = adapter
        }
    }

    private fun truncateText(text: String, maxLines: Int): String {
        val lines = text.split("\n")
        return if (lines.size > maxLines) {
            lines.subList(0, maxLines).joinToString("\n") + "..."
        } else {
            text
        }
    }
}