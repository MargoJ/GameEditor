package pl.margoj.editor.utils

import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock
import kotlin.reflect.KProperty

class SynchronizedVariable<T>(val lock: Lock, val default: T)
{
    private var realValue: T = default

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    {
        this.lock.withLock {
            @Suppress("UNCHECKED_CAST")
            return this.realValue as T
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    {
        this.lock.withLock {
            this.realValue = value
        }
    }
}