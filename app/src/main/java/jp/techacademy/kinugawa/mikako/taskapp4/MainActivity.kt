package jp.techacademy.kinugawa.mikako.taskapp4

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import io.realm.RealmQuery
import jp.techacademy.kinugawa.mikako.taskapp.R

//パッケージ名を含めた文字列をIntentのExtraのキーとして利用するのは、他のアプリのExtraと間違えないようにするため。
const val EXTRA_TASK = "jp.techacademy.kinugawa.mikako.taskapp.TASK"

class MainActivity : AppCompatActivity() {

    //Realmクラスを保持するmRealmを定義
    private  lateinit var mRealm: Realm
    //Realmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        //更新して変更している？
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    //TaskAdapterクラスを保持するプロパティを定義
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //カテゴリ検索
        Button1.setOnClickListener {

            val seachWord = EditText.text.toString()


            if (seachWord == "") {


            } else if (seachWord != ""){

                var categoryWord = mRealm.where(Task::class.java).equalTo("category",seachWord).findAll()
                Log.d("LOG", categoryWord.toString())

            }
        }



        //FloatingActionButton
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()  //オブジェクトを取得
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id) //putExtra()値を渡す
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                //タスクを削除するタイミングでアラームを解除
                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        //ListViewの更新
        reloadListView()
    }

    //reloadListView() ListViewの更新
    private fun reloadListView() {

        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、mRealm.copyFromRealm(taskRealmResults) でコピーして、アダプタのTaskListにセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        //notifyDataSetChangedメソッド データが変わったことを伝えてリストを再描画
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    //onDestroyメソッド Activityが破棄されるときに呼び出される
    override fun onDestroy() {
        super.onDestroy()
        //getDefaultInstanceメソッドで取得したRealmクラスのオブジェクトはcloseメソッドで終了させる必要がある
        mRealm.close()
    }


}

