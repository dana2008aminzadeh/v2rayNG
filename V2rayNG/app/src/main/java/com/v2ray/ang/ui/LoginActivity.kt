package com.v2ray.ang.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v2ray.ang.R
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. بررسی لاگین بودن کاربر (حل مشکل پریدن اکانت با بستن برنامه)
        val sharedPref = getSharedPreferences("v2rayng_user_data", Context.MODE_PRIVATE)
        val remaining = sharedPref.getString("user_remaining_data", "")
        if (!remaining.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return // توقف اجرای صفحه لاگین و ورود مستقیم به برنامه
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvTitle = findViewById<TextView>(R.id.tvLoginTitle)
        val tvSub = findViewById<TextView>(R.id.tvLoginSub)
        val tvQr = findViewById<TextView>(R.id.tvQr)

        // 2. تنظیم خودکار زبان بر اساس زبان گوشی کاربر
        val isFa = Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar"
        if (isFa) {
            tvTitle.text = "ورود به حساب"
            tvSub.text = "لطفاً نام کاربری و رمز عبور خود را وارد کنید"
            etUsername.hint = "نام کاربری"
            etPassword.hint = "رمز عبور"
            btnLogin.text = "دریافت سرورها"
            tvQr.text = "ورود با بارکد"
        }

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString()
            val pass = etPassword.text.toString()
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                performLogin(user, pass, isFa)
            }
        }
    }

    private fun performLogin(username: String, pass: String, isFa: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://dana.s16.viptelbot.top/v2/api.php?action=login")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:109.0) Gecko/109.0 Firefox/119.0")

                val postData = "username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(pass, "UTF-8")}"
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(postData)
                writer.flush()

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (json.getBoolean("success")) {
                        val accountInfo = json.getJSONObject("account_info")
                        
                        val sharedPref = getSharedPreferences("v2rayng_user_data", Context.MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("user_remaining_data", accountInfo.getString("remaining_data_gb"))
                            putString("user_days_left", accountInfo.getString("days_left"))
                            apply()
                        }

                        val serversArray = json.getJSONArray("servers")
                        var combinedLinks = ""
                        for (i in 0 until serversArray.length()) {
                            combinedLinks += serversArray.getString(i) + "\n"
                        }
                        
                        AngConfigManager.importBatchConfig(combinedLinks, "", false)
                        Toast.makeText(this@LoginActivity, if(isFa) "ورود موفقیت‌آمیز بود!" else "Login Successful!", Toast.LENGTH_SHORT).show()
                        
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, json.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, if(isFa) "خطا در ارتباط با سرور!" else "Connection Error!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
