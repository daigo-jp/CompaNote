package jp.ac.neec.it.k023c0039.jobcalendar // (あなたのパッケージ名)

import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// XMLの `tools:context` に合わせてクラス名を変更
class CompanyList : AppCompatActivity() {

    private lateinit var _helper: DatabaseHelper

    // --- UI部品の変数を宣言 ---
    private lateinit var lvCompanyList: ListView
    private lateinit var tvCompanyDetail: TextView

    // --- DBから取得した全データを保持しておくためのリスト ---
    // (クリックされた時に、どの企業の詳細を表示するか判断するために必要)
    private var companyDataList: List<Map<String, Any>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // あなたが送ってくれたレイアウトファイルを指定
        setContentView(R.layout.activity_company_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        _helper = DatabaseHelper(this)

        // --- レイアウトファイルからUI部品のインスタンスを取得 ---
        // (IDをあなたのXMLに合わせる)
        lvCompanyList = findViewById(R.id.listView)
        tvCompanyDetail = findViewById(R.id.CompanyDetail)

        // ★★★ 最大の変更点： ListViewの項目がクリックされたときの処理を追加 ★★★
        lvCompanyList.setOnItemClickListener { parent, view, position, id ->
            // `position` に、クリックされたのが何番目の項目か入っている

            // 1. 事前に保存しておいた全データリストから、クリックされた項目のデータを取得
            val clickedItem = companyDataList[position]

            // 2. データから詳細情報を取り出す
            val name = clickedItem["name"] as String
            val address = clickedItem["address"] as String
            val industry = clickedItem["industry"] as String

            // 3. 詳細表示用のTextViewに、見やすいように整形してセットする
            val detailText = """
                企業名: $name
                
                業種: $industry
                
                所在地: $address
            """.trimIndent() // trimIndent()で余分な空白を削除

            tvCompanyDetail.text = detailText
        }
    }

    override fun onResume() {
        super.onResume()
        // 画面が表示されるたびに、DBからリストを非同期で読み込み直す
        LoadListTask().execute()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 押されたボタンのIDをチェック
        when (item.itemId) {
            // IDが "home" (＝左上の矢印ボタン) だったら
            android.R.id.home -> {
                // このActivityを終了（破棄）して、前の画面に戻る
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * DBから企業リストを非同期で読み込むためのAsyncTask
     */
    private inner class LoadListTask : AsyncTask<Void, Void, List<Map<String, Any>>>() {

        override fun doInBackground(vararg params: Void?): List<Map<String, Any>> {
            val dataList = mutableListOf<Map<String, Any>>()
            val db = _helper.readableDatabase

            try {
                val cursor = db.query(
                    "CompanyList",
                    arrayOf("_id", "name", "address", "industry"),
                    null, null, null, null, "_id DESC" // 新しい順
                )

                cursor.use {
                    while (it.moveToNext()) {
                        val name = it.getString(it.getColumnIndexOrThrow("name"))
                        val address = it.getString(it.getColumnIndexOrThrow("address"))
                        val industry = it.getString(it.getColumnIndexOrThrow("industry"))

                        // ★重要：クリック時のために全データを保持しつつ、
                        // SimpleAdapterに必要なデータも作成する
                        val item = mapOf(
                            "name" to name,
                            "address" to address,
                            "industry" to industry,
                            // ↓ これはListViewの2行目に表示するための文字列
                            "details_for_list" to "$industry / $address"
                        )
                        dataList.add(item)
                    }
                }
            } finally {
                db.close()
            }
            return dataList
        }

        override fun onPostExecute(resultList: List<Map<String, Any>>) {
            // 1. 取得した結果をクラス変数に保存する (クリック処理で使うため)
            companyDataList = resultList

            // 2. SimpleAdapterを生成する
            val adapter = SimpleAdapter(
                this@CompanyList,
                resultList, // 表示するデータのリスト
                R.layout.list_item_company, // 1. で作成した「1行分」のレイアウト
                arrayOf("name", "details_for_list"), // Mapのどのキーのデータを
                intArrayOf(R.id.tv_item_company_name, R.id.tv_item_company_details) // どのTextViewに表示するか
            )

            // 3. ListViewにアダプターをセット
            lvCompanyList.adapter = adapter
        }
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}