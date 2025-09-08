package jp.ac.neec.it.k023c0039.jobcalendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * DatabaseHelper を必要とする CompanyListViewModel を作成するための専用工場
 */
class CompanyListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanyListViewModel::class.java)) {
            val dbHelper = DatabaseHelper(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return CompanyListViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}