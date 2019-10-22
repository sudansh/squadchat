package com.squad.chat.utils

import android.os.SystemClock
import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
    private var doubleClickQualificationSpanInMillis: Long = 0
    private var timestampLastClick: Long = 0

    init {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN
        timestampLastClick = 0
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - timestampLastClick < doubleClickQualificationSpanInMillis) {
            onDoubleClick()
        } else {
            onSingleClick()
        }
        timestampLastClick = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick()
    abstract fun onSingleClick()

    companion object {

        // The time in which the second tap should be done in order to qualify as
        // a double click
        private const val DEFAULT_QUALIFICATION_SPAN: Long = 200
    }

}