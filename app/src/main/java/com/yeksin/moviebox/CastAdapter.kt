package com.yeksin.moviebox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class CastAdapter(
    private val castList: List<CastMember>,
    private val onItemClick: (CastMember) -> Unit
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = castList[position]
        holder.bind(cast)
        holder.itemView.setOnClickListener {
            onItemClick(cast)
        }
    }

    override fun getItemCount(): Int = castList.size

    inner class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val castImageView: ShapeableImageView = itemView.findViewById(R.id.castImageView)
        private val castNameTextView: TextView = itemView.findViewById(R.id.castNameActorTextView)
        private val castCharacterTextView: TextView = itemView.findViewById(R.id.castNameCharacterTextView)

        fun bind(cast: CastMember) {
            // Görselleri ve metinleri bağlayın
            Picasso.get().load("https://image.tmdb.org/t/p/w500${cast.profile_path}").into(castImageView)
            castNameTextView.text = cast.name.replace(" ", "\n")
            castCharacterTextView.text = if (cast.character.contains("/")) {
                cast.character.split("/")[0]
            } else {
                cast.character
            }
        }
    }
}
