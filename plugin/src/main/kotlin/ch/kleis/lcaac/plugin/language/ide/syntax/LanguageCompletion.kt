package ch.kleis.lcaac.plugin.language.ide.syntax

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiErrorElement


class LanguageCompletion : CompletionContributor() {


    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (parameters.position.parent is PsiErrorElement) {
            val parent = parameters.position.parent as PsiErrorElement
            result.addElements(*extractKeyWordFromError(parent).toTypedArray())
        }
    }


    private fun extractKeyWordFromError(elt: PsiErrorElement): List<String> {
        val description = elt.errorDescription
        val matchingSuggestions = keys.filter {
            description.contains(it)
        }.mapNotNull { suggestions[it] }.flatten()
        val suggestionsToRemove = matchingSuggestions
            .map {
                matchingSuggestions.filter { suggestion ->
                    it != suggestion && it.contains(suggestion) }
            }.flatten().toSet()
        return matchingSuggestions.minus(suggestionsToRemove)
    }


    private val keys =
        listOf(
            "meta", // All Blocks
            "unit", "process", "substance", "import", "package", "variables", // Root
            "name", "type", "compartment", "sub_compartment", "reference_unit", "impacts", // Substance block
            "Emission", "Resource", "Land_use", // Substance types
            "description", "author", "other", // Meta default keys
            "reference_unit", "symbol", "dimension", "alias_for", // Unit block
            "variables", "params", "labels", // Process Block
            "products", "inputs", "resources", "emissions", "land_use", "impacts", // Process SubBlocks
            "test", "given", "assert", "between", "and", // Test blocks
            "datasource", "schema", "location", // Data source blocks
            "for_each", // For each
            "sum", "lookup", "default_record" // Primitives
        )
    private val suggestions: Map<String, List<String>> =
        keys.associateWith { listOf(it) }
            .plus(
                "process" to listOf("process", "@cached")
            )

    private fun CompletionResultSet.addElements(vararg strings: String) {
        strings.forEach { this.addElement(LookupElementBuilder.create(it)) }
    }


}
