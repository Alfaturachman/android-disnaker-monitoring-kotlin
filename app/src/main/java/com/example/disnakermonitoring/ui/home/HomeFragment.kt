package com.example.disnakermonitoring.ui.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.arsipsurat.api.ApiResponse
import com.example.arsipsurat.api.RetrofitClient
import com.example.disnakermonitoring.databinding.FragmentHomeBinding
import com.example.disnakermonitoring.model.StatsTotalData
import com.example.disnakermonitoring.model.TotalRekapData
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.toString

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
                kontributorStatsData(idUser)
            }
            "pemimpin" -> {
                fetchPemimpinTotalData(idUser)
                pemimpinStatsData(idUser)
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

    private fun kontributorStatsData(userId: Int) {
        // Buat request body
        val requestBody = HashMap<String, Int>()
        requestBody["id_user"] = userId

        // Panggil API
        val call = RetrofitClient.instance.StatsDataKontributor(requestBody)
        call.enqueue(object : Callback<ApiResponse<List<StatsTotalData>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<StatsTotalData>>>,
                response: Response<ApiResponse<List<StatsTotalData>>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    Log.d("API_RESPONSE", apiResponse.toString())

                    val stokDataList = apiResponse.data

                    // Struktur data
                    val dataDiproses = FloatArray(12) { 0f }
                    val dataDisetujui = FloatArray(12) { 0f }
                    val dataDitolak = FloatArray(12) { 0f }

                    // Spinner tahun
                    // val tahun = selectedYearFromDropdown

                    // Tahun sekarang
                    val calendar = Calendar.getInstance()
                    val tahun = calendar.get(Calendar.YEAR)

                    // Gabungkan data stok berdasarkan bulan untuk tahun yang diinginkan
                    for (stokData in stokDataList) {
                        if (stokData.tahun == tahun) { // Filter tahun
                            val bulanIndex = stokData.bulan - 1 // Konversi bulan (1-12) ke index (0-11)
                            if (bulanIndex in 0..11) {
                                dataDiproses[bulanIndex] += stokData.total_belum_disetujui.toFloat()
                                dataDisetujui[bulanIndex] += stokData.total_disetujui.toFloat()
                                dataDitolak[bulanIndex] += stokData.total_ditolak.toFloat()
                            }
                        }
                    }

                    // Buat data untuk BarChart
                    val entriesDiproses = mutableListOf<BarEntry>()
                    val entriesDisetujui = mutableListOf<BarEntry>()
                    val entriesDitolak = mutableListOf<BarEntry>()

                    for (i in 0 until 12) {
                        entriesDiproses.add(BarEntry(i.toFloat(), dataDiproses[i]))
                        entriesDisetujui.add(BarEntry(i.toFloat(), dataDisetujui[i]))
                        entriesDitolak.add(BarEntry(i.toFloat(), dataDitolak[i]))
                    }

                    // Set data set untuk BarChart
                    val dataSetDiproses = BarDataSet(entriesDiproses, "Diproses").apply {
                        color = ContextCompat.getColor(requireContext(), com.example.disnakermonitoring.R.color.badge_warning)
                        valueTextColor = Color.BLACK
                    }

                    val dataSetDisetujui = BarDataSet(entriesDisetujui, "Disetujui").apply {
                        color = ContextCompat.getColor(requireContext(), com.example.disnakermonitoring.R.color.badge_success)
                        valueTextColor = Color.BLACK
                    }

                    val dataSetDitolak = BarDataSet(entriesDitolak, "Ditolak").apply {
                        color = ContextCompat.getColor(requireContext(), com.example.disnakermonitoring.R.color.badge_danger)
                        valueTextColor = Color.BLACK
                    }

                    val intValueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }

                    dataSetDiproses.valueFormatter = intValueFormatter
                    dataSetDisetujui.valueFormatter = intValueFormatter
                    dataSetDitolak.valueFormatter = intValueFormatter

                    // Buat BarData untuk chart
                    val barData = BarData(dataSetDiproses, dataSetDisetujui, dataSetDitolak).apply {
                        // Atur lebar grup dan spacing
                        barWidth = 0.25f // Lebar masing-masing bar dalam grup
                        groupBars(0f, 0.4f, 0.1f) // (startX, groupSpace, barSpace)
                    }

                    // Set data ke chart
                    binding?.chartData?.apply {
                        data = barData
                        setVisibleXRangeMaximum(12f) // Menampilkan semua 12 bulan

                        // Konfigurasi sumbu X
                        val bulanLabels = arrayOf(
                            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
                        )
                        xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(bulanLabels)
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawGridLines(false)
                            axisMinimum = 0f
                            axisMaximum = 12f // Karena kita punya 12 bulan
                        }

                        // Konfigurasi sumbu Y
                        axisLeft.apply {
                            axisMinimum = 0f
                            granularity = 1f
                        }
                        axisRight.isEnabled = false

                        // Konfigurasi legenda
                        legend.apply {
                            isEnabled = true
                            textColor = Color.BLACK
                            verticalAlignment = Legend.LegendVerticalAlignment.TOP
                            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                            orientation = Legend.LegendOrientation.HORIZONTAL
                            setDrawInside(false)
                        }

                        // Animasi
                        animateY(1000)

                        // Atur viewport
                        setVisibleXRangeMaximum(12f)
                        moveViewToX(0f)

                        // Atur deskripsi
                        description.isEnabled = false

                        // Atur agar semua batang terlihat jelas
                        setDrawBarShadow(false)
                        setDrawValueAboveBar(true)
                        setPinchZoom(false)
                        setScaleEnabled(false)
                    }

                    // Menampilkan chart
                    binding?.chartData?.invalidate()
                } else {
                    Log.e("API_ERROR", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<StatsTotalData>>>, t: Throwable) {
                Log.e("API_FAILURE", "Request gagal: ${t.message}", t)
            }
        })
    }

    private fun fetchPemimpinTotalData(userId: Int) {
        val requestBody = hashMapOf("id_user" to userId)

        RetrofitClient.instance.TotalDataPemimpin(requestBody)
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

    private fun pemimpinStatsData(userId: Int) {
        // Buat request body
        val requestBody = HashMap<String, Int>()
        requestBody["id_user"] = userId

        // Panggil API
        val call = RetrofitClient.instance.StatsDataPemimpin(requestBody)
        call.enqueue(object : Callback<ApiResponse<List<StatsTotalData>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<StatsTotalData>>>,
                response: Response<ApiResponse<List<StatsTotalData>>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    Log.d("API_RESPONSE", apiResponse.toString())

                    val stokDataList = apiResponse.data

                    // Struktur data
                    val dataDiproses = FloatArray(12) { 0f }
                    val dataDisetujui = FloatArray(12) { 0f }
                    val dataDitolak = FloatArray(12) { 0f }

                    // Spinner tahun
                    // val tahun = selectedYearFromDropdown

                    // Tahun sekarang
                    val calendar = Calendar.getInstance()
                    val tahun = calendar.get(Calendar.YEAR)

                    // Gabungkan data stok berdasarkan bulan untuk tahun yang diinginkan
                    for (stokData in stokDataList) {
                        if (stokData.tahun == tahun) { // Filter tahun
                            val bulanIndex = stokData.bulan - 1 // Konversi bulan (1-12) ke index (0-11)
                            if (bulanIndex in 0..11) {
                                dataDiproses[bulanIndex] += stokData.total_belum_disetujui.toFloat()
                                dataDisetujui[bulanIndex] += stokData.total_disetujui.toFloat()
                                dataDitolak[bulanIndex] += stokData.total_ditolak.toFloat()
                            }
                        }
                    }

                    // Buat data untuk BarChart
                    val entriesDiproses = mutableListOf<BarEntry>()
                    val entriesDisetujui = mutableListOf<BarEntry>()
                    val entriesDitolak = mutableListOf<BarEntry>()

                    for (i in 0 until 12) {
                        entriesDiproses.add(BarEntry(i.toFloat(), dataDiproses[i]))
                        entriesDisetujui.add(BarEntry(i.toFloat(), dataDisetujui[i]))
                        entriesDitolak.add(BarEntry(i.toFloat(), dataDitolak[i]))
                    }

                    // Set data set untuk BarChart
                    val dataSetDiproses = BarDataSet(entriesDiproses, "Diproses").apply {
                        color = ContextCompat.getColor(requireContext(), com.example.disnakermonitoring.R.color.badge_warning)
                        valueTextColor = Color.BLACK
                    }

                    val dataSetDisetujui = BarDataSet(entriesDisetujui, "Disetujui").apply {
                        color = ContextCompat.getColor(requireContext(), com.example.disnakermonitoring.R.color.badge_success)
                        valueTextColor = Color.BLACK
                    }

                    val dataSetDitolak = BarDataSet(entriesDitolak, "Ditolak").apply {
                        color = ContextCompat.getColor(requireContext(), com.example.disnakermonitoring.R.color.badge_danger)
                        valueTextColor = Color.BLACK
                    }

                    val intValueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }

                    dataSetDiproses.valueFormatter = intValueFormatter
                    dataSetDisetujui.valueFormatter = intValueFormatter
                    dataSetDitolak.valueFormatter = intValueFormatter

                    // Buat BarData untuk chart
                    val barData = BarData(dataSetDiproses, dataSetDisetujui, dataSetDitolak).apply {
                        // Atur lebar grup dan spacing
                        barWidth = 0.25f // Lebar masing-masing bar dalam grup
                        groupBars(0f, 0.4f, 0.1f) // (startX, groupSpace, barSpace)
                    }

                    // Set data ke chart
                    binding?.chartData?.apply {
                        data = barData
                        setVisibleXRangeMaximum(12f) // Menampilkan semua 12 bulan

                        // Konfigurasi sumbu X
                        val bulanLabels = arrayOf(
                            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
                        )
                        xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(bulanLabels)
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawGridLines(false)
                            axisMinimum = 0f
                            axisMaximum = 12f // Karena kita punya 12 bulan
                        }

                        // Konfigurasi sumbu Y
                        axisLeft.apply {
                            axisMinimum = 0f
                            granularity = 1f
                        }
                        axisRight.isEnabled = false

                        // Konfigurasi legenda
                        legend.apply {
                            isEnabled = true
                            textColor = Color.BLACK
                            verticalAlignment = Legend.LegendVerticalAlignment.TOP
                            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                            orientation = Legend.LegendOrientation.HORIZONTAL
                            setDrawInside(false)
                        }

                        // Animasi
                        animateY(1000)

                        // Atur viewport
                        setVisibleXRangeMaximum(12f)
                        moveViewToX(0f)

                        // Atur deskripsi
                        description.isEnabled = false

                        // Atur agar semua batang terlihat jelas
                        setDrawBarShadow(false)
                        setDrawValueAboveBar(true)
                        setPinchZoom(false)
                        setScaleEnabled(false)
                    }

                    // Menampilkan chart
                    binding?.chartData?.invalidate()
                } else {
                    Log.e("API_ERROR", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<StatsTotalData>>>, t: Throwable) {
                Log.e("API_FAILURE", "Request gagal: ${t.message}", t)
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