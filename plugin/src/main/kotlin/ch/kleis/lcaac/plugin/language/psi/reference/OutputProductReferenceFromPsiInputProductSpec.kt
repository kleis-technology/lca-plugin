package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.output_product.OutputProductStubKeyIndex
import ch.kleis.lcaac.plugin.language.type_checker.LcaMatchLabelsEvaluator
import ch.kleis.lcaac.plugin.psi.LcaInputProductSpec
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class OutputProductReferenceFromPsiInputProductSpec(
    element: LcaInputProductSpec
) : PsiReferenceBase<LcaInputProductSpec>(element), PsiPolyVariantReference {
    private val project = element.project
    private val file = element.containingFile as LcaFile
    private val pkgName = file.getPackageName()
    private val imports = file.getImportNames()
    private val allPkgNames = listOf(pkgName).plus(imports)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val candidateFqns = allPkgNames.map {
            "$it.${element.name}"
        }
        val candidateOutputProducts =
            candidateFqns.flatMap { fqn -> OutputProductStubKeyIndex.Util.findOutputProducts(project, fqn) }
        if (element.getProcessTemplateSpec() == null) {
            return candidateOutputProducts.map(::PsiElementResolveResult).toTypedArray()
        }

        val processName = element.getProcessTemplateSpec()?.name
        val matchLabels =
            element.getProcessTemplateSpec()?.getMatchLabels()?.let { LcaMatchLabelsEvaluator().evalOrNull(it) }
                ?: emptyMap()
        return candidateOutputProducts.filter {
            val process = it.getContainingProcess()
            (processName == null || process.name == processName) && process.getLabels() == matchLabels
        }.map(::PsiElementResolveResult).toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        return StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.OUTPUT_PRODUCTS, project).filter { key ->
            allPkgNames.any {
                allPkgNames.any {
                    val parts = key.split(".")
                    val prefix = parts.take(parts.size - 1).joinToString(".")
                    prefix.startsWith(it)
                }
            }
        }.flatMap { OutputProductStubKeyIndex.Util.findOutputProducts(project, it) }.map {
            val process = it.getContainingProcess().name
            val labels = it.getContainingProcess().getLabels()
                .map { entry -> "${entry.key} = \"${entry.value}\"" }
                .joinToString(", ")
            if (labels.isBlank()) "${it.name} from $process"
            else "${it.name} from $process match (${labels})"
        }.map { LookupElementBuilder.create(it) }.toTypedArray()
    }
}
