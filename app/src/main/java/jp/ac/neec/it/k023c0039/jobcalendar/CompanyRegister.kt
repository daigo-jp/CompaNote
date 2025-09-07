package jp.ac.neec.it.k023c0039.jobcalendar

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel // ★ Gemini SDKのimport
import com.google.ai.client.generativeai.type.generateContent // ★ Gemini SDKのimport

// ★ 不要なimport (HttpURLConnection, JSONObject, URLなど) を削除

class CompanyRegister : AppCompatActivity() {

    private lateinit var _helper: DatabaseHelper
    private lateinit var companyNameEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_register)

        _helper = DatabaseHelper(this)
        companyNameEditText = findViewById(R.id.et_CompanyName)
        registerButton = findViewById(R.id.btn_save)

        registerButton.setOnClickListener {
            val name = companyNameEditText.text.toString().trim()
            if (name.isNotBlank()) {
                RegisterTask().execute(name)
            } else {
                Toast.makeText(this, "企業名を入力してください", Toast.LENGTH_SHORT).show()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * Gemini APIを使った登録処理を行うAsyncTask
     */
    private inner class RegisterTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            val companyName = params[0]
            try {
                // 1. DBチェック
                val existing = _helper.findCompanyByName(companyName)
                if (existing != null) {
                    return "エラー: 登録済みです"
                }

                // 2. Gemini APIの呼び出し
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = "YOUR_GEMINI_API_KEY_HERE" // ★★★ 必ず自分のAPIキーに差し替えてください ★★★
                )
                val prompt = "企業名「${companyName}」について、主要な事業内容、本社所在地、業種などの企業概要を200文字程度で簡潔にまとめてください。"

                val response = generativeModel.generateContent(prompt)
                val geminiDetails = response.text ?: "情報が取得できませんでした。"

                // 3. DB保存
                _helper.insertCompany(companyName, geminiDetails)

                return "登録完了: $companyName"

            } catch (e: Exception) {
                Log.e("RegisterTask", "Gemini APIまたはDB処理でエラー", e)
                return "エラーが発生しました: ${e.message}"
            }
        }

        override fun onPostExecute(resultMessage: String) {
            Toast.makeText(this@CompanyRegister, resultMessage, Toast.LENGTH_LONG).show()
            if (resultMessage.contains("登録完了")) {
                companyNameEditText.text.clear()
            }
        }
    } // --- RegisterTask ここまで ---


    // ★★★ 不要になった2つの補助関数 (downloadApiData と parseJsonData) はここから削除 ★★★


    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}