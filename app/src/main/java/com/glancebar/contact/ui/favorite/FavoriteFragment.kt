package com.glancebar.contact.ui.favorite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glancebar.contact.MainActivity
import com.glancebar.contact.R
import com.glancebar.contact.enums.NavigationDetailsEnum
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    companion object {
        fun newInstance() = FavoriteFragment()
    }

    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var viewModel: FavoriteViewModel
    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as MainActivity).showNavigator()
        val root = inflater.inflate(R.layout.favorite_fragment, container, false)
        viewModel = ViewModelProvider(this).get(FavoriteViewModel::class.java)

        initRecyclerView(root)
        if (viewModel.contacts.value!!.size == 0) {
            loadFavorites()
        }
        return root
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            contactDao.getFavorite().collect {
                it.forEach { contact ->
                    viewModel.contacts.value?.add(contact)
                }
                favoriteRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun initRecyclerView(root: View) {
        favoriteRecyclerView = root.findViewById(R.id.favorite_recycler)
        favoriteRecyclerView.layoutManager = GridLayoutManager(context, 2)
//        favoriteRecyclerView.layoutManager.height =
        favoriteRecyclerView.adapter = FavoriteRecyclerAdapter(viewModel.contacts.value!!)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavoriteViewModel::class.java)
        // TODO: Use the ViewModel
    }

}


class FavoriteRecyclerAdapter(
    val contacts: MutableList<Contact>
) : RecyclerView.Adapter<FavoriteRecyclerAdapter.ViewHolder>() {

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val favoriteCard: CardView = view.findViewById(R.id.favorite_card_item)
        private val avatarView: ImageView = view.findViewById(R.id.favorite_card_avatar)
        private val usernameTextView: TextView = view.findViewById(R.id.favorite_card_username)
        private val moreInfoView: ImageButton = view.findViewById(R.id.favorite_card_more_info)

        fun setData(contact: Contact) {
            if (contact.avatar != null) {
                Glide.with(view).load(contact.avatar).into(avatarView)
            }
            usernameTextView.text = contact.username
            moreInfoView.setOnClickListener {
                val actions =
                    FavoriteFragmentDirections.actionNavigationFavoriteToNavigationDetails(
                        contactNumber = contact.number!!,
                        sourceFragment = NavigationDetailsEnum.FAVORITE
                    )
                view.findNavController().navigate(actions)
            }
            favoriteCard.setOnClickListener {
                val uri = "tel:${contact.number}"
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
//                intent.data = Uri.parse(uri)
                view.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}