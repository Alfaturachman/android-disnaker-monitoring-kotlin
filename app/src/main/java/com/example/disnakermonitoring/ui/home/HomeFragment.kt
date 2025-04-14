package com.example.disnakermonitoring.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.arsipsurat.api.ApiResponse
import com.example.arsipsurat.api.RetrofitClient
import com.example.disnakermonitoring.databinding.FragmentHomeBinding
import com.example.disnakermonitoring.model.TotalRekapData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var idUser: Int = -1
    private var idUserDetail: Int = -1
    private var userNama: String = "Tidak ada"
    private lateinit var tvNama: TextView
    private lateinit var tvMediaBelumDisetujui: TextView
    private lateinit var tvMediaDisetujui: TextView
    private lateinit var tvMediaDitolak: TextView

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tvNama = binding?.tvNama!!
        tvMediaBelumDisetujui = binding?.tvMediaBelumDisetujui!!
        tvMediaDisetujui = binding?.tvMediaDisetujui!!
        tvMediaDitolak = binding?.tvMediaDitolak!!

        idUser = getuserIdFromSharedPreferences()
        userNama = getNamaFromSharedPreferences().toString()
        tvNama.text = "Selamat Datang, $userNama"

        // Level dari SharedPreferences
        val level = getLevelFromSharedPreferences()
        when (level) {
            "kontributor" -> {
                fetchKontributorTotalData(idUser)
            }
            "pemimpin" -> {
                fetchKontributorTotalData(idUser)
            }
            else -> {
                Toast.makeText(requireContext(), "Level pengguna tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun fetchKontributorTotalData(userId: Int) {
        val requestBody = hashMapOf("id_user" to userId)

        RetrofitClient.instance.TotalDataKontributor(requestBody)
            .enqueue(object : Callback<ApiResponse<TotalRekapData>> {
                override fun onResponse(
                    call: Call<ApiResponse<TotalRekapData>>,
                    response: Response<ApiResponse<TotalRekapData>>
                ) {
                    Log.d("API_RESPONSE", "Response Code: ${response.code()}")

                    if (response.isSuccessful) {
                        val pelaporResponse = response.body()
                        Log.d("API_RESPONSE", "Response Body: $pelaporResponse")

                        if (pelaporResponse?.status == true) {
                            pelaporResponse.data?.let { pelapor ->
                                tvMediaBelumDisetujui.text = pelapor.belum_disetujui.toInt().toString()
                                tvMediaDisetujui.text = pelapor.disetujui.toInt().toString()
                                tvMediaDitolak.text = pelapor.tolak.toInt().toString()
                                Log.d("API_SUCCESS", "Data berhasil diperoleh: $pelapor")
                            }
                        } else {
                            Log.e("API_ERROR", "Pesan error dari server: ${pelaporResponse?.message}")
                            Toast.makeText(requireContext(), pelaporResponse?.message ?: "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Response Error Body: $errorBody")
                        Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<TotalRekapData>>, t: Throwable) {
                    Log.e("API_FAILURE", "Failure: ${t.message}", t)
                    Toast.makeText(requireContext(), "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getuserIdFromSharedPreferences(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id_user", -1)
    }

    private fun getuserIdDetailFromSharedPreferences(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id_user_detail", -1)
    }

    private fun getNamaFromSharedPreferences(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("nama", "0")
    }

    private fun getLevelFromSharedPreferences(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("level", "0")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}