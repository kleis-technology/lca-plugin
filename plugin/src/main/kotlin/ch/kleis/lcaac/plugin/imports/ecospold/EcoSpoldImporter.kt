package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.EcoSpoldImportSettings
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.LCIASettings
import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.UPRSettings
import ch.kleis.lcaac.plugin.imports.Imported
import ch.kleis.lcaac.plugin.imports.Importer
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.ecospold.lci.*
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.ecospold.model.Parser
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.model.ImportedUnitAliasFor
import ch.kleis.lcaac.plugin.imports.shared.UnitManager
import ch.kleis.lcaac.plugin.imports.shared.UnitRenderer
import ch.kleis.lcaac.plugin.imports.util.AsyncTaskController
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaac.plugin.imports.util.ImportInterruptedException
import ch.kleis.lcaac.plugin.imports.util.StringUtils
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

class EcoSpoldImporter(
    private val settings: EcoSpoldImportSettings,
) : Importer() {

    companion object {
        private val LOG = Logger.getInstance(EcoSpoldImporter::class.java)

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
    private val processRenderer = EcoSpoldProcessRenderer()
    private val methodName: String = when (settings) {
        is UPRSettings -> "EcoSpold LCI library file."
        is LCIASettings -> settings.methodName
    }
    private val unitManager = UnitManager()

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val methodMapping =
            if (settings is UPRSettings && settings.mappingFile.isNotEmpty()) {
                buildMapping(watcher, settings)
            } else {
                null
            }

        val path = Path.of(settings.libraryFile)
        val pkg = settings.rootPackage.ifBlank { "default" }
        val writer = ModelWriter(pkg, settings.rootFolder, builtinLibraryImports(settings), watcher)

        SevenZFile(path.toFile()).use { f ->
            importEntries(f, methodMapping, writer, controller, watcher)
        }
    }

    private fun buildMapping(watcher: AsynchronousWatcher, settings: UPRSettings): Map<String, MappingExchange> {
        watcher.notifyCurrentWork("Building requested method map")
        FileInputStream(settings.mappingFile).use {
            val bomIS = BOMInputStream(it)
            val isr = InputStreamReader(bomIS, StandardCharsets.UTF_8)

            return EcospoldMethodMapper.buildMapping(isr)
        }
    }

    private fun builtinLibraryImports(settings: EcoSpoldImportSettings): List<String> =
        if (settings is UPRSettings && settings.importBuiltinLibrary != null) {
            listOf(settings.importBuiltinLibrary.toString())
        } else listOf()

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectResults(): List<Imported> {
        return listOf(
            Imported(unitManager.getNumberOfAddInvocations(), "units"),
            Imported(processRenderer.nbProcesses, "processes"),
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

        // must happen before activity parsing because of #348.
        importUnits(f.entries, f, writer)

        val methodMappingFunction = methodMapping?.let { buildMethodMappingFunction(it) } ?: { it }
        val parsedEntries = f.entries.asFlow()
            .filter { it.hasStream() }
            .filter { it.name.endsWith(".spold") }
            .map {
                (it.name to parseEntry(f.getInputStream(it), controller, watcher))
            }.map {
                methodMappingFunction(it)
            }.buffer()
            .flowOn(Dispatchers.Default)

        val knownUnitSymbols = emptySet<String>() // TODO: EcoSpoldManager
        runBlocking {
            parsedEntries.collect { it: Pair<String, ActivityDataset> ->
                writeImportedDataset(it.second, processDict, knownUnitSymbols, writer, it.first)
            }
        }

        val duration = Duration.between(start, Instant.now())
        renderMain(writer, unitManager.getNumberOfAddInvocations(), processRenderer.nbProcesses, methodName, duration)
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
                                is OrphanMappingExchange, is UnknownMappingExchange -> originalExchange.copy(
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
            val unitConversions = Parser.readUnitConversions(it)
            unitConversions.asSequence()
                .map { u ->
                    ImportedUnit(
                        u.dimension,
                        u.fromUnit,
                        ImportedUnitAliasFor(
                            u.factor,
                            u.toUnit,
                        ),
                    )
                }
        }

        val methodsFile = entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
        val fromMethod = f.getInputStream(methodsFile).use {
            val indicators = Parser.readIndicators(it, methodName)
            indicators.asSequence()
                .map { indicator ->
                    ImportedUnit(
                        indicator.name,
                        indicator.unitName,
                    )
                }
        }

        val unitRenderer = UnitRenderer(unitManager)
        (fromMeta + fromMethod)
            .distinctBy { it.symbol }
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
            Duration: $durAsStr
        """.trimIndent()

        writer.writeFile("main", StringUtils.asComment(block))
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

    private fun parseEntry(
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
        dataset: ActivityDataset,
        processDict: Map<String, ProcessDictRecord>,
        knownUnits: Set<String>,
        writer: ModelWriter,
        path: String
    ) {
        LOG.info("Read dataset from $path")
        processRenderer.render(
            data = dataset,
            processDict = processDict,
            knownUnits = knownUnits,
            writer = writer,
            processComment = "from $path",
            methodName = methodName
        )
    }
}
