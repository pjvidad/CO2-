package com.example.co2_
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.co2_.databinding.LibraryLandingBinding
import com.example.co2_.databinding.SearchItemLayoutBinding
import com.google.android.material.imageview.ShapeableImageView

class BookFragment : Fragment() {

    private var _binding: LibraryLandingBinding? = null
    private val binding get() = _binding!!

    private lateinit var allBooks: List<BookItem>
    private lateinit var searchAdapter: SearchAdapter

    // --- Data Class for Book Items ---
    data class BookItem(val title: String, val imageResId: Int, val isLocked: Boolean)

    // --- RecyclerView Adapter for Search Results ---
    inner class SearchAdapter(
        private var books: List<BookItem>,
        private val clickListener: (BookItem) -> Unit
    ) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: SearchItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(book: BookItem) {
                binding.searchItemTitle.text = book.title
                binding.searchItemImage.setImageResource(book.imageResId)
                itemView.setOnClickListener { clickListener(book) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = SearchItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(books[position])
        }

        override fun getItemCount() = books.size

        fun updateData(newBooks: List<BookItem>) {
            this.books = newBooks
            notifyDataSetChanged()
        }
    }

    // --- Fragment Lifecycle Methods ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the master list of books
        allBooks = listOf(
            BookItem(getString(R.string.bookTitle0), R.drawable.thecarboncycle, false),
            BookItem(getString(R.string.bookTitle1), R.drawable.trashtravels, true),
            BookItem(getString(R.string.bookTitle2), R.drawable.co2forkids, true),
            BookItem(getString(R.string.bookTitle3), R.drawable.reducingco2f, true)
        )

        // Set up fragment result listeners
        parentFragmentManager.setFragmentResultListener("lesson_exit", this) { _, _ ->
            val currentScore = binding.libraryEnergy.text.toString().toIntOrNull() ?: 100
            val newScore = currentScore - 20
            binding.libraryEnergy.text = newScore.toString()
        }

        parentFragmentManager.setFragmentResultListener("lesson_complete", this) { _, _ ->
            binding.checkIcon0.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LibraryLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure the bottom navigation bar is visible
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.VISIBLE

        // --- Setup Search --- 
        setupSearch()

        // --- Initial UI State ---
        updateBookVisibility("")

        // --- Listeners for the main page buttons ---
        binding.bookTitle0.setOnClickListener {
            navigateToLesson()
        }
        val wipToast = View.OnClickListener { Toast.makeText(requireContext(), "Work In Progress.", Toast.LENGTH_SHORT).show() }
        binding.bookTitle1.setOnClickListener(wipToast)
        binding.bookTitle2.setOnClickListener(wipToast)
        binding.bookTitle3.setOnClickListener(wipToast)
    }

    private fun setupSearch() {
        binding.searchView.setupWithSearchBar(binding.searchBar)

        // Setup the adapter for the search results
        searchAdapter = SearchAdapter(allBooks) { book ->
            if (book.isLocked) {
                Toast.makeText(requireContext(), "Work In Progress.", Toast.LENGTH_SHORT).show()
            } else {
                navigateToLesson()
            }
            binding.searchView.hide()
        }

        // You need to add a RecyclerView inside your SearchView in the XML
        // For now, let's assume you have one with the id `search_recycler_view`
        val searchRecyclerView = binding.searchView.findViewById<RecyclerView>(R.id.search_recycler_view) // This is a temporary solution
        if (searchRecyclerView != null) {
          searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
          searchRecyclerView.adapter = searchAdapter
        }

        // Text listener for filtering
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                updateBookVisibility(query)

                // Filter and update the search results adapter
                val filteredBooks = allBooks.filter { it.title.lowercase().contains(query) }
                searchAdapter.updateData(filteredBooks)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateBookVisibility(query: String) {
        binding.linearLayout3.visibility = if (getString(R.string.bookTitle0).lowercase().contains(query)) View.VISIBLE else View.GONE
        binding.linearLayout4.visibility = if (getString(R.string.bookTitle1).lowercase().contains(query)) View.VISIBLE else View.GONE
        binding.linearLayout5.visibility = if (getString(R.string.bookTitle2).lowercase().contains(query)) View.VISIBLE else View.GONE
        binding.linearLayout6.visibility = if (getString(R.string.bookTitle3).lowercase().contains(query)) View.VISIBLE else View.GONE
    }

    private fun navigateToLesson() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, Lesson1Fragment())
            .addToBackStack("library_page")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
