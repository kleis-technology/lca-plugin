package ch.kleis.lcaac.plugin.imports.simapro

import ch.kleis.lcaac.plugin.ide.imports.simapro.SimaproImportSettings
import ch.kleis.lcaac.plugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaac.plugin.imports.Imported
import ch.kleis.lcaac.plugin.imports.Importer
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.model.ImportedUnitAliasFor
import ch.kleis.lcaac.plugin.imports.shared.UnitManager
import ch.kleis.lcaac.plugin.imports.shared.UnitRenderer
import ch.kleis.lcaac.plugin.imports.simapro.substance.SimaproSubstanceRenderer
import ch.kleis.lcaac.plugin.imports.util.AsyncTaskController
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaac.plugin.imports.util.ImportInterruptedException
import ch.kleis.lcaac.plugin.imports.util.StringUtils.asComment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.CountingInputStream
import org.openlca.simapro.csv.CsvBlock
import org.openlca.simapro.csv.SimaProCsv
import org.openlca.simapro.csv.refdata.SystemDescriptionBlock
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt


class SimaproImporter(
    private val settings: SimaproImportSettings
) : Importer() {
    private var totalValue = 1L

    companion object {
        private val LOG = Logger.getInstance(SimaproImporter::class.java)
    }

    private val processRenderer = SimaproProcessRenderer(settings.importSubstancesMode)
    private val simaproSubstanceRenderer = SimaproSubstanceRenderer()
    private val inputParameterRenderer = InputParameterRenderer()
    private var counting: CountingInputStream? = null
    private val unitManager = UnitManager()

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val path = Path.of(settings.libraryFile)

        val pkg = settings.rootPackage.ifBlank { "default" }
        val fileHeaderImports = when (settings.importSubstancesMode) {
            SubstanceImportMode.EF30 -> listOf("ef30")
            SubstanceImportMode.EF31 -> listOf("ef31")
            SubstanceImportMode.SIMAPRO, SubstanceImportMode.NOTHING -> listOf()
        }
        val writer = ModelWriter(pkg, settings.rootFolder, fileHeaderImports, watcher)

        importFile(path, writer, controller, watcher)
    }

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectResults(): List<Imported> {
        return listOf(
            Imported(unitManager.numberOfAddInvocations, "units"),
            Imported(inputParameterRenderer.nbParameters, "parameters"),
            Imported(processRenderer.nbProcesses, "processes"),
            Imported(simaproSubstanceRenderer.nbSubstances, "substances")
        )
    }

    private fun importFile(
        path: Path,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        val file = path.toFile()
        totalValue = file.length()
        val input = file.inputStream()
        input.use {
            val countingVal = CountingInputStream(input)
            val realInput: InputStream = when {
                file.path.endsWith(".gz") -> GZIPInputStream(countingVal)
                file.path.endsWith(".zip") -> {
                    val zip = ZipInputStream(countingVal); zip.nextEntry; zip
                }

                else -> countingVal
            }
            counting = countingVal
            val reader = InputStreamReader(realInput)
            SimaProCsv.read(reader) { block: CsvBlock ->
                importBlock(block, writer, controller, watcher)
            }
        }
    }


    private fun importBlock(
        block: CsvBlock,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        val read = counting?.bytesRead ?: 0L
        watcher.notifyProgress((100.0 * read / totalValue).roundToInt())

        if (!controller.isActive()) throw ImportInterruptedException()
        val unitRenderer = UnitRenderer(unitManager)
        when {
            block.isProcessBlock && settings.importProcesses -> processRenderer.render(
                block.asProcessBlock(),
                writer
            )

            block.isElementaryFlowBlock ->
                if (settings.importSubstancesMode == SubstanceImportMode.SIMAPRO)
                    simaproSubstanceRenderer.render(block.asElementaryFlowBlock(), writer)

            block.isUnitBlock && settings.importUnits -> block.asUnitBlock().units()
                .map {
                    ImportedUnit(it.quantity().lowercase(), it.name(), ImportedUnitAliasFor(it.conversionFactor(), it.referenceUnit()))
                }
                .forEach { unitRenderer.render(it, writer) }

            block.isInputParameterBlock -> inputParameterRenderer.render(block.asInputParameterBlock(), writer)
            block.isSystemDescriptionBlock -> renderMain(block.asSystemDescriptionBlock(), writer)
            block.isQuantityBlock -> { /* Dimensions => no need */
            }

            block.isCalculatedParameterBlock -> {/* Ecoinvent => empty*/
            }

            block.isProductStageBlock -> { /* Ecoinvent => empty */
            }

            block.isImpactMethodBlock -> { /* Ecoinvent => empty */
            }

            else -> LOG.warn("Missing case for ${block.javaClass}")
        }
    }

    private fun renderMain(block: SystemDescriptionBlock?, writer: ModelWriter) {
        block?.let {
            writer.writeAppendFile("main", asComment(block.name()))
            writer.writeAppendFile("main", asComment(block.description()))
        }
    }


}
