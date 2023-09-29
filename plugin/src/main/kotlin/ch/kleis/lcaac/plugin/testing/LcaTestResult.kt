package ch.kleis.lcaac.plugin.testing

sealed interface LcaTestResult

object LcaTestSuccess : LcaTestResult {
    override fun toString(): String = "Success"
}

object LcaTestFailure : LcaTestResult {
    override fun toString(): String = "Failure"
}
