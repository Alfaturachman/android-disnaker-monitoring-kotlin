package com.example.disnakermonitoring.model

data class StatsTotalData (
    val tahun: Int,
    val bulan: Int,
    val total_belum_disetujui: Int,
    val total_disetujui: Int,
    val total_ditolak: Int
)