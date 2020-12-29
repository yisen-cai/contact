package com.glancebar.contact.ui.history

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.glancebar.contact.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    var columns = arrayOf(
        CallLog.Calls.CACHED_NAME // 通话记录的联系人
        , CallLog.Calls.NUMBER // 通话记录的电话号码
        , CallLog.Calls.DATE // 通话记录的日期
        , CallLog.Calls.DURATION // 通话时长
        , CallLog.Calls.TYPE
    )
    var callUri: Uri = CallLog.Calls.CONTENT_URI

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

        val textView: TextView = root.findViewById(R.id.today_history_label)
        textView.setOnClickListener {
            findNavController().navigate(R.id.history_navigate_to_contacts)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataList()
    }


    private fun getDataList(): List<Map<String, String>>? {
        // 1.获得ContentResolver
        val resolver = requireActivity().contentResolver
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        }
        // 2.利用ContentResolver的query方法查询通话记录数据库
        /**
         * @param uri 需要查询的URI，（这个URI是ContentProvider提供的）
         * @param projection 需要查询的字段
         * @param selection sql语句where之后的语句
         * @param selectionArgs ?占位符代表的数据
         * @param sortOrder 排序方式
         */
        val cursor: Cursor? = resolver.query(
            callUri,  // 查询通话记录的URI
            columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER // 按照时间逆序排列，最近打的最先显示
        )
        // 3.通过Cursor获得数据
        val list: MutableList<Map<String, String>> = ArrayList()
        while (cursor!!.moveToNext()) {
            val name: String? = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
            val number: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
            val dateLong: Long = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
            val date: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateLong))
            val time: String = SimpleDateFormat("HH:mm").format(Date(dateLong))
            val duration: Int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))
            val type: Int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
            val dayCurrent: String = SimpleDateFormat("dd").format(Date())
            val dayRecord: String = SimpleDateFormat("dd").format(Date(dateLong))
            var typeString = ""
            when (type) {
                CallLog.Calls.INCOMING_TYPE ->                     //"打入"
                    typeString = "打入"
                CallLog.Calls.OUTGOING_TYPE ->                     //"打出"
                    typeString = "打出"
                CallLog.Calls.MISSED_TYPE ->                     //"未接"
                    typeString = "未接"
                else -> {
                }
            }
            Log.i("TAG", "getDataList: $number, $name, $date, $duration, $typeString")
//            if (MobileUtil.isMobileNO(number)) {
//                var dayString = ""
//                dayString = if (dayCurrent.toInt() == dayRecord.toInt()) {
//                    //今天
//                    "今天"
//                } else if (dayCurrent.toInt() - 1 == dayRecord.toInt()) {
//                    //昨天
//                    "昨天"
//                } else {
//                    //前天
//                    "前天"
//                }
//                val day_lead: Long = TimeStampUtil.compareDayTime(date)
//                if (day_lead < 2) { //只显示48小时以内通话记录，防止通     //话记录数据过多影响加载速度
//                    val map: MutableMap<String, String> = HashMap()
//                    //"未备注联系人"
//                    map["name"] = name ?: "未备注联系人" //姓名
//                    map["number"] = number //手机号
//                    map["date"] = date //通话日期
//                    // "分钟"
//                    map["duration"] = (duration / 60).toString() + "分钟" //时长
//                    map["type"] = typeString //类型
//                    map["time"] = time //通话时间
//                    map["day"] = dayString //
//                    map["time_lead"] = TimeStampUtil.compareTime(date) //
//                    list.add(map)
//                } else {
//                    return list
//                }
//            }
        }
        return list
    }


}