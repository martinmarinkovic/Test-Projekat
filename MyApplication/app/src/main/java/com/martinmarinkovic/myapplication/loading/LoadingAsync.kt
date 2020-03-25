package com.martinmarinkovic.myapplication.loading

import android.os.AsyncTask


interface LoadingImplementation {
    fun onFinishedLoading()
}

class LoadingAsync(private val listener: LoadingImplementation) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        for (i in 0 until 5) {
            Thread.sleep(1000)
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        listener.onFinishedLoading()
    }
}
