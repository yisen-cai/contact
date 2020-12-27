package com.glancebar.contact.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.glancebar.contact.R

class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        val root = inflater.inflate(R.layout.history_fragment, container, false)
//        val textView: TextView = root.findViewById(R.id.text)
//        viewModel.text.observe(viewLifecycleOwner, {
//            textView.text = it
//        })
        return root
    }

}