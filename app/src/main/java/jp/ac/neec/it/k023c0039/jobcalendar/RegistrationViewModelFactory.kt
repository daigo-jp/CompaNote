package jp.ac.neec.it.k023c0039.jobcalendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * DatabaseHelper を必要とする RegistrationViewModel を作成するための専用工場
 */
class RegistrationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            // 1. 必要な部品（DatabaseHelper）を作成
            val dbHelper = DatabaseHelper(context.applicationContext)

            // 2. 部品を渡してViewModelを生成し、返す
            @Suppress("UNCHECKED_CAST")
            return RegistrationViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}