package com.example.weathermaterialdesign


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val mainView: Int = R.layout.activity_main_basic

        super.onCreate(savedInstanceState)
        setContentView(mainView)

        val pxHeight: Int
        val density: Float
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val displayMetrics = resources.displayMetrics
            pxHeight = windowMetrics.bounds.height()
            density  = displayMetrics.density
            Log.d("DBG",pxHeight.toString())

        }else{
            val windowMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(windowMetrics)
            pxHeight = windowMetrics.heightPixels
            density = windowMetrics.density
        }

        val bottomsheet = BottomSheetBehavior.from(findViewById(R.id.frame_container)).apply {
            maxHeight = pxHeight - (180 * density).roundToInt()
            peekHeight = 0
            state = BottomSheetBehavior.STATE_COLLAPSED
            isDraggable = false
        }
        val locationEdit = findViewById<TextInputEditText>(R.id.location_tf)
        val locationView = findViewById<TextView>(R.id.location_tv)
        val countryView = findViewById<TextView>(R.id.country_tv)

        val animFadeOut = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

        animFadeOut.fillAfter = true

        locationView.alpha = 0f
        countryView.alpha = 0f


        locationEdit.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(this.currentFocus?.windowToken , 0)
                    locationEdit.clearFocus()
                    getWeather(locationEdit.editableText.toString())
                    return@OnEditorActionListener true
                }
                false
            })

        locationView.setOnClickListener {
            bottomsheet.state = BottomSheetBehavior.STATE_COLLAPSED
            it.startAnimation(animFadeOut)
            countryView.startAnimation(animFadeOut)
            locationEdit.requestFocus()
        }
    }

    private fun getWeather(location: String){
        val apiClient = ApiClient.getInstance()
        val testResponse = apiClient.getWeather(ApiClient.API_KEY,location)

        testResponse?.enqueue(object : Callback<Response?> {
            override fun onResponse(
                call: Call<Response?>?,
                response: retrofit2.Response<Response?>?
            ) {
                val responseBody = response?.body()

                val animFadeIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
                animFadeIn.fillAfter = true

                findViewById<TextView>(R.id.location_tv).apply{
                    text = responseBody?.location?.name ?: "location not found"
                    this.alpha = 1f
                    this.startAnimation(animFadeIn)
                }
                findViewById<TextView>(R.id.country_tv).apply{
                    text = responseBody?.location?.country ?: "???"
                    this.alpha = 1f
                    this.startAnimation(animFadeIn)
                }

                ((responseBody?.current?.tempC?.toString() ?: "?") + "°C").also { findViewById<TextView>(R.id.temperature_tv).text = it }
                findViewById<ImageView>(R.id.weather_icon).setImageResource(
                    when (responseBody?.current?.condition?.code){
                        1000 -> if (responseBody.current.isDay == 1) R.drawable.ic_sun else R.drawable.ic_moon_25
                        1003 -> if (responseBody.current.isDay == 1) R.drawable.ic_cloud_sun else R.drawable.ic_cloud_moon
                        1006 -> R.drawable.ic_cloud
                        1009 -> R.drawable.ic_clouds
                        1030, 1135, 1147 -> R.drawable.ic_fog
                        1063, 1180 -> if (responseBody.current.isDay == 1) R.drawable.ic_cloud_rain_sun else R.drawable.ic_cloud_rain_moon
                        1066, 1069, 1210, 1216, 1222 -> if (responseBody.current.isDay == 1) R.drawable.ic_cloud_snow_sun else R.drawable.ic_cloud_snow_moon
                        1072, 1150 -> if (responseBody.current.isDay == 1) R.drawable.ic_cloud_drizzle_sun else R.drawable.ic_cloud_drizzle_moon
                        1087 -> R.drawable.ic_cloud_lightning
                        1114, 1117 -> R.drawable.ic_snow
                        in 1153..1171 -> R.drawable.ic_cloud_drizzle
                        in 1183..1201, 1240, 1243, 1246 -> R.drawable.ic_cloud_rain
                        1204, 1207, 1213, 1219, 1225, in 1249..1258 -> R.drawable.ic_cloud_snow
                        1237, 1261, 1264 -> R.drawable.ic_cloud_hail
                        1273 -> if (responseBody.current.isDay == 1) R.drawable.ic_cloud_rain_lightning_sun else R.drawable.ic_cloud_rain_lightning_moon
                        1276 -> R.drawable.ic_cloud_rain_lightning
                        1279 -> if (responseBody.current.isDay == 1) R.drawable.ic_cloud_drizzle_lightning_sun else R.drawable.ic_cloud_drizzle_lightning_moon
                        1282 -> R.drawable.ic_cloud_drizzle_lightning
                    else -> {R.drawable.ic_baseline_question_mark_24} })
                findViewById<TextView>(R.id.weather_desc_tv).text = responseBody?.current?.condition?.text

                findViewById<TextView>(R.id.weather_update_date_tv).text = responseBody?.current?.lastUpdated ?: "???"
                ((responseBody?.current?.windKph?.toString() ?: "???") + " km/h").also { findViewById<TextView>(R.id.wind_speed_tv).text = it }
                ((responseBody?.current?.pressureMb?.toString() ?: "???") + " hPa").also { findViewById<TextView>(R.id.pressure_tv).text = it }
                ((responseBody?.current?.humidity?.toString() ?: "???") + " %").also { findViewById<TextView>(R.id.humidity_tv).text = it }
                ((responseBody?.current?.feelslikeC?.toString() ?: "???") + " °C").also { findViewById<TextView>(R.id.feel_temp_tv).text = it }
                ((responseBody?.current?.cloud?.toString() ?: "???") + " %").also { findViewById<TextView>(R.id.cloud_tv).text = it }
                ((responseBody?.current?.precipMm?.toString() ?: "???") + " mm").also { findViewById<TextView>(R.id.precipitation_tv).text = it }
                BottomSheetBehavior.from(findViewById(R.id.frame_container)).state = BottomSheetBehavior.STATE_EXPANDED
            }

            override fun onFailure(call: Call<Response?>?, t: Throwable?) {
                Log.d("DBG",t.toString())
            }
        })
    }

    override fun onBackPressed() {
        if(BottomSheetBehavior.from(findViewById(R.id.frame_container)).state == BottomSheetBehavior.STATE_EXPANDED)
            findViewById<TextView>(R.id.location_tv).callOnClick()
        else
            super.onBackPressed()
    }
}