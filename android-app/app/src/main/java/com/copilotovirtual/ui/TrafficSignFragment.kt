package com.copilotovirtual.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.copilotovirtual.databinding.FragmentTrafficSignBinding
import com.copilotovirtual.ui.viewmodel.CurrentSpeedViewModel
import com.copilotovirtual.ui.viewmodel.SpeedLimitViewModel
import com.copilotovirtual.ui.viewmodel.TrafficSignViewModel

class TrafficSignFragment : Fragment() {
    private var _binding: FragmentTrafficSignBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TrafficSignViewModel by activityViewModels() // Shared ViewModel
    private val speedLimitViewModel: SpeedLimitViewModel by activityViewModels() // Speed limit signs
    // private val currentSpeedViewModel: CurrentSpeedViewModel by activityViewModels() // Current speed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrafficSignBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel // Bind ViewModel to XML
        binding.speedLimitViewModel = speedLimitViewModel // Bind second ViewModel
        // binding.currentSpeedViewModel = currentSpeedViewModel // Bind third ViewModel

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
