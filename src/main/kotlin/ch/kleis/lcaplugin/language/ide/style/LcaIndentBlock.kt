package ch.kleis.lcaplugin.language.ide.style

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.LcaFile
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
            LcaTypes.rule(LcaLangParser.RULE_params),
            LcaTypes.rule(LcaLangParser.RULE_variables),
            LcaTypes.rule(LcaLangParser.RULE_assignment),
            LcaTypes.rule(LcaLangParser.RULE_globalAssignment),
            LcaTypes.rule(LcaLangParser.RULE_block_inputs),
            LcaTypes.rule(LcaLangParser.RULE_block_products),
            LcaTypes.rule(LcaLangParser.RULE_block_resources),
            LcaTypes.rule(LcaLangParser.RULE_block_emissions),
            LcaTypes.rule(LcaLangParser.RULE_block_impacts),
            LcaTypes.rule(LcaLangParser.RULE_block_meta),
            LcaTypes.rule(LcaLangParser.RULE_block_land_use),
            LcaTypes.rule(LcaLangParser.RULE_technoProductExchange),
            LcaTypes.rule(LcaLangParser.RULE_technoInputExchange),
            LcaTypes.rule(LcaLangParser.RULE_bioExchange),
            LcaTypes.rule(LcaLangParser.RULE_impactExchange),
            LcaTypes.rule(LcaLangParser.RULE_nameField),
            LcaTypes.rule(LcaLangParser.RULE_symbolField),
            LcaTypes.rule(LcaLangParser.RULE_dimField),
            LcaTypes.rule(LcaLangParser.RULE_referenceUnitField),
            LcaTypes.rule(LcaLangParser.RULE_typeField),
            LcaTypes.rule(LcaLangParser.RULE_compartmentField),
            LcaTypes.rule(LcaLangParser.RULE_subCompartmentField),
            LcaTypes.rule(LcaLangParser.RULE_meta_assignment),
            LcaTypes.rule(LcaLangParser.RULE_aliasForField),
            LcaTypes.token(LcaLangLexer.LINE_COMMENT),
            LcaTypes.token(LcaLangLexer.COMMENT),
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
