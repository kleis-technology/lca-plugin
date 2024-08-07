package ch.kleis.lcaac.plugin.language.ide.style

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaTypes.*
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
            PARAMS, LABELS, VARIABLES, LABEL_ASSIGNMENT, ASSIGNMENT, GLOBAL_ASSIGNMENT,
            BLOCK_INPUTS, BLOCK_PRODUCTS, BLOCK_RESOURCES, BLOCK_EMISSIONS, BLOCK_IMPACTS, BLOCK_META, BLOCK_LAND_USE,
            TECHNO_PRODUCT_EXCHANGE, TECHNO_INPUT_EXCHANGE, BIO_EXCHANGE, IMPACT_EXCHANGE,
            NAME_FIELD, SYMBOL_FIELD, DIM_FIELD, REFERENCE_UNIT_FIELD, TYPE_FIELD, COMPARTMENT_FIELD, SUB_COMPARTMENT_FIELD,
            META_ASSIGNMENT, ALIAS_FOR_FIELD,
            COMMENT_LINE, COMMENT_BLOCK_START, COMMENT_CONTENT, COMMENT_BLOCK_END,
            ASSERT, GIVEN, RANGE_ASSERTION,
            SCHEMA_DEFINITION, COLUMN_DEFINITION, LOCATION_FIELD,
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
