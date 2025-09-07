package jp.ac.neec.it.k023c0039.jobcalendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
    fun CompanyRegisterButtonClick(view: View) {
        val intent = Intent(this@MainActivity, CompanyRegister::class.java)
        startActivity(intent)
    }
    fun CompanyListButtonClick(view: View) {
        val intent = Intent(this@MainActivity, CompanyList::class.java)
        startActivity(intent)
    }
}