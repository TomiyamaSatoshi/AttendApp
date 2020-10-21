package jp.co.fuyouj.attendapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqlDBHelper (context: Context, databaseName:String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, databaseName, factory, version) {

    override fun onCreate(database: SQLiteDatabase?) {
        Log.d("DB", "テーブル作成")
        database?.execSQL(" create table if not exists userData ( " +
                "  id text " +
                ", name text " +
                ", PRIMARY KEY( id )) ")
        database?.execSQL(" create table if not exists userKintai ( " +
                "  id text " +
                ", kintaidate Date " +
                ", kintaiflg text" +
                ", intime text" +
                ", outtime text" +
                ", lastname text" +
                ", lastdate Date" +
                ", PRIMARY KEY( id, kintaidate )) ")
        database?.execSQL(" create table if not exists calenderData ( " +
                "  day Date " +
                ", daystate text " +
                ", holiday text" +
                ", lastname text" +
                ", lastdate Date" +
                ", PRIMARY KEY( day )) ")
        Log.d("DB", "テーブル作成終わり")
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("DB", "テーブル削除")
        database?.execSQL("drop table userData")
        database?.execSQL("drop table userKintai")
        database?.execSQL("drop table calenderData")
        onCreate(database)
    }
}