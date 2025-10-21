package com.example.co2_
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.LibraryLandingBinding
import com.example.co2_.Lesson1Fragment

class BookFragment : Fragment() {

    private var _binding: LibraryLandingBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LibraryLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Lesson 1
        binding.bookTitle0.setOnClickListener {
            val lesson1Fragment = Lesson1Fragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, lesson1Fragment)
                .addToBackStack(null) // Allows user to go back
                .commit()
        }

        // Show WIP Toast for other buttons
        val wipToast = View.OnClickListener {
            Toast.makeText(requireContext(), "Work In Progress.", Toast.LENGTH_SHORT).show()
        }

        binding.bookTitle1.setOnClickListener(wipToast)
        binding.bookTitle2.setOnClickListener(wipToast)
        binding.bookTitle3.setOnClickListener(wipToast)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
