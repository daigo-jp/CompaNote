package jp.ac.neec.it.k023c0039.jobcalendar

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels // ★ by viewModels のために必要
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer // ★ LiveData の監視に必要

// ★ AsyncTaskや通信関連のごちゃごちゃした import は全て不要になる

class CompanyRegister : AppCompatActivity() {

    // DatabaseHelper は ViewModel が管理するので、Activityはもう持つ必要がない
    // private lateinit var _helper: DatabaseHelper // ← 削除

    private lateinit var companyNameEditText: EditText
    private lateinit var registerButton: Button

    // --- ★★★ ここが最大の変更点 ★★★ ---
    // 「by viewModels」を使い、専用の「工場」を指定して ViewModel を取得
    private val viewModel: RegistrationViewModel by viewModels {
        RegistrationViewModelFactory(this.applicationContext)
    }
    // --- ★★★ --------------------- ★★★ ---


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_register)

        // _helper の初期化は不要になる
        companyNameEditText = findViewById(R.id.et_CompanyName)
        registerButton = findViewById(R.id.btn_save)

        // --- ボタンが押された時の処理 ---
        registerButton.setOnClickListener {
            val name = companyNameEditText.text.toString().trim()
            if (name.isNotBlank()) {
                // ★ やることは「頭脳（ViewModel）」に依頼するだけ。
                // ★ ここには非同期処理もDBロジックも一切書かない。
                viewModel.registerCompany(name)
            } else {
                Toast.makeText(this, "企業名を入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        // --- ★ ViewModelからの「結果」を監視する処理 ---
        // (AsyncTask の onPostExecute の代わり)
        viewModel.registrationStatus.observe(this, Observer { statusMessage ->
            // ViewModelの LiveData (statusMessage) が変更されるたびに、
            // この { } の中が自動的に（メインスレッドで）実行される

            Toast.makeText(this, statusMessage, Toast.LENGTH_LONG).show()

            if (statusMessage.contains("登録完了")) {
                companyNameEditText.text.clear()
            }
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // (変更なし)
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



}