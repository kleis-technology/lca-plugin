package ch.kleis.lcaac.plugin.imports

import ch.kleis.lcaac.plugin.imports.ecospold.EcospoldImporter
import ch.kleis.lcaac.plugin.imports.util.AsyncTaskController
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaac.plugin.imports.util.ImportInterruptedException
import com.intellij.openapi.diagnostic.Logger
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit

abstract class Importer {
    companion object {
        private val LOG = Logger.getInstance(EcospoldImporter::class.java)
    }

    private val begin = Instant.now()

    fun import(controller: AsyncTaskController, watcher: AsynchronousWatcher): Summary {
        return try {
            importAll(controller, watcher)
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            SummaryInSuccess(duration, collectResults())
        } catch (e: ImportInterruptedException) {
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            SummaryInterrupted(duration, collectResults())
        } catch (e: Exception) {
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            LOG.warn("Unexpected error during import", e)
            SummaryInError(duration, collectResults(), e.message ?: "")
        }
    }

    protected abstract fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher)

    protected abstract fun collectResults(): List<Imported>
    abstract fun getImportRoot(): Path

}
