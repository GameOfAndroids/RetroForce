package com.tm78775.retroforce.model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * The onChanged function will only fire if the observed object has not been passed previously
 * to the observer parameter. This can pass null if the previous value was not null and then
 * the value becomes null.
 * @param lifecycleOwner The [LifecycleOwner].
 * @param observer The [Observer] which will be notified when the proper conditions are met.
 */
fun <T> LiveData<T>.distinctObserveOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        private var obj: T? = null

        override fun onChanged(t: T?) {
            if(obj != t) {
                obj = t
                observer.onChanged(obj)
            }
        }
    })
}