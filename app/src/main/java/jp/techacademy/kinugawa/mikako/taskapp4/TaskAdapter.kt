package jp.techacademy.kinugawa.mikako.taskapp4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context): BaseAdapter() {

    //LayoutInflaterプロパティ　他のxmlリソースのViewを取り扱うための仕組み　
    private val mLayoutInflater: LayoutInflater

    //TaskクラスのList を taskList というプロパティで定義  MutableListは書き込み可能
    var taskList = mutableListOf<Task>()

    //コンストラクタを新規に追加して取得
    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    //アイテム（データ）の数を返す
    override fun getCount(): Int {
        return taskList.size
    }

    //アイテム（データ）を返す
    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    //アイテム（データ）のIDを返す
    override fun getItemId(position: Int): Long {
        return taskList[position].id.toLong()
    }

    //Viewを返す
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        //エルビス演算子?:は左辺がnullの時に右辺を返す
        //simple_list_item_2 タイトルとサブタイトルがあるセル
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        // 後でTaskクラスから情報を取得するように変更する
        textView1.text = taskList[position].title

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        textView2.text = simpleDateFormat.format(date)

        return view
    }
}