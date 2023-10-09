package ch.kleis.lcaac.plugin.actions.csv

import ch.kleis.lcaac.plugin.imports.util.StringUtils
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaScaleQuantityExpression
import com.intellij.openapi.application.runReadAction
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Path


data class ModelProcess(
    val name: String,
    val labels: Map<String, String>,
    val variables: Map<String, String>,
    val products: List<String>,
    val inputs: List<String>,
    val emissions: List<String>,
    val resources: List<String>,
    val landuse: List<String>,
)

class OutputToModelWriter(
    private val processTemplate: LcaProcess,
    private val requests: List<CsvRequest>,
    private val path: Path
) {

    fun writeModels() {
        FileWriter(path.toFile(), StandardCharsets.UTF_8).use { writer ->
            val builder = StringBuilder()
            val file = processTemplate.containingFile as LcaFile
            builder.appendLine("package ${file.getPackageName()}.generated")
            builder.appendLine()
            builder.appendLine("import ${file.getPackageName()}")
            file.getImportNames()
                .filter { it != "builtin_units" }
                .forEach {
                    builder.appendLine("import $it")
                }
            builder.appendLine()
            builder.appendLine()
            writer.write(builder.toString())

            runReadAction {
                requests.forEach { writeModel(it, writer) }
            }
        }
    }

    private fun writeModel(request: CsvRequest, fileWriter: FileWriter) {
        val label = request["model"] ?: StringUtils.sanitize(processTemplate.name)
        val variables = processTemplate.variablesList
            .flatMap { it.assignmentList }
            .associate { it.name to it.dataExpressionList[1].text }
        val templateParamUnits = processTemplate.paramsList
            .flatMap { it.assignmentList }
            .associate { it.name to ((it.dataExpressionList[1] as LcaScaleQuantityExpression).dataExpression as LcaDataRef).uid.text }
        val params = request.columns()
            .mapNotNull { pName ->
                val unit = templateParamUnits[pName]
                unit?.let { pName to "${request[pName]} $it" }
            }
            .associate { it }
        val process = ModelProcess(
            name = "${request.processName}_gen",
            labels = processTemplate.getLabels() + mapOf("model" to label),
            variables = variables + params,
            products = processTemplate.blockProductsList
                .flatMap { it.technoProductExchangeList }
                .map { it.text },
            inputs = processTemplate.blockInputsList
                .flatMap { it.technoInputExchangeList }
                .map { it.text },
            resources = processTemplate.blockResourcesList
                .flatMap { it.bioExchangeList }
                .map { it.text },
            emissions = processTemplate.blockEmissionsList
                .flatMap { it.bioExchangeList }
                .map { it.text },
            landuse = processTemplate.blockLandUseList
                .flatMap { it.bioExchangeList }
                .map { it.text },
        )
        val processAsStr = serialize(process)
        fileWriter.write(processAsStr.toString())
    }
}


private fun serialize(process: ModelProcess): CharSequence {
    val builder = StringBuilder()
    val labels = process.labels.entries.map { """${it.key} = "${it.value}"""" }
    val variables = process.variables.entries.map { """${it.key} = ${it.value}""" }
    val blocks = listOf(
        "labels" to labels,
        "variables" to variables,
        "products" to process.products,
        "inputs" to process.inputs,
        "resources" to process.resources,
        "emissions" to process.emissions,
        "landUse" to process.landuse,
    )

    builder.appendLine("process ${process.name} {")
    blocks
        .filter { (_, l) -> l.isNotEmpty() }
        .forEach { (keyword, block) ->
            val blockContent = block.joinToString("\n").prependIndent()
            val exchangeBlock = "$keyword {\n$blockContent\n}".prependIndent()
            builder.appendLine(exchangeBlock)
            builder.appendLine()
        }
    builder.appendLine("}")
    builder.appendLine()

    return builder
}
