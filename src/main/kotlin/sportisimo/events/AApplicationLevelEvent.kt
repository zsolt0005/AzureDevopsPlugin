package sportisimo.events

abstract class AApplicationLevelEvent<T>: AEvent<T>()
{
    fun fire()
    {
        fire(null)
    }

    fun fire(result: T?)
    {
        listeners.forEach {
            if(it.first.isDisposed)
            {
                unregister(it.first, it.second.first)
                return
            }

            it.second.second.onChange(result)
        }
    }
}