package jp.co.fuyouj.attendapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {

    //ログタグ
    val TAG = "MainActivity"

    //ユーザMAPを格納するリスト
    private val listMap = mutableListOf<MutableMap<String, String>>()

    /**
     * 起動処理
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ユーザ情報の取得
        getUserData()
    }

    /**
     * サーバからユーザデータの取得
     */
    private fun getUserData(){

        //ユーザデータ取得
        val url = Constant.URL + Constant.GET_USER_KINTAI
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

                //DB格納用
                val values_userKintai = ContentValues()
                //JSON変換
                val parentJsonObj = JSONArray(responseText)
                Log.d(TAG, "取得項目：$parentJsonObj")

                //書き込みでDBインスタンス取得
                val dbHelper = SqlDBHelper(applicationContext, Constant.DB_NAME, null, Constant.DB_VARSION)
                val database = dbHelper.writableDatabase

                try{
                    //一旦DB値をすべて削除
                    database.delete(Constant.TABLE_NAME_USERKINTAI, null, null)

                    //取得したリスト分ループ
                    for (i in 0 until parentJsonObj.length()) {
                        //リストから個別のユーザ情報取得
                        val detailJsonObj = parentJsonObj.getJSONObject(i)
                        //DB格納用箱に入れる
                        values_userKintai.put(Constant.USERKINTAI_ID_KEY, detailJsonObj.getString(Constant.USERKINTAI_ID_KEY))
                        values_userKintai.put(Constant.USERKINTAI_KINTAIDATE_KEY, detailJsonObj.getString(Constant.USERKINTAI_KINTAIDATE_KEY))
                        values_userKintai.put(Constant.USERKINTAI_KINTAIFLG_KEY, detailJsonObj.getString(Constant.USERKINTAI_KINTAIFLG_KEY))
                        values_userKintai.put(Constant.USERKINTAI_INTIME_KEY, detailJsonObj.getString(Constant.USERKINTAI_INTIME_KEY))
                        values_userKintai.put(Constant.USERKINTAI_OUTTIME_KEY, detailJsonObj.getString(Constant.USERKINTAI_OUTTIME_KEY))

                        //値を挿入する
                        database.insertOrThrow(Constant.TABLE_NAME_USERKINTAI, null, values_userKintai)
                    }
                    //画面項目を設定する
                    runOnUiThread  { displaySet() }

                }catch(exception: Exception) {
                    Log.e(TAG, exception.toString())
                }finally {
                    database.close()
                }
            }
        })
    }

    /**
     * 画面項目の設定
     */
    private fun displaySet(){

        //読み込みでDBインスタンス取得
        val dbHelper = SqlDBHelper(applicationContext, Constant.DB_NAME, null, Constant.DB_VARSION)
        val database = dbHelper.readableDatabase

        try{
            //SQLを実行してカーソル取得
            val cursor = database.rawQuery(Constant.USER_INFO_SQL, null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    //ユーザMAP作成
                    val map = mutableMapOf<String, String>()
                    //IDと名前と勤怠フラグを格納する
                    map[Constant.USERDATA_ID_KEY] = cursor.getString(0)
                    map[Constant.USERDATA_NAME_KEY] = cursor.getString(1)
                    if(cursor.getString(2) == null){
                        map[Constant.USERKINTAI_KINTAIFLG_KEY] = Constant.NONE_WORK
                    }else{
                        map[Constant.USERKINTAI_KINTAIFLG_KEY] = cursor.getString(2)
                    }
                    //リストに追加
                    listMap.add(map)
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }catch(exception: Exception) {
            Log.e(TAG, exception.toString())
        }finally {
            database.close()
        }

        //ユーザMAP数取得
        val userNum = listMap.count()
        Log.d(TAG, "ユーザ数：$userNum")
        //表示列を固定いているためユーザ数を割って行数を取得する
        var row : Int = userNum / Constant.COLUMN_NUM_USER
        //余りがある場合もう一行追加
        if (userNum % Constant.COLUMN_NUM_USER != 0){
            row += 1
        }
        Log.d(TAG, "行数：$row")
        //レイアウトの親を取得
        val userNameLayout = findViewById<LinearLayout>(R.id.user_name_layout)
        //一旦子レイアウトを削除する
        userNameLayout.removeAllViews()
        //作成したレイアウトのカウント変数初期化
        var count = 0
        //計算した行数分ループ
        for(i in 1..row){
            //行のレイアウト作成
            val rowLayout  = LinearLayout(this)
            rowLayout.orientation = HORIZONTAL
            rowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.weightSum = 4.0F
            //5列作成のため5回ループ
            for(a in 0..3){
                try{
                    //ユーザMAP取得
                    val userMap = listMap[count]

                    //ボタンのレイアウトを勤怠フラグによって取得
                    var btnLayout = this.layoutInflater.inflate(R.layout.user_btn_mi, null) as TextView
                    when (userMap[Constant.USERKINTAI_KINTAIFLG_KEY]){
                        Constant.IN_WORK -> {
                            btnLayout = this.layoutInflater.inflate(R.layout.user_btn_in, null) as TextView
                        }
                        Constant.OUT_WORK -> {
                            btnLayout = this.layoutInflater.inflate(R.layout.user_btn_out, null) as TextView
                        }
                        Constant.REST -> {
                            btnLayout = this.layoutInflater.inflate(R.layout.user_btn_rest, null) as TextView
                        }
                    }
                    //ボタンレイアウトを設定
                    btnLayout.layoutParams = LinearLayout.LayoutParams(0, 150, 1.0F)
                    btnLayout.setText(userMap[Constant.USERDATA_NAME_KEY],null)
                    btnLayout.id = Integer.parseInt(userMap[Constant.USERDATA_ID_KEY].toString())
                    //ボタンレイアウトを行レイアウトに追加
                    rowLayout.addView(btnLayout)
                    //カウントを1上げる
                    count++
                }catch(exception: Exception){
                    //5個なかったらExceptionで終了させる
                    Log.d(TAG, "なくなったから終わり")
                    continue
                }
            }
            //親レイアウトに行レイアウトを追加
            userNameLayout.addView(rowLayout)
        }
        //親レイアウトに戻るボタンを追加
        userNameLayout.addView(this.layoutInflater.inflate(R.layout.menu_back_btn, null) as Button)
    }

    /**
     * ユーザクリック処理
     */
    fun clickUser(view: View) {
        //インテント作成
        val intent = Intent(this, UserActivity::class.java)
        var selectUserMap : MutableMap<String,String> = mutableMapOf()
        for(userMap in listMap){
            if(userMap[Constant.USERDATA_ID_KEY] == view.id.toString()){
                selectUserMap = userMap
                break
            }
        }
        //IDと名前を遷移項目として格納
        intent.putExtra(Constant.INTENT_ID_KEY, view.id.toString())
        intent.putExtra(Constant.INTENT_NAME_KEY, selectUserMap[Constant.USERDATA_NAME_KEY])
        intent.putExtra(Constant.INTENT_KINTAIFLG_KEY, selectUserMap[Constant.USERKINTAI_KINTAIFLG_KEY])
        Log.d(TAG, "選択ID:" + view.id)
        Log.d(TAG, "選択名前:" + selectUserMap[Constant.USERDATA_NAME_KEY])
        Log.d(TAG, "選択勤怠フラグ:" + selectUserMap[Constant.USERKINTAI_KINTAIFLG_KEY])
        startActivity(intent)
        finish()
    }

    /**
     * 戻るクリック処理
     */
    fun clickMenuBack(view: View) {
        //メインの画面に遷移する
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onBackPressed() {}
}
