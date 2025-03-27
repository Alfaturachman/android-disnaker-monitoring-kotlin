package com.example.disnakermonitoring.ui.kontributor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.disnakermonitoring.R
import com.example.disnakermonitoring.model.Media
import com.example.arsipsurat.api.RetrofitClient

class RiwayatMediaAdapter(private var mediaList: List<Media>) : RecyclerView.Adapter<RiwayatMediaAdapter.MediaViewHolder>() {

    // ViewHolder untuk Media
    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJudul: TextView = itemView.findViewById(R.id.tvJudul)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val ivMedia: ImageView = itemView.findViewById(R.id.ivMedia) // Tambahkan ImageView
    }

    // Update data adapter
    fun updateData(newMediaList: List<Media>) {
        mediaList = newMediaList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daftar_riwayat_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]

        holder.tvJudul.text = media.judul
        holder.tvKategori.text = "Kategori: ${media.id_kategori}"
        holder.tvStatus.text = "Status: ${media.status}"
        holder.tvTanggal.text = "Tanggal: ${media.tanggal}"

        // Load gambar dengan Glide
        Glide.with(holder.itemView.context)
            .load("${RetrofitClient.BASE_URL_UPLOADS}${media.gambar}")
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.ivMedia)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }
}
