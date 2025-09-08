package jp.ac.neec.it.k023c0039.jobcalendar

// import android.os.AsyncTask // ← 不要になるので削除
import android.os.Bundle
import android.view.MenuItem // (戻るボタン用に残す)
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.viewModels // ★ by viewModels のために必要
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer // ★ LiveData の監視に必要

class CompanyList : AppCompatActivity() {

    // DBヘルパーはVMが持つので、Activityはもう持つ必要がない
    // private lateinit var _helper: DatabaseHelper // ← 削除

    private lateinit var lvCompanyList: ListView
    private lateinit var tvCompanyDetail: TextView

    // ★ クリック時に使うため、データリストはクラス変数として保持する
    private var currentDataList: List<Map<String, Any?>> = listOf()

    // --- ★★★ ViewModel（頭脳）の取得 ★★★ ---
    private val viewModel: CompanyListViewModel by viewModels {
        CompanyListViewModelFactory(this.applicationContext)
    }
    // --- ★★★ --------------------- ★★★ ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_list)

        // _helper の初期化は不要になる
        lvCompanyList = findViewById(R.id.listView)
        tvCompanyDetail = findViewById(R.id.CompanyDetail)

        // --- ★ ViewModelからの「結果」を監視する処理 ---
        // (AsyncTask の onPostExecute の代わり)
        viewModel.companyList.observe(this, Observer { companyList ->
            // ViewModelの LiveData (companyList) が変更されるたびに、
            // この { } の中が自動的に（メインスレッドで）実行される

            // 1. 取得した結果をクラス変数に保存 (クリック処理で使うため)
            currentDataList = companyList

            // 2. SimpleAdapterを生成
            val adapter = SimpleAdapter(
                this,
                companyList, // VMから受け取ったリスト
                R.layout.list_item_company,
                arrayOf("name_for_list", "details_preview"),
                intArrayOf(R.id.tv_item_company_name, R.id.tv_item_company_details)
            )

            // 3. ListViewにアダプターをセット
            lvCompanyList.adapter = adapter
        })

        // --- リスト項目がクリックされた時の処理 (変更なし) ---
        lvCompanyList.setOnItemClickListener { parent, view, position, id ->
            // 保存しておいたリストからデータを取得
            val clickedItemMap = currentDataList[position]
            val detailsText = clickedItemMap["full_details"] as String?
            tvCompanyDetail.text = detailsText ?: "詳細情報はありません。"
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        // 画面が表示されるたびに、VMに「データ読み込み」を命令する
        viewModel.loadCompanies()
    }


    // ★★★ private inner class LoadListTask : AsyncTask<...> ★★★
    // ★★★
    // ★★★ この下に書いてあった AsyncTask (LoadListTask) の巨大なコードは、
    // ★★★ まるごと削除する（ViewModelに引っ越したため）
    // ★★★


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // (戻るボタン処理。変更なし)
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // ★ onDestroy() での _helper.close() も不要になります
}