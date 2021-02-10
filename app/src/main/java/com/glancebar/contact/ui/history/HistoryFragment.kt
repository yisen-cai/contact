package com.glancebar.contact.ui.history

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glancebar.contact.R
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History
import com.glancebar.contact.persistence.repository.HistoryRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    // number contact
    private val contactsMap: MutableMap<String, Contact> = mutableMapOf()
    private val historyRepository = HistoryRepository(AppDatabase.INSTANCE!!)
    private val historyDao = AppDatabase.INSTANCE!!.getHistoryDao()
    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()
    private var syncCount = 0
    private var histories: MutableList<History> = mutableListOf()
    private lateinit var historyRecyclerView: RecyclerView

    var columns = arrayOf(
        CallLog.Calls.CACHED_NAME // 通话记录的联系人
        , CallLog.Calls.NUMBER // 通话记录的电话号码
        , CallLog.Calls.DATE // 通话记录的日期
        , CallLog.Calls.DURATION // 通话时长
        , CallLog.Calls.TYPE
    )
    var callUri: Uri = CallLog.Calls.CONTENT_URI

    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        val root = inflater.inflate(R.layout.history_fragment, container, false)
        initRecyclerView(root)
        loadData()
        return root
    }

    private fun initRecyclerView(root: View) {
        historyRecyclerView = root.findViewById(R.id.history_fragment_recycler)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        historyRecyclerView.adapter =
            HistoryAdapter(histories, contactsMap)
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            historyDao.findAllLimit(0, 100).collect {
                histories.clear()
                histories.addAll(it)
                historyRecyclerView.adapter?.notifyDataSetChanged()
            }
            contactDao.getAllContacts().collect {
                contactsMap.clear()
                it.forEach { contact ->
                    contactsMap[contact.number!!] = contact
                }
                historyRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.history_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sync_history_item -> {
                getDataList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDataList() {
        // 1.获得ContentResolver
        val resolver = requireActivity().contentResolver
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        }

        val cursor: Cursor? = resolver.query(
            callUri,  // 查询通话记录的URI
            columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER // 按照时间逆序排列，最近打的最先显示
        )

        while (cursor!!.moveToNext()) {
            val name: String? = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
            val number: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
            val dateLong: Long = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
            val duration: Int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))
            val type: Int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
            val isCall = type == CallLog.Calls.OUTGOING_TYPE
            val isMissedCall = type == CallLog.Calls.MISSED_TYPE
            val result = historyRepository.insert(
                History(
                    number = number,
                    last = duration,
                    isCall = isCall,
                    isMissedCall = isMissedCall,
                    createTime = dateLong
                )
            )
            syncCount += result
        }
        Toast.makeText(
            requireContext(),
            "${getString(R.string.synced_history)} $syncCount ${getString(R.string.sync_item)}",
            Toast.LENGTH_SHORT
        )
            .show()
        syncCount = 0
    }
}