package jp.co.fuyouj.attendapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class UserActivity : AppCompatActivity()  {

    //ログタグ
    val TAG = "UserActivity"

    //ユーザID
    var id = ""
    //コンテキスト
    lateinit var context : Context

    /**
     * 初期処理
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        //遷移項目取得
        id = intent.getStringExtra(Constant.INTENT_ID_KEY)
        val name = intent.getStringExtra(Constant.INTENT_NAME_KEY)
        //コンテキスト取得
        context = applicationContext

        //レイアウト取得
        val dateLayout = findViewById<TextView>(R.id.date)
        val userLayout = findViewById<TextView>(R.id.user)
        val monthLayout = findViewById<TextView>(R.id.month)
        val goWorkBtn = findViewById<Button>(R.id.go_work)
        val outWorkBtn = findViewById<Button>(R.id.out_work)

        //名前を設定
        userLayout.text = name

        //出勤状態からボタンの制御
        when (intent.getStringExtra(Constant.INTENT_KINTAIFLG_KEY)){
            Constant.NONE_WORK -> {
                goWorkBtn.isEnabled  = true
                outWorkBtn.isEnabled  = true
            }
            Constant.IN_WORK -> {
                goWorkBtn.isEnabled  = false
            }
            Constant.OUT_WORK, Constant.REST -> {
                goWorkBtn.isEnabled  = false
                outWorkBtn.isEnabled  = false
            }
        }

        //現在日付を表示
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        dateLayout.text = dateFormat.format(date)

        //現在月を表示
        val monthFormat = SimpleDateFormat("MM月", Locale.getDefault())
        monthLayout.text = monthFormat.format(date)

        //読み込みでDBインスタンス取得
        val dbHelper = SqlDBHelper(applicationContext, Constant.DB_NAME, null, Constant.DB_VARSION)
        val database = dbHelper.readableDatabase

        //ユーザMAPを格納するリスト
        val kintaiListMap = mutableListOf<MutableMap<String, String>>()
        //ユーザMAPを格納するリスト
        val calenderListMap = mutableListOf<MutableMap<String, String>>()

        try{
            //SQLを実行してカーソル取得
            val kintaiCursor = database.rawQuery(Constant.USER_KINTAI_SQL.replace("ID", id), null)
            if (kintaiCursor.count > 0) {
                kintaiCursor.moveToFirst()
                while (!kintaiCursor.isAfterLast) {
                    //ユーザMAP作成
                    val map = mutableMapOf<String, String>()
                    //IDと名前と勤怠フラグを格納する
                    map[Constant.USERKINTAI_KINTAIDATE_KEY] = kintaiCursor.getString(0)
                    map[Constant.USERKINTAI_INTIME_KEY] = kintaiCursor.getString(1)
                    map[Constant.USERKINTAI_OUTTIME_KEY] = kintaiCursor.getString(2)
                    Log.d(TAG, "日："+kintaiCursor.getString(0))
                    Log.d(TAG, "出勤時間："+kintaiCursor.getString(1))
                    Log.d(TAG, "退勤時間："+kintaiCursor.getString(2))
                    //リストに追加
                    kintaiListMap.add(map)
                    kintaiCursor.moveToNext()
                }
            }
            //カーソルクローズ
            kintaiCursor.close()

            //SQLを実行してカーソル取得
            val calenderCursor = database.rawQuery(Constant.CALENDER_SQL, null)
            if (calenderCursor.count > 0) {
                calenderCursor.moveToFirst()
                while (!calenderCursor.isAfterLast) {
                    //ユーザMAP作成
                    val map = mutableMapOf<String, String>()
                    //IDと名前と勤怠フラグを格納する
                    map[Constant.CALENDERDATA_DAY_KEY] = calenderCursor.getString(0)
                    map[Constant.CALENDERDATA_DAYSTATE_KEY] = calenderCursor.getString(1)
                    map[Constant.CALENDERDATA_HOLIDAY_KEY] = calenderCursor.getString(2)
                    Log.d(TAG, "日："+calenderCursor.getString(0))
                    Log.d(TAG, "状態："+calenderCursor.getString(1))
                    Log.d(TAG, "祝日："+calenderCursor.getString(2))
                    //リストに追加
                    calenderListMap.add(map)
                    calenderCursor.moveToNext()
                }
            }
            //カーソルクローズ
            calenderCursor.close()
        }catch(exception: Exception) {
            Log.e(TAG, exception.toString())
        }finally {
            database.close()
        }

        //レイアウトの親を取得
        val tableMainLayout = findViewById<Button>(R.id.table_main_layout) as TableLayout
        //一旦子レイアウトを削除する
        tableMainLayout.removeAllViews()

        //取得したカレンダーの日付分ループ
        for((index, map) in calenderListMap.withIndex()){
            //日付行レイアウトを取得
            var rowLayout = this.layoutInflater.inflate(R.layout.table_row_normal, null) as TableRow

            //土曜日、日曜日、祝日で取得するレイアウトを変える
            if(map[Constant.CALENDERDATA_HOLIDAY_KEY] == Constant.NOT_HOLIDAY) {
                when (map[Constant.CALENDERDATA_DAYSTATE_KEY]) {
                    Constant.SATURDAY -> {
                        rowLayout = this.layoutInflater.inflate(R.layout.table_row_staurday, null) as TableRow
                    }
                    Constant.SUNDAY -> {
                        rowLayout = this.layoutInflater.inflate(R.layout.table_row_sunday, null) as TableRow
                    }
                }
            }else{
                rowLayout = this.layoutInflater.inflate(R.layout.table_row_sunday, null) as TableRow
            }

            //日、出勤、退勤のレイアウトを取得する
            val dayText = rowLayout.findViewById<TextView>(R.id.day)
            val inTimeText = rowLayout.findViewById<TextView>(R.id.in_time)
            val outTimetext = rowLayout.findViewById<TextView>(R.id.out_time)
            //表示する日付を作成して設定する
            val dayTexStr : String = (index + 1).toString() + "日"
            dayText.text = dayTexStr

            //0付きで取得しているため1桁は0を追加
            var a = "0"
            if((index + 1) < 10){
                a += (index + 1).toString()
            }else{
                a = (index + 1).toString()
            }
            //出勤または退勤が存在したら設定する
            for(kintaiMap in kintaiListMap){
                if(kintaiMap[Constant.USERKINTAI_KINTAIDATE_KEY] == a){
                    inTimeText.text = kintaiMap[Constant.USERKINTAI_INTIME_KEY]
                    outTimetext.text = kintaiMap[Constant.USERKINTAI_OUTTIME_KEY]
                }
            }
            tableMainLayout.addView(rowLayout)
        }
    }

    /**
     * 出勤ボタン
     */
    fun clickGoWork(view: View) {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("出勤しますか？")
            .setPositiveButton("はい") { dialog, which ->
                changeKintai(Constant.IN_WORK)
            }
            .setNegativeButton("いいえ") { dialog, which -> }.show()
    }

    /**
     * 退勤ボタン
     */
    fun clickOutWork(view: View) {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("退勤しますか？")
            .setPositiveButton("はい") { dialog, which ->
                changeKintai(Constant.OUT_WORK)
            }
            .setNegativeButton("いいえ") { dialog, which -> }.show()
    }

    /**
     * 戻るボタン
     */
    fun clickBack(view: View) {
        //メインの画面に遷移する
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 勤務変更
     * 1:出勤 2:退勤
     */
    private fun changeKintai(kintaiState : String) {

        Log.d(TAG, "変更する勤怠状態：$kintaiState")
        //勤怠状態の変更
        val url = Constant.URL + Constant.CHANGE_KINTAI + "?id=$id&kintai=$kintaiState"
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "通信失敗")
                Log.d(TAG, e.toString())
            }
            override fun onResponse(call: Call, response: Response) {
                val responseText : String? = response.body?.string()
                Log.d(TAG, "通信成功")

                //レスポスが１だったらエラーのためリターン
                if(responseText == "1"){ return }

                //トーストのレイアウトを取得
                val view = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.toast, null)
                val textView = view.findViewById(R.id.message) as TextView

                //出勤状態によって言葉を変える
                if(kintaiState == Constant.IN_WORK) textView.text = Constant.INWORK_MESSAGE else textView.text = Constant.OUTWORK_MESSAGE
                //トーストを表示する
                runOnUiThread  {
                    Toast(context).run {
                        this.view = view
                        duration = Toast.LENGTH_SHORT
                        setGravity(Gravity.CENTER, 0, 0)
                        show()
                        onDetachedFromWindow().run {
                            clickBack(view)
                        }
                    }
                }
            }
        })
    }

    override fun onBackPressed() {}

}