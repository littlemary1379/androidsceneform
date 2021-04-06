package com.mary.myapplication.util.event

import java.lang.ref.WeakReference
import java.util.*

class EventCenter private constructor() {
    private val map = HashMap<String, ArrayList<EventObserver>>()

    interface EventRunnable {
        fun run(arrow: String?, poster: Any?, data: HashMap<String?, Any?>?)
    }

    inner class EventObserver internal constructor(
        var arrow: String,
        obj: Any,
        runnable: EventRunnable?
    ) {
        var objectWeakReference: WeakReference<Any>
        var runnable: EventRunnable?

        init {
            objectWeakReference = WeakReference(obj)
            this.runnable = runnable
        }
    }

    fun addEventObserver(arrow: String, obj: Any, runnable: EventRunnable?) {
        val eventObserver = EventObserver(arrow, obj, runnable)
        getObserverListForArrows(arrow).add(eventObserver)
    }

    fun removeAllObserver(obj: Any) {
        val deleteList = ArrayList<EventObserver>()
        for (arrayList in map.values) {
            deleteList.clear()
            for (observer in arrayList) {
                if (observer.objectWeakReference.get() === obj) {
                    deleteList.add(observer)
                }
            }
            arrayList.removeAll(deleteList)
        }
    }

    fun removeObserverForArrows(arrow: String, obj: Any) {
        val result = getObserverListForArrows(arrow)
        val deleteList = ArrayList<EventObserver>()
        var `object`: Any
        for (eventObserver in result) {
            `object` = eventObserver.objectWeakReference.get()!!
            if (`object` === obj) {
                deleteList.add(eventObserver)
            }
        }
        for (eventObserver in deleteList) {
            result.remove(eventObserver)
        }
    }

    fun sendEvent(arrow: String, sender: Any?, data: HashMap<String?, Any?>?) {
        val result = getObserverListForArrows(arrow)
        for (eventObserver in result) {
            if (eventObserver.runnable != null) {
                eventObserver.runnable!!.run(arrow, sender, data)
            }
        }
    }

    private fun getObserverListForArrows(arrow: String): ArrayList<EventObserver> {
        var result = map[arrow]
        if (result == null) {
            result = ArrayList()
            map[arrow] = result
        }
        return result
    }

    companion object {
        var instance = EventCenter()
    }
}