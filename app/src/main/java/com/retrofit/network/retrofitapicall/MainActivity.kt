package com.retrofit.network.retrofitapicall

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var apiClient: ApiClient
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        button = findViewById(R.id.onClick)
        button.setOnClickListener {

            apiClient = ApiClient(
                baseUrl = "https://jiobeatplanner.st.ril.com:8082/api/ChannelPartnersApi/",
                isDebug = true
            )

            val requestJson =
                """{"data":"0DYkiFRh5JEEdvXJViNPj3TBZ+6bOKq7PSTZABiYITUBeG8lbrN69mrjTMjW14tn7boAv/ZmZws4cHat1GMSjw=="}"""

            val headers = mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Content-Length" to "113",
                "appVersion" to "5.1.0",
                "appOS" to "1",
                "token" to "0adw1nrb0hilWAxkLGc30uDfzGGiNQfF",
            )

            apiClient.callApi(
                RequestType.POST,
                "GetChannelPartnersDetailsInArea",
                headers,
                requestJson
            ) { result ->   // callback invoked asynchronously
                when (result) {
                    is ApiResult.Success -> {
                        val response = result.data  // OkHttp Response
                        val bodyStr = response.body?.string()  // consume when needed
                        Log.d("API", "Response code: ${response.code}, Body: $bodyStr")
                    }

                    is ApiResult.Failure -> {
                        Log.e("API", "Error: ${result.message}")
                    }
                }
            }
        }
    }
}
//./gradlew publishToMavenLocal