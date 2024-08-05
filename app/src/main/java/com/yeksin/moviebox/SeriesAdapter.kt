package com.yeksin.moviebox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class SeriesAdapter(
    private val series: List<Series>,
    private val onItemClickListener: (Series) -> Unit
) : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    inner class SeriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)

        fun bind(series: Series) {
            titleTextView.text = series.original_name
            Picasso.get().load("https://image.tmdb.org/t/p/w500${series.poster_path}")
                .into(posterImageView)

            itemView.setOnClickListener {
                onItemClickListener(series)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_series, parent, false)
        return SeriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        holder.bind(series[position])
    }

    override fun getItemCount(): Int {
        return series.size
    }
}