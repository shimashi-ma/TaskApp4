package jp.techacademy.kinugawa.mikako.taskapp4

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import io.realm.Realm
import jp.techacademy.kinugawa.mikako.taskapp.R
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class InputActivity : AppCompatActivity() {

    //タスクの日時用
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    //Taskクラスのオブジェクト
    private var mTask: Task? = null

    //日付ボタン
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    //時間ボタン
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    //決定ボタン
    private val mOnDoneClickListener = View.OnClickListener {
        //Realmに保存/更新
        addTask()
        //InputActivityを閉じて前の画面（MainActivity）に戻る
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // setSupportActionBarメソッドにより、ツールバーをActionBarとして使えるように設定
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            //setDisplayHomeAsUpEnabledメソッドで、ActionBarに戻るボタンを表示
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1) //getIntExtra()値を受け取る。EXTRA_TASK が設定されていないと taskId に-1を代入。
        val realm = Realm.getDefaultInstance()
        //askId のものが検索され、findFirst() によって最初に見つかったインスタンスが返され、 mTask へ代入。
        //taskId に -1 が入っている(タスクを新規作成するとき)と、検索に引っかからず、 mTask には null が代入。
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            // 新規作成の場合 現在の時刻を表示させる
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            category_edit_text.setText(mTask!!.category)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {

        //Realmオブジェクトを取得
        val realm = Realm.getDefaultInstance()

        //Realmでデータを追加、削除など変更を行う場合はbeginTransactionメソッドをまず呼び出す
        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            //保存されているタスクの中の最大のidの値に1を足してユニークなIDを設定
            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = category_edit_text.text.toString()

        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category = category

        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        //copyToRealmOrUpdate() 引数で与えたオブジェクトが存在していれば更新、なければ追加を行うメソッド
        realm.copyToRealmOrUpdate(mTask!!)

        //beginTransaction()とセットで使うメソッド
        realm.commitTransaction()

        realm.close()


        //スケジュールされた日時でブロードキャストするための、PendingIntentを作成
        //PendingIntentは、すぐに発行するのではなく特定のタイミングで後から発行させるIntent
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT  //既存のPendingIntentがあれば、それはそのままでextraのデータだけ置き換えるという指定
        )

        //指定した時間でアラーム
        //RTC_WAKEUPは「UTC時間を指定する。画面スリープ中でもアラームを発行する」
        //getSystemService メソッドはシステムレベルのサービスを取得するためのメソッド
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)

    }
}
