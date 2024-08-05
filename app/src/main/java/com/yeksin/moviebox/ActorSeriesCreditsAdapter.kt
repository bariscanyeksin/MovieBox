package com.yeksin.moviebox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ActorSeriesCreditsAdapter(
    private val seriesCredits: List<ActorSeriesCredit>,
    private val onItemClickListener: (ActorSeriesCredit) -> Unit
) : RecyclerView.Adapter<ActorSeriesCreditsAdapter.SeriesCreditViewHolder>() {

    inner class SeriesCreditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)

        fun bind(seriesCredit: ActorSeriesCredit) {
            titleTextView.text = seriesCredit.name
            val posterUrl = "https://image.tmdb.org/t/p/w500${seriesCredit.poster_path}"
            Picasso.get().load(posterUrl).into(posterImageView)

            itemView.setOnClickListener {
                onItemClickListener(seriesCredit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesCreditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor_movie_credit, parent, false)
        return SeriesCreditViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeriesCreditViewHolder, position: Int) {
        holder.bind(seriesCredits[position])
    }

    override fun getItemCount(): Int {
        return seriesCredits.size
    }
}