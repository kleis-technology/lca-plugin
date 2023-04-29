package ch.kleis.lcaplugin.language.ide.style

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.ALIAS_FOR_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.ASSIGNMENT
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BIO_EXCHANGE
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_EMISSIONS
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_IMPACTS
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_INPUTS
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_LAND_USE
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_META
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_PRODUCTS
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.BLOCK_RESOURCES
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.COMPARTMENT_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.DIM_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.GLOBAL_ASSIGNMENT
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.IMPACT_EXCHANGE
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.META_ASSIGNMENT
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.NAME_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.PARAMS
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.REFERENCE_UNIT_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.SUB_COMPARTMENT_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.SYMBOL_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.TECHNO_INPUT_EXCHANGE
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.TECHNO_PRODUCT_EXCHANGE
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.TYPE_FIELD
import ch.kleis.lcaplugin.psi.LcaElementTypes.Companion.VARIABLES
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_BLOCK_END
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_BLOCK_START
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_CONTENT
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_LINE
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet.*

class LcaIndentBlock(node: ASTNode, private val spaceBuilder: SpacingBuilder) :
    AbstractBlock(node, null, Alignment.createAlignment()) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spaceBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.getChildren(ANY).isNotEmpty()
    }

    override fun getIndent(): Indent? {
        if (node.treeParent?.psi is LcaFile) {
            return Indent.getNoneIndent()
        }

        return when (node.elementType) {
            PARAMS, VARIABLES, ASSIGNMENT, GLOBAL_ASSIGNMENT,
            BLOCK_INPUTS, BLOCK_PRODUCTS, BLOCK_RESOURCES, BLOCK_EMISSIONS, BLOCK_IMPACTS, BLOCK_META, BLOCK_LAND_USE,
            TECHNO_PRODUCT_EXCHANGE, TECHNO_INPUT_EXCHANGE, BIO_EXCHANGE, IMPACT_EXCHANGE,
            NAME_FIELD, SYMBOL_FIELD, DIM_FIELD, REFERENCE_UNIT_FIELD, TYPE_FIELD, COMPARTMENT_FIELD, SUB_COMPARTMENT_FIELD,
            META_ASSIGNMENT, ALIAS_FOR_FIELD,
            COMMENT_LINE, COMMENT_BLOCK_START, COMMENT_CONTENT, COMMENT_BLOCK_END
            -> Indent.getNormalIndent(true)

            else -> Indent.getNoneIndent()
        }
    }

    override fun buildChildren(): List<Block> {
        return myNode.getChildren(andNot(ANY, WHITE_SPACE)).map {
            LcaIndentBlock(
                it,
                spaceBuilder
            )
        }
    }
}
