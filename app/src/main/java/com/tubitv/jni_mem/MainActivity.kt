package com.tubitv.jni_mem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tubitv.native.MemBench

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MemBench.benchGetResultFromJNI()
    }
}