package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.TokenSet

class LcaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LcaLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return LcaFileType.INSTANCE
    }

    override fun toString(): String {
        return "Lca File"
    }

    fun getPackage(): PsiPackage {
        return node.findChildByType(LcaTypes.PACKAGE)?.psi as PsiPackage
    }

    fun getImports(): PsiImport {
        return node.findChildByType(LcaTypes.IMPORT)?.psi as PsiImport
    }
    
    fun getProducts(): Collection<PsiProduct> {
        return node.getChildren(TokenSet.create(LcaTypes.PRODUCT))
            .map { it.psi as PsiProduct }
    }

    fun getProcesses(): Collection<PsiProcess> {
        return node.getChildren(TokenSet.create(LcaTypes.PROCESS))
            .map { it.psi as PsiProcess }
    }

    fun getSystems(): Collection<PsiSystem> {
        return node.getChildren(TokenSet.create(LcaTypes.SYSTEM))
            .map { it.psi as PsiSystem }
    }

    fun getUnits(): Collection<PsiUnit> {
        return node.getChildren(TokenSet.create(LcaTypes.UNIT))
            .map { it.psi as PsiUnit }
    }

    fun getLocalAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }
}
