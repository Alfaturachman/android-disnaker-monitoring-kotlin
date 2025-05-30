package com.example.disnakermonitoring.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.disnakermonitoring.databinding.FragmentDashboardBinding
import com.example.disnakermonitoring.ui.kontributor.RiwayatMediaActivity
import com.example.disnakermonitoring.ui.kontributor.tambah.TambahMediaActivity

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.cardViewTambahMedia.setOnClickListener {
            val intent = Intent(requireContext(), TambahMediaActivity::class.java)
            startActivity(intent)
        }

        binding.cardViewRiwayatMedia.setOnClickListener {
            val intent = Intent(requireContext(), RiwayatMediaActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}