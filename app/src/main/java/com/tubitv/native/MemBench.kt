package com.tubitv.native

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.system.measureTimeMillis

interface ApiResultReceiver {
    fun onApiResult(strTime: Int, result: ByteBuffer) {}
    fun onApiStringResult(strTime: Int, result: String) {}
}

object MemBench {
    init {
        System.loadLibrary("jellyfish")
    }

    private const val TAG = "MemBench"
    private const val ITERATE_COUNTS = 10
    private const val KB = 1000
    private const val MB = KB * KB

    private val stringSize = arrayOf(10 * KB, 50 * KB, 100 * KB, 500 * KB, 1 * MB, 2 * MB, 4 * MB, 8 * MB)

    fun benchGetResultFromJNI() {
        GlobalScope.launch(Dispatchers.IO) {
            var totalTime = 0
            Log.i(TAG, "benchGetResultFromJNI")
            for (i in stringSize) {
                var strGenTime = 0
                val execTime = measureTimeMillis {
                    nativeGetResultViaDirect(i, object: ApiResultReceiver{
                        override fun onApiResult(strTime: Int, result: ByteBuffer) {
                            strGenTime = strTime
                            val s = StandardCharsets.UTF_8.decode(result).toString()
                            Log.i(TAG, "s= ${s.length}.")
                        }
                    })
                }
                val transTime = execTime.toInt() - strGenTime
                Log.i(TAG, "====> Direct Size ${i / KB} ${execTime}ms, transTime:${transTime}ms.")
            }
            //Log.i(TAG, "nativeGetResultViaReturnString AVG transmision ${totalTime/ITERATE_COUNTS}ms.")
            Log.i(TAG, "")

            totalTime = 0
            for (i in stringSize) {
                var strGenTime = 0
                val execTime = measureTimeMillis {
                    nativeGetResultViaReturnString(i, object: ApiResultReceiver{
                        override fun onApiStringResult(strTime: Int, result: String) {
                            strGenTime = strTime
                            //Log.i(TAG, "s= ${result.subSequence(0, 10)}.")
                        }
                    })
                }
                val transTime = execTime.toInt() - strGenTime
                Log.i(TAG, "====>  ReturnString ${execTime}ms, transTime:$transTime.")
                totalTime += transTime
            }
            Log.i(TAG, "nativeGetResultViaReturnString AVG transmision ${totalTime/ITERATE_COUNTS}ms.")
        }
    }

    private external fun nativeGetResultViaDirect(stringLen: Int, callback: ApiResultReceiver)
    private external fun nativeGetResultViaReturnString(stringLen: Int, callback: ApiResultReceiver)
}
