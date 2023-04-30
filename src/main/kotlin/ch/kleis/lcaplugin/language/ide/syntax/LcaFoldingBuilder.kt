package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.PsiParameters
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiVariables
import ch.kleis.lcaplugin.language.psi.type.block.*
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class LcaFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val blocks = PsiTreeUtil.findChildrenOfAnyType(
            root,
            PsiProcess::class.java,
            PsiParameters::class.java,
            PsiVariables::class.java,
            PsiBlockProducts::class.java,
            PsiBlockInputs::class.java,
            PsiBlockEmissions::class.java,
            PsiBlockResources::class.java,
            PsiBlockImpacts::class.java,
            PsiUnitDefinition::class.java,
        )
        blocks.forEach { block ->
            val braces = PsiTreeUtil.collectElements(block) {
                it.elementType == LcaTypes.token(LcaLangLexer.LBRACE) || it.elementType == LcaTypes.token(LcaLangLexer.RBRACE)
            }

            if (braces.size > 1) {
                descriptors.add(
                    FoldingDescriptor(
                        block,
                        TextRange(braces[0].textOffset, braces[braces.size - 1].textOffset + 1)
                    )
                )
            }
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
