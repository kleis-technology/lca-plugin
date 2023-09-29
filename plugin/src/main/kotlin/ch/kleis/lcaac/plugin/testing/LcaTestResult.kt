package ch.kleis.lcaac.plugin.testing

sealed interface LcaTestResult
object LcaTestSuccess : LcaTestResult
object LcaTestFailure : LcaTestResult

