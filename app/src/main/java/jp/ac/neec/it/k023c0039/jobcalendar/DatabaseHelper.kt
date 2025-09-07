package jp.ac.neec.it.k023c0039.jobcalendar

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import java.sql.SQLException // @Throwsで使う

// (※ data class Company(...) は別ファイルに定義されている前提)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Company.db"
        private const val DATABASE_VERSION = 1
    }

    /**
     * テーブル作成
     */
    // DatabaseHelper.kt (抜粋)

    /**
     * テーブル作成
     * address と industry を削除し、「details TEXT」を追加
     */
    override fun onCreate(db: SQLiteDatabase) {
        val sb = StringBuilder()
        sb.append("CREATE TABLE CompanyList (")
        sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT, ")
        sb.append("name TEXT NOT NULL UNIQUE, ") // ★重複をDBレベルでも禁止するUNIQUEを追加
        sb.append("details TEXT")               // ★Geminiの回答を保存するカラム
        sb.append(");")
        val sql = sb.toString()
        db.execSQL(sql)
    }

    /**
     * 挿入（INSERT）関数の修正
     * 新しいカラムに合わせて引数を変更
     */
    @Throws(SQLException::class)
    fun insertCompany(name: String, details: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("details", details) // address, industry の代わりに details を保存
        }
        db.insertOrThrow("CompanyList", null, values)
        db.close()
    }

    /**
     * 検索（FIND）関数の修正
     * 戻り値の Company オブジェクトを新しい形式で返す
     */
    @Throws(IllegalArgumentException::class)
    fun findCompanyByName(name: String): Company? {
        val db = this.readableDatabase
        // 取得するカラムを "details" に変更
        val cursor: Cursor = db.query(
            "CompanyList",
            arrayOf("_id", "name", "details"), // address, industry を削除
            "name = ?",
            arrayOf(name),
            null, null, null, "1"
        )

        var company: Company? = null

        try {
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow("_id")
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                val detailsIndex = cursor.getColumnIndexOrThrow("details") // details を取得

                val id = cursor.getLong(idIndex)
                val foundName = cursor.getString(nameIndex)
                val foundDetails = cursor.getString(detailsIndex)

                // 新しい Company オブジェクトを返す
                company = Company(id, foundName, foundDetails)
            }
        } finally {
            cursor.close()
            db.close()
        }

        return company
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // バージョンアップ時の処理（今回は未使用）
    }
}