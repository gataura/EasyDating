package easy.dating.foryou.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import easy.dating.foryou._core.BaseActivity
import easy.dating.foryou.activities.MainScreenActivity
import com.github.arturogutierrez.Badges
import com.github.arturogutierrez.BadgesNotSupportedException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.onesignal.OneSignal
import com.uxcam.UXCam
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import easy.dating.foryou.*
import easy.dating.foryou.service.mUserIdClient
import kotlinx.android.synthetic.main.activity_web_view.*
import me.leolin.shortcutbadger.ShortcutBadger
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Minutes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 3/13/19.
 */
class SplashActivity : BaseActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    private lateinit var dataSnapshot: DataSnapshot

    private lateinit var database: DatabaseReference
    val REFERRER_DATA = "REFERRER_DATA"
    val badgeCount = 1

    lateinit var prefs: SharedPreferences

    private var client = mUserIdClient().build()

    lateinit var firebaseAnalytic: FirebaseAnalytics

    override fun getContentView(): Int = R.layout.activity_web_view

    private fun generateId() = client.generateId()


    override fun initUI() {
        webView = web_view
        progressBar = progress_bar

        firebaseAnalytic = FirebaseAnalytics.getInstance(this)

        prefs = getSharedPreferences("easy.dating.foryou", Context.MODE_PRIVATE)

        prefs.edit().putString("sessionTime", DateTime.now().toString()).apply()
        if (prefs.getString("sessionDate", "") != "") {
            if (Days.daysBetween(DateTime(prefs.getString("sessionDate", "")), DateTime.now()).days != 0) {
                prefs.edit().putString("minutesToday", "0").apply()
                prefs.edit().putString("sessionDate", DateTime.now().toString()).apply()
                prefs.edit().putBoolean("gtuToday", false).apply()
                prefs.edit().putBoolean("ltuToday", false).apply()
            }
        }

        checkReturn()


        UXCam.startWithKey("pzonkud8fbhz8mi")

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }


    fun getPreferer(context: Context): String? {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (!sp.contains(REFERRER_DATA)) {
            return "Didn't got any referrer follow instructions"
        }
        return sp.getString(REFERRER_DATA, null)
    }


    fun checkReturn() {
        if (prefs.getString("dateInstall", "") != "") {
            if (Days.daysBetween(DateTime(prefs.getString("dateInstall", "")), DateTime.now()).days == 1) {
                if (!prefs.getBoolean("rrToday", false)) {
                    prefs.edit().putString("rr", "RR1").apply()
                    val rrOneBundle = Bundle()
                    rrOneBundle.putString("RR1", "RR1")

                    firebaseAnalytic.logEvent("RR1", rrOneBundle)
                    prefs.edit().putBoolean("rrToday", true).apply()
                }
            } else if (Days.daysBetween(DateTime(prefs.getString("dateInstall", "")), DateTime.now()).days == 2) {
                if (prefs.getString("rr", "") != "") {
                    if (prefs.getString("rr", "")!!.contains("RR1")) {
                        if (!prefs.getBoolean("rrToday", false)) {
                            prefs.edit().putString("rr", "RR2").apply()
                            val rrOneBundle = Bundle()
                            rrOneBundle.putString("RR2", "RR2")

                            firebaseAnalytic.logEvent("RR2", rrOneBundle)
                            prefs.edit().putBoolean("rrToday", true).apply()
                        }
                    }
                }
            } else if (Days.daysBetween(DateTime(prefs.getString("dateInstall", "")), DateTime.now()).days == 3) {
                if (prefs.getString("rr", "") != "") {
                    if (prefs.getString("rr", "")!!.contains("RR2")) {
                        if (!prefs.getBoolean("rrToday", false)) {
                            prefs.edit().putString("rr", "RR3").apply()
                            val rrOneBundle = Bundle()
                            rrOneBundle.putString("RR3", "RR3")

                            firebaseAnalytic.logEvent("RR3", rrOneBundle)
                            prefs.edit().putBoolean("rrToday", true).apply()
                        }
                    }
                }
            }
        }
    }


    override fun setUI() {
        logEvent("splash-screen")
        webView.webViewClient = object : WebViewClient() {
            /**
             * Check if url contains key words:
             * /money - needed user (launch WebViewActivity or show in browser)
             * /main - bot or unsuitable user (launch ContentActivity)
             */
            @SuppressLint("deprecated")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains("/main")) {
                    // task url for web view or browser
//                    val taskUrl = dataSnapshot.child(TASK_URL).value as String
                    val value = dataSnapshot.child(SHOW_IN).value as String
                    var taskUrl = dataSnapshot.child(TASK_URL).value as String


                    taskUrl = prefs.getString("endurl", taskUrl).toString()

                    if (prefs.getBoolean("firstrun", true)) {

                        prefs.edit().putString("dateInstall", DateTime.now().toString()).apply()
                        prefs.edit().putString("sessionDate", DateTime.now().toString()).apply()

                        generateId().enqueue(object: Callback<String> {
                            override fun onFailure(call: Call<String>?, t: Throwable?) {
                                Log.d("UserId", "jpa")
                            }

                            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                                if (response?.body() != null) {
                                    val userIdBundle = Bundle()
                                    userIdBundle.putString("userId", response.body())

                                    firebaseAnalytic.logEvent("userId", userIdBundle)

                                    prefs.edit().putBoolean("firstrun", false).apply()
                                    if (taskUrl.contains("{t3}")) {
                                        taskUrl.replace("{t3}", response.body())
                                    }
                                }
                            }

                        })

                    }

                    if (value == WEB_VIEW) {
                            startActivity(
                                    Intent(this@SplashActivity, WebViewActivity::class.java)
                                .putExtra(EXTRA_TASK_URL, taskUrl)
                            )
                        finish()
                    } else if (value == BROWSER) {
                        // launch browser with task url
                        val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("")
                        )

                        logEvent("task-url-browser")
                        startActivity(browserIntent)
                        finish()
                    }
                } else if (url.contains("/main")) {
                    val taskUrl = dataSnapshot.child(TASK_URL).value as String
                    startActivity(Intent(this@SplashActivity, MainScreenActivity::class.java)
                            .putExtra(EXTRA_TASK_URL, taskUrl))
                    finish()
                }
                progressBar.visibility = View.GONE
                return false
            }
        }

        progressBar.visibility = View.VISIBLE


        val config = YandexMetricaConfig.newConfigBuilder("6e43103a-29f0-42b8-8264-3178b799e6b4").build()
        YandexMetrica.activate(this, config)
        YandexMetrica.enableActivityAutoTracking(this.application)

        database = FirebaseDatabase.getInstance().reference

        Log.d("testest", getPreferer(this))

        getValuesFromDatabase({
            dataSnapshot = it


            // load needed url to determine if user is suitable
            webView.loadUrl(it.child(SPLASH_URL).value as String)
        }, {
            Log.d("SplashErrActivity", "didn't work fetchremote")
            progressBar.visibility = View.GONE
        })
    }
}