package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v2ray.ang.R
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.util.MmkvManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // باید ابتدا فایل xml بالا را ساخته باشید

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString()
            val pass = etPassword.text.toString()
            
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                performLogin(user, pass)
            }
        }
    }

    private fun performLogin(username: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // آدرس API خود را اینجا قرار دهید
                val url = URL("https://dana.s16.viptelbot.top/v2/api.php?action=login")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true

                val postData = "username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(pass, "UTF-8")}"
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(postData)
                writer.flush()

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (json.getBoolean("success")) {
                        val accountInfo = json.getJSONObject("account_info")
                        
                        // ذخیره اطلاعات اکانت در دیتابیس محلی برنامه (MMKV)
                        MmkvManager.encodeString("user_remaining_data", accountInfo.getString("remaining_data_gb"))
                        MmkvManager.encodeString("user_days_left", accountInfo.getString("days_left"))

                        // دریافت لیست سرورها و وارد کردن به برنامه
                        val serversArray = json.getJSONArray("servers")
                        var combinedLinks = ""
                        for (i in 0 until serversArray.length()) {
                            combinedLinks += serversArray.getString(i) + "\n"
                        }
                        
                        // استفاده از توابع اصلی v2rayNG برای ایمپورت کانفیگ‌ها
                        AngConfigManager.importBatchConfig(combinedLinks, "", false)

                        Toast.makeText(this@LoginActivity, "خوش آمدید!", Toast.LENGTH_SHORT).show()
                        
                        // انتقال به صفحه اصلی
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, json.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "خطا در ارتباط با سرور", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
