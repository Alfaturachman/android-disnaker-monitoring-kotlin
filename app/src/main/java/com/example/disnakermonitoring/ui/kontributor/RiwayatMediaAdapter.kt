package com.example.disnakermonitoring.ui.kontributor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.disnakermonitoring.R
import com.example.disnakermonitoring.model.Media
import com.example.arsipsurat.api.RetrofitClient
import com.example.disnakermonitoring.ui.kontributor.detail.DetailMediaActivity

class RiwayatMediaAdapter(
    private var mediaList: List<Media>,
    private val startForResult: ActivityResultLauncher<Intent>,
    ) : RecyclerView.Adapter<RiwayatMediaAdapter.MediaViewHolder>() {

    // ViewHolder untuk Media
    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJudul: TextView = itemView.findViewById(R.id.tvJudul)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val ivMedia: ImageView = itemView.findViewById(R.id.ivMedia)
        val cardView: View = itemView.findViewById(R.id.cardView)
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
        val context = holder.itemView.context

        holder.tvJudul.text = media.judul
        holder.tvKategori.text = "Kategori: ${getKategoriName(media.id_kategori)}"

        // Status
        holder.tvStatus.text = "Status: ${media.status}"
        val statusColor = when (media.status) {
            "belum disetujui" -> R.color.badge_warning
            "disetujui" -> R.color.badge_success
            "tolak" -> R.color.badge_danger
            else -> R.color.badge_secondary
        }
        holder.tvStatus.backgroundTintList = ContextCompat.getColorStateList(context, statusColor)
        holder.tvTanggal.text = "Tanggal: ${media.tanggal}"

        // Load gambar dengan Glide
        Glide.with(holder.itemView.context)
            .load("${RetrofitClient.BASE_URL_UPLOADS}${media.gambar}")
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.ivMedia)

        // Button CardView
        holder.cardView.setOnClickListener {
            val intent = Intent(context, DetailMediaActivity::class.java).apply {
                putExtra("id_media", media.id)
                putExtra("id_kategori", media.id_kategori)
                putExtra("nama", media.nama)
                putExtra("judul", media.judul)
                putExtra("url", media.url)
                putExtra("status", media.status)
                putExtra("tanggal", media.tanggal)
                putExtra("gambar", media.gambar)
                putExtra("deskripsi", media.deskripsi)
                putExtra("view", media.view)
            }
            startForResult.launch(intent)
        }

    }

    private fun getKategoriName(id: Int?): String {
        return when (id) {
            1 -> "Lowongan"
            2 -> "Pelatihan"
            3 -> "Bisnis"
            4 -> "UMKM"
            5 -> "Pabrik"
            6 -> "Buruh"
            7 -> "PHK"
            8 -> "Mediasi"
            else -> "Tidak Diketahui"
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }
}
