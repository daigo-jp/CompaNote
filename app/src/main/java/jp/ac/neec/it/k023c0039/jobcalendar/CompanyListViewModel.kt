package jp.ac.neec.it.k023c0039.jobcalendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 企業一覧画面専用の ViewModel
 * （DBヘルパーを必要とする）
 */
class CompanyListViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {

    // DBから読み取った全データを格納・通知するための変数 (LiveData)
    // List<Map<String, Any?>> 型 ＝ SimpleAdapter が必要とするデータ形式
    private val _companyList = MutableLiveData<List<Map<String, Any?>>>()
    val companyList: LiveData<List<Map<String, Any?>>> = _companyList

    /**
     * このVMが作成されたとき（またはリフレッシュしたいとき）に呼ばれる関数
     * (Activityの onResume から呼ぶ)
     */
    fun loadCompanies() {
        // AsyncTask.execute() の代わり。IOスレッドでDB読み込みを開始
        viewModelScope.launch(Dispatchers.IO) {

            val dataList = mutableListOf<Map<String, Any?>>()
            val db = dbHelper.readableDatabase // ★DBヘルパーはVM(のコンストラクタ)が持っている

            try {
                // ここは LoadListTask.doInBackground と全く同じロジック
                val cursor = db.query(
                    "CompanyList",
                    arrayOf("_id", "name", "details"),
                    null, null, null, null, "_id DESC"
                )

                cursor.use {
                    while (it.moveToNext()) {
                        val name = it.getString(it.getColumnIndexOrThrow("name"))
                        val details = it.getString(it.getColumnIndexOrThrow("details"))

                        val preview = details?.take(50) + "..." // 詳細の先頭50文字プレビュー

                        val item = mapOf(
                            "name_for_list" to name,      // リスト1行目用
                            "details_preview" to preview, // リスト2行目用
                            "full_details" to details     // ★クリック時に使う「詳細全文」
                        )
                        dataList.add(item)
                    }
                }
            } finally {
                db.close() // DBクローズを忘れない
            }

            // 取得した全リストをメインスレッドに通知（postValue）
            _companyList.postValue(dataList)
        }
    }
}