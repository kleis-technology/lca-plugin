package ch.kleis.lcaac.plugin.imports.util

interface AsynchronousWatcher {

    fun notifyProgress(percent: Int)
    fun notifyCurrentWork(current: String)
}
