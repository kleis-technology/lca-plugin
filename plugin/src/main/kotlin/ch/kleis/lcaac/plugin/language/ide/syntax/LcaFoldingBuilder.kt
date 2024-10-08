package ch.kleis.lcaac.plugin.language.ide.syntax

import ch.kleis.lcaac.plugin.psi.*
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
            LcaBlockEmissions::class.java,
            LcaBlockImpacts::class.java,
            LcaBlockInputs::class.java,
            LcaBlockLandUse::class.java,
            LcaBlockMeta::class.java,
            LcaBlockProducts::class.java,
            LcaBlockResources::class.java,
            LcaParams::class.java,
            LcaProcess::class.java,
            LcaUnitDefinition::class.java,
            LcaVariables::class.java,
        )
        blocks.forEach { block ->
            val braces = PsiTreeUtil.collectElements(block) {
                it.elementType == LcaTypes.LBRACE || it.elementType == LcaTypes.RBRACE
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

    override fun getPlaceholderText(node: ASTNode): String {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
