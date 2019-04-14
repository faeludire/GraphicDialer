package com.nezspencer.graphicdialer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nezspencer.callanalytics.AnalyticsHomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, AnalyticsHomeFragment.newInstance()).commit()
    }
}
