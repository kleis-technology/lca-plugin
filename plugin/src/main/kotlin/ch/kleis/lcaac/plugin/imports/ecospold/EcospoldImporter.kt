package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.EcospoldImportSettings
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.LCIASettings
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.UPRAndLCISettings
import ch.kleis.lcaac.plugin.imports.Imported
import ch.kleis.lcaac.plugin.imports.Importer
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.ecospold.lci.*
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.ecospold.model.Parser
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.shared.serializer.UnitRenderer
import ch.kleis.lcaac.plugin.imports.util.AsyncTaskController
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaac.plugin.imports.util.ImportInterruptedException
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.io.input.BOMInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class EcospoldImporter(
    private val settings: EcospoldImportSettings,
) : Importer() {

    companion object {
        private val LOG = Logger.getInstance(EcospoldImporter::class.java)

        fun unitToStr(u: String): String {
            return if (u != "metric ton*km") u else "ton*km"
        }

        fun getMethodNames(libFile: String): List<String> {
            val path = Path.of(libFile)
            try {
                SevenZFile(path.toFile()).use { f ->
                    val methodsFile = f.entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
                    f.getInputStream(methodsFile).use {
                        return Parser.readMethodName(it)
                    }
                }
            } catch (e: Exception) {
                return listOf("")
            }
        }
    }

    private var totalValue = 1
    private var currentValue = 0
    private val processRenderer = EcospoldProcessRenderer()
    private val methodName: String = when (settings) {
        is UPRAndLCISettings -> "Ecospold LCI library file."
        is LCIASettings -> settings.methodName
    }
    private val mapper = ToValue(BasicOperations)
    private val predefinedUnits = Prelude.unitMap<BasicNumber>().values
        .map { with(mapper) { it.toUnitValue() } }
        .associateBy { it.symbol.toString() }
    private val unitRenderer = UnitRenderer.of(predefinedUnits)

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val methodMapping =
            if (settings is UPRAndLCISettings && settings.mappingFile.isNotEmpty()) {
                buildMapping(watcher, settings)
            } else {
                null
            }

        val path = Path.of(settings.libraryFile)
        val pkg = settings.rootPackage.ifBlank { "default" }

        SevenZFile(path.toFile()).use { f ->
            ModelWriter(pkg, settings.rootFolder, builtinLibraryImports(settings), watcher).use { w ->
                importEntries(f, methodMapping, w, controller, watcher)
            }
        }
    }

    private fun buildMapping(watcher: AsynchronousWatcher, settings: UPRAndLCISettings): Map<String, MappingExchange> {
        watcher.notifyCurrentWork("Building requested method map")
        FileInputStream(settings.mappingFile).use {
            val bomIS = BOMInputStream(it)
            val isr = InputStreamReader(bomIS, StandardCharsets.UTF_8)

            return EcospoldMethodMapper.buildMapping(isr)
        }
    }

    private fun builtinLibraryImports(settings: EcospoldImportSettings): List<String> =
        if (settings is UPRAndLCISettings && settings.importBuiltinLibrary != null) {
            listOf(settings.importBuiltinLibrary.toString())
        } else listOf()

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectResults(): List<Imported> {
        return listOf(
            Imported(unitRenderer.nbUnit, "units"),
            Imported(processRenderer.nbProcesses, "processes"),
            Imported(processRenderer.nbProcesses, "substances"),
        )
    }

    private fun importEntries(
        f: SevenZFile,
        methodMapping: Map<String, MappingExchange>?,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        val start = Instant.now()
        totalValue = f.entries.count()

        val processDict = readProcessDict(f, f.entries)

        if (settings.importUnits) {
            importUnits(f.entries, f, writer)
        }

        val methodMappingFunction = methodMapping?.let { buildMethodMappingFunction(it) } ?: { it }
        val parsedEntries = f.entries.asFlow()
            .filter { it.hasStream() }
            .filter { it.name.endsWith(".spold") }
            .map {
                (it.name to importEntry(f.getInputStream(it), controller, watcher))
            }.map {
                methodMappingFunction(it)
            }.buffer()
            .flowOn(Dispatchers.Default)

        runBlocking {
            parsedEntries.collect { it: Pair<String, ActivityDataset> ->
                writeImportedDataset(it.second, processDict, writer, it.first)
            }
        }

        val duration = Duration.between(start, Instant.now())
        renderMain(writer, unitRenderer.nbUnit, processRenderer.nbProcesses, methodName, duration)
    }

    private fun buildMethodMappingFunction(
        methodMapping: Map<String, MappingExchange>,
    ): (Pair<String, ActivityDataset>) -> Pair<String, ActivityDataset> =
        { (fileName, activityDataset) ->
            fileName to activityDataset.copy(
                flowData = activityDataset.flowData.copy(
                    elementaryExchanges = activityDataset.flowData.elementaryExchanges.map { originalExchange ->
                        methodMapping[originalExchange.elementaryExchangeId]?.let { mapping ->
                            when (mapping) {
                                is OrphanMappingExchange, is UnkownMappingExchange -> originalExchange.copy(
                                    comment = originalExchange.comment?.let { it + "\n" + mapping.comment }
                                        ?: mapping.comment,
                                    printAsComment = true,
                                )

                                is FoundMappingExchange -> {
                                    originalExchange.copy(
                                        amount = mapping.conversionFactor?.let { it * originalExchange.amount }
                                            ?: originalExchange.amount,
                                        name = mapping.name ?: originalExchange.name,
                                        unit = mapping.unit ?: originalExchange.unit,
                                        compartment = mapping.compartment ?: originalExchange.compartment,
                                        subCompartment = mapping.subCompartment
                                            ?: originalExchange.subCompartment?.ifEmpty { null },
                                        comment = originalExchange.comment?.let { it + "\n" + mapping.comment }
                                            ?: mapping.comment
                                    )
                                }
                            }
                        } ?: originalExchange
                    })
            )
        }

    private fun importUnits(
        entries: Iterable<SevenZArchiveEntry>,
        f: SevenZFile,
        writer: ModelWriter
    ) {
        val unitConversionFile = entries.firstOrNull { it.name.endsWith("UnitConversions.xml") }
        val fromMeta = f.getInputStream(unitConversionFile).use {
            val unitConversions = Parser.readUnits(it)
            unitConversions.asSequence()
                .map { u ->
                    ImportedUnit(
                        u.dimension, u.fromUnit, u.factor,
                        unitToStr(u.toUnit)
                    )
                }
                .filter { u -> u.name != "foot-candle" }
        }

        val methodsFile = entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
        val fromMethod = f.getInputStream(methodsFile).use {
            val unitConversions = Parser.readMethodUnits(it, methodName)
            unitConversions.asSequence()
                .map { u ->
                    ImportedUnit(
                        u.dimension, u.fromUnit, u.factor,
                        unitToStr(u.toUnit)
                    )
                }
                .filter { u -> u.name != "foot-candle" }
                .filter { u -> !predefinedUnits.containsKey(u.name) }
        }

        (fromMeta + fromMethod)
            .distinctBy { it.name }
            .forEach { unitRenderer.render(it, writer) }
    }

    private fun renderMain(
        writer: ModelWriter,
        nbUnits: Int,
        nbProcess: Int,
        methodName: String,
        duration: Duration
    ) {
        val s = duration.seconds
        val durAsStr = String.format("%02dm %02ds", s / 60, (s % 60))
        val block = """
            Import Method: $methodName
            Date: ${ZonedDateTime.now()}
            Import Summary:
                * $nbUnits units
                * $nbProcess processes
                * $nbProcess substances
            Duration: $durAsStr
        """.trimIndent()

        writer.write("main", ModelWriter.pad(ModelWriter.asComment(block), 0), false)
    }

    data class ProcessDictRecord(
        val processId: String,
        val fileName: String,
        val processName: String,
        val geo: String,
        val productName: String
    )

    private fun readProcessDict(
        f: SevenZFile,
        entries: Iterable<SevenZArchiveEntry>
    ): Map<String, ProcessDictRecord> {
        val dictEntry = entries.first { it.name.endsWith("FilenameToActivityLookup.csv") }
        val csvFormat = CSVFormat.Builder.create().setDelimiter(";").setHeader().build()
        val records = CSVParser.parse(f.getInputStream(dictEntry), Charset.defaultCharset(), csvFormat)
        return records.map {
            ProcessDictRecord(
                it["Filename"].substring(0, it["Filename"].indexOf("_")),
                it["Filename"],
                it["ActivityName"],
                it["Location"],
                it["ReferenceProduct"],
            )
        }.associateBy { it.processId }
    }

    private fun importEntry(
        input: InputStream,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ): ActivityDataset {
        if (!controller.isActive()) throw ImportInterruptedException()
        val activityDataset: ActivityDataset = Parser.readDataset(input)

        currentValue++
        watcher.notifyProgress((100.0 * currentValue / totalValue).roundToInt())
        return activityDataset
    }

    private fun writeImportedDataset(
        dataSet: ActivityDataset,
        processDict: Map<String, ProcessDictRecord>,
        w: ModelWriter,
        path: String
    ) {
        LOG.info("Read dataset from $path")
        processRenderer.render(dataSet, w, processDict, "from $path", methodName)
    }
}