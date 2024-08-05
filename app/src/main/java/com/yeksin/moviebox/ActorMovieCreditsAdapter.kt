package com.yeksin.moviebox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ActorMovieCreditsAdapter(
    private val movieCredits: List<ActorMovieCredit>,
    private val onItemClickListener: (ActorMovieCredit) -> Unit
) : RecyclerView.Adapter<ActorMovieCreditsAdapter.MovieCreditViewHolder>() {

    inner class MovieCreditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)

        fun bind(movieCredit: ActorMovieCredit) {
            titleTextView.text = movieCredit.title
            Picasso.get().load("https://image.tmdb.org/t/p/w500${movieCredit.poster_path}")
                .into(posterImageView)

            itemView.setOnClickListener {
                onItemClickListener(movieCredit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieCreditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor_movie_credit, parent, false)
        return MovieCreditViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieCreditViewHolder, position: Int) {
        holder.bind(movieCredits[position])
    }

    override fun getItemCount(): Int {
        return movieCredits.size
    }
}