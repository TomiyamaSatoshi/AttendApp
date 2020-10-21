package jp.co.fuyouj.attendapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MenuActivity : AppCompatActivity() {

    //ログタグ
    val TAG = "MenuActivity"

    /**
     * 起動処理
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        //ユーザデータ取得
        val url = Constant.URL + Constant.GET_BASES
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

                //JSON変換
                val parentJsonObj = JSONArray(responseText)
                Log.d(TAG, "取得項目：$parentJsonObj")

                //画面設定
                runOnUiThread{ setDisplay(parentJsonObj) }
            }
        })
    }

    /**
     * 画面表示設定
     */
    fun setDisplay(parentJsonObj : JSONArray){

        //レイアウトの親を取得
        val baseNameLayout = findViewById<LinearLayout>(R.id.base_name_layout)
        //一旦子レイアウトを削除する
        baseNameLayout.removeAllViews()

        //行のレイアウト
        val rowLayout = this.layoutInflater.inflate(R.layout.base_row, null) as LinearLayout
        rowLayout.weightSum = 5.0F

        //拠点数分ループ
        for (i in (0 until parentJsonObj.length())) {

            //リストから個別の拠点情報取得
            val detailJsonObj = parentJsonObj.getJSONObject(i)
            Log.d(TAG, "拠点" + detailJsonObj.getString(Constant.BASEDATA_NAME_KEY))
            Log.d(TAG, "拠点ID" + detailJsonObj.getString(Constant.BASEDATA_ID_KEY))

            //ボタンのレイアウトを勤怠フラグによって取得
            val btnLayout = this.layoutInflater.inflate(R.layout.base_btn, null) as Button
            //ボタンレイアウトを設定
            btnLayout.layoutParams = LinearLayout.LayoutParams(0, 150, 1.0F)
            btnLayout.setText(detailJsonObj.getString(Constant.BASEDATA_NAME_KEY),null)
            btnLayout.id = Integer.parseInt(detailJsonObj.getString(Constant.BASEDATA_ID_KEY))
            //ボタンレイアウトを行レイアウトに追加
            rowLayout.addView(btnLayout)
        }
        //親レイアウトに行レイアウトを追加
        baseNameLayout.addView(rowLayout)
    }

    /**
     * 初期化に遷移
     */
    fun clickBase(view : View){
        //ユーザ情報と日付情報を初期化
        initializeData(view.id.toString())
    }

    /**
     * 初期化処理
     */
    private fun initializeData(baseId : String){

        //ユーザデータ取得
        val url = Constant.URL + Constant.GET_USERS + "?baseId=$baseId"
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
                val values_userData = ContentValues()
                //JSON変換
                val parentJsonObj = JSONArray(responseText)
                Log.d(TAG, "取得項目：$parentJsonObj")

                //書き込みでDBインスタンス取得
                val dbHelper = SqlDBHelper(applicationContext, Constant.DB_NAME, null, Constant.DB_VARSION)
                val database = dbHelper.writableDatabase

                try{
                    //一旦DB値をすべて削除
                    database.delete(Constant.TABLE_NAME_USERDATA, null, null)

                    //取得したリスト分ループ
                    for (i in 0 until parentJsonObj.length()) {
                        //リストから個別のユーザ情報取得
                        val detailJsonObj = parentJsonObj.getJSONObject(i)
                        //DB格納用箱に入れる
                        values_userData.put(Constant.USERDATA_ID_KEY, detailJsonObj.getString(Constant.USERDATA_ID_KEY))
                        values_userData.put(Constant.USERDATA_NAME_KEY, detailJsonObj.getString(Constant.USERDATA_NAME_KEY))

                        //値を挿入する
                        database.insertOrThrow(Constant.TABLE_NAME_USERDATA, null, values_userData)
                    }
                    //日付情報の取得
                    getDayInfo()

                }catch(exception: Exception) {
                    Log.e(TAG, exception.toString())
                }finally {
                    database.close()
                }
            }
        })
    }

    /**
     * 日付情報の取得
     */
    fun getDayInfo(){
        //日付情報取得
        val url = Constant.URL + Constant.GET_DAY_INFO
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
                val values_dayData = ContentValues()
                //JSON変換
                val parentJsonObj = JSONArray(responseText)
                Log.d(TAG, "取得項目：$parentJsonObj")

                //書き込みでDBインスタンス取得
                val dbHelper = SqlDBHelper(applicationContext, Constant.DB_NAME, null, Constant.DB_VARSION)
                val database = dbHelper.writableDatabase

                try{
                    //一旦DB値をすべて削除
                    database.delete(Constant.TABLE_NAME_CALENDERDATA, null, null)

                    //取得したリスト分ループ
                    for (i in 0 until parentJsonObj.length()) {
                        //リストから個別のユーザ情報取得
                        val detailJsonObj = parentJsonObj.getJSONObject(i)
                        //DB格納用箱に入れる
                        values_dayData.put(Constant.CALENDERDATA_DAY_KEY, detailJsonObj.getString(Constant.CALENDERDATA_DAY_KEY))
                        values_dayData.put(Constant.CALENDERDATA_DAYSTATE_KEY, detailJsonObj.getString(Constant.CALENDERDATA_DAYSTATE_KEY))
                        values_dayData.put(Constant.CALENDERDATA_HOLIDAY_KEY, detailJsonObj.getString(Constant.CALENDERDATA_HOLIDAY_KEY))

                        //値を挿入する
                        database.insertOrThrow(Constant.TABLE_NAME_CALENDERDATA, null, values_dayData)
                    }
                    //画面遷移
                    moveActivity()

                }catch(exception: Exception) {
                    Log.e(TAG, exception.toString())
                }finally {
                    database.close()
                }
            }
        })
    }

    /**
     * 遷移処理
     */
    fun moveActivity() {
        //インテント作成
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {}
}