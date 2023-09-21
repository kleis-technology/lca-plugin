package ch.kleis.lcaac.plugin.language.psi

import ch.kleis.lcaac.plugin.LcaFileType
import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

class LcaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LcaLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return LcaFileType.INSTANCE
    }

    override fun toString(): String {
        return "Lca File"
    }

    fun getPackageName(): String {
        return PsiTreeUtil.getChildOfType(this, LcaPackage::class.java)?.name
            ?: "default"
    }

    private fun getImports(): Collection<LcaImport> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaImport::class.java)
    }

    fun getImportNames(): Collection<String> {
        return listOf(Prelude.pkgName) + getImports().map { it.name }
    }

    fun getProcesses(): Collection<LcaProcess> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaProcess::class.java)
    }

    fun getSubstances(): Collection<LcaSubstance> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaSubstance::class.java)
    }

    fun getGlobalAssignments(): Collection<Pair<String, LcaDataExpression>> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaGlobalVariables::class.java)
            .flatMap {
                it.globalAssignmentList
                    .map { a -> a.getDataRef().name to a.getValue() }
            }
    }

    fun getUnitDefinitions(): Collection<LcaUnitDefinition> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaUnitDefinition::class.java)
    }

    fun getBlocksOfGlobalVariables(): Collection<LcaGlobalVariables> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaGlobalVariables::class.java)
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getBlocksOfGlobalVariables()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }
}
