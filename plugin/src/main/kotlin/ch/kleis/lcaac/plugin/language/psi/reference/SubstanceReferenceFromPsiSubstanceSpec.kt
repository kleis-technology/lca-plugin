package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceKeyIndex
import ch.kleis.lcaac.plugin.language.psi.type.PsiSubstance
import ch.kleis.lcaac.plugin.psi.LcaSubstanceSpec
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex

class SubstanceReferenceFromPsiSubstanceSpec(
    element: LcaSubstanceSpec
) : PsiReferenceBase<LcaSubstanceSpec>(element), PsiPolyVariantReference {
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
        val type = element.getType()?.value ?: return emptyArray()
        val compartment = element.getCompartmentField()?.getValue() ?: return emptyArray()

        val candidateFqns = allPkgNames.map {
            "$it.${element.name}"
        }

        val subCompartment = element.getSubCompartmentField()?.getValue()

        fun findSubstances(subCompartment: String?): (String) -> Collection<PsiSubstance> = { fqn: String ->
            SubstanceKeyIndex.Util.findSubstances(
                project,
                fqn, type, compartment, subCompartment,
            )
        }

        return if (subCompartment != null) {
            candidateFqns
                .flatMap(findSubstances(subCompartment))
                .ifEmpty { candidateFqns.flatMap(findSubstances(null)) }
        } else {
            candidateFqns.flatMap(findSubstances(null))
        }.map(::PsiElementResolveResult).toTypedArray()

    }

    override fun getVariants(): Array<Any> {
        val type = element.getType()?.value ?: return emptyArray()
        val allKeys = StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.SUBSTANCES, project)
        return allKeys
            .filter { key ->
                key.type == type
                        && allPkgNames.any {
                    key.getPackageName().startsWith(it)
                }
            }
            .map { LookupElementBuilder.create(it.getDisplayName()) }
            .toTypedArray()
    }
}
