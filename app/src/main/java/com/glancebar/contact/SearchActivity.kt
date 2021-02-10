package com.glancebar.contact

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.ui.contacts.ContactsAdapter
import com.glancebar.contact.utils.OnRecyclerReachBottomListener
import com.glancebar.contact.utils.notEmpty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {


    private lateinit var searchRecyclerView: RecyclerView
    private var contacts: MutableList<Contact> = mutableListOf()
    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
//        handleIntent(intent)
        title = getString(R.string.search_contact)
        val query = intent.getStringExtra(SearchManager.QUERY)

        initRecyclerView()

        if (query != null && notEmpty(query)) {
            loadContacts(query, query)
        } else {
            loadContacts()
        }
    }


    private fun loadContacts(name: String = "", number: String = "") {
        val nameQuery = "%$name%"
        val numberQuery = "%$number%"
        GlobalScope.launch {
            contactDao.findByNameLikeOrNumberLike(nameQuery, numberQuery).collect {
                contacts.addAll(it)
                searchRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun initRecyclerView() {
        searchRecyclerView = findViewById(R.id.search_activity_recycler)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = ContactsAdapter(contacts,
            object : OnRecyclerReachBottomListener {
                override fun onBottomReached(position: Int) {
                    // do nothing
                }
            })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onSearchRequested(): Boolean {
        return super.onSearchRequested()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater.inflate(R.menu.search_actionbar_menu, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu!!.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        val searchItem = menu.findItem(R.id.search)
        searchItem.expandActionView()
        val searchView = searchItem.actionView
//        searchView.hasExplicitFocusable()
//        searchView.isFocusable = true
//        searchView.requestFocusFromTouch()
        return super.onCreateOptionsMenu(menu)
    }

    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {
            contacts.clear()
            val query = intent.getStringExtra(SearchManager.QUERY)
            //use the query to search your data somehow
            // TODO: add something
        }
    }
}