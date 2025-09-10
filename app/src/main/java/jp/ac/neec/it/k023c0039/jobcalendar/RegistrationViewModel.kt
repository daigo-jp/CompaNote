package jp.ac.neec.it.k023c0039.jobcalendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers // ★ IOスレッドを指定するために必要
import kotlinx.coroutines.launch     // ★ コルーチンを起動するために必要
import jp.ac.neec.it.k023c0039.jobcalendar.BuildConfig


/**
 * 登録画面専用の ViewModel（AsyncTask の代わりとなる「頭脳」）
 *
 * このクラスは DatabaseHelper が必要なので、Factory経由で受け取る
 */
class RegistrationViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {

    // 処理結果をUI（Activity）に通知するための変数 (LiveData)
    private val _registrationStatus = MutableLiveData<String>()
    val registrationStatus: LiveData<String> = _registrationStatus

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY// ★★★ あなたのAPIキー
    )

    /**
     * Activityから呼ばれる「登録実行」関数
     */
    fun registerCompany(companyName: String) {

        // ★これがコルーチンの起動命令。AsyncTask.execute() の代わり。
        // ★viewModelScope が、このVMが生きている間だけ処理を自動管理してくれる。
        viewModelScope.launch(Dispatchers.IO) { // ★ DB/通信を行うため IO スレッドを指定

            try {
                // ここは AsyncTask.doInBackground と全く同じ考え方

                // 1. DBチェック (これは重い処理なので IO スレッドで実行)
                val existing = dbHelper.findCompanyByName(companyName)
                if (existing != null) {
                    _registrationStatus.postValue("エラー: 登録済みです") // メインスレッドに結果を通知
                    return@launch // 処理終了
                }

                // 2. Gemini API 呼び出し
                val prompt = "企業名「${companyName}」について、主要な事業内容、本社所在地、業種などの企業概要を200文字程度で簡潔にまとめてください。"

                // ★注目ポイント★
                // generateContent は suspend 関数だが、コルーチン(launch)の中からは
                // 「runBlocking」のような翻訳機なしで「直接」呼び出せる！
                val response = generativeModel.generateContent(prompt)
                val geminiDetails = response.text ?: "情報が取得できませんでした。"

                // 3. DB保存 (これも重い処理なので IO スレッドで実行)
                dbHelper.insertCompany(companyName, geminiDetails)

                _registrationStatus.postValue("登録完了: $companyName") // メインスレッドに成功を通知

            } catch (e: Exception) {

                // ★★★ 以下のデバッグ用ログを追加・変更します ★★★

                // 1. エラーの「種類（クラス名）」と「メッセージ」を特定する
                val errorType = e.javaClass.name // (例: "java.net.UnknownHostException")
                val errorMessage = e.message      // (例: "Unable to resolve host...")

                // 2. それをLogcatに強制的に出力する
                Log.e("ViewModel", "★根本原因(Type): [${errorType}]")
                Log.e("ViewModel", "★根本原因(Message): [${errorMessage}]")

                // 3. 元のログも残しておく
                Log.e("ViewModel", "Gemini APIまたはDB処理でエラー", e)

                // 4. UIへの通知も、エラーの種類がわかるように変更する
                _registrationStatus.postValue("エラー発生: ${errorType}")
            }
        }
    }
}