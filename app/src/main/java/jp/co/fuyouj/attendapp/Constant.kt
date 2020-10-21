package jp.co.fuyouj.attendapp

class Constant {

    companion object {
        //DB関連
        const val DB_NAME = "AttendDB"
        const val DB_VARSION = 7
        const val TABLE_NAME_USERDATA = "userData"
        const val TABLE_NAME_USERKINTAI = "userKintai"
        const val TABLE_NAME_CALENDERDATA = "calenderData"

        //キー
        const val BASEDATA_ID_KEY = "base"
        const val BASEDATA_NAME_KEY = "basenm"
        const val USERDATA_ID_KEY = "id"
        const val USERDATA_NAME_KEY = "name"
        const val CALENDERDATA_DAY_KEY = "day"
        const val CALENDERDATA_DAYSTATE_KEY = "daystate"
        const val CALENDERDATA_HOLIDAY_KEY = "holiday"
        const val USERKINTAI_ID_KEY = "id"
        const val USERKINTAI_KINTAIDATE_KEY = "kintaidate"
        const val USERKINTAI_KINTAIFLG_KEY = "kintaiflg"
        const val USERKINTAI_INTIME_KEY = "intime"
        const val USERKINTAI_OUTTIME_KEY = "outtime"

        //SQL
        const val USER_INFO_SQL = "SELECT d.id, d.name, k.kintaiflg, k.kintaidate " +
                " FROM userData d " +
                " LEFT OUTER JOIN (SELECT id, kintaiflg, kintaidate " +
                " FROM userKintai WHERE kintaidate = CURRENT_DATE) k ON d.id = k.id " +
                " ORDER BY k.kintaiflg, d.id"
        const val USER_KINTAI_SQL = "SELECT strftime('%d', kintaidate), intime, outtime " +
                " FROM userKintai " +
                " WHERE id = 'ID'" +
                " AND strftime('%m', 'now') = strftime('%m', kintaidate) " +
                " ORDER BY kintaidate"
        const val CALENDER_SQL = "SELECT day, daystate, holiday FROM calenderData"

        //通信関連
        const val URL = "http://118.27.37.1/"
        const val GET_BASES = "get-bases"
        const val GET_USERS = "get-users"
        const val GET_USER_KINTAI = "get-userskintai"
        const val CHANGE_KINTAI = "change-kintai"
        const val GET_DAY_INFO = "get-dayinfo"

        //ユーザ画面
        const val COLUMN_NUM_USER = 4

        //フラグ
        const val NONE_WORK = "0"
        const val IN_WORK = "1"
        const val OUT_WORK = "2"
        const val REST = "3"
        const val NOT_HOLIDAY = "0"
        const val HOLIDAY = "1"
        const val WORKDAY = "0"
        const val SATURDAY = "1"
        const val SUNDAY = "2"

        //遷移項目
        const val INTENT_ID_KEY = "id"
        const val INTENT_NAME_KEY = "name"
        const val INTENT_KINTAIFLG_KEY = "kintaiflg"

        //メッセージ
        const val INWORK_MESSAGE = "出勤しました。\nおはようございます。"
        const val OUTWORK_MESSAGE = "退勤しました。\nお疲れ様でした。"
    }
}