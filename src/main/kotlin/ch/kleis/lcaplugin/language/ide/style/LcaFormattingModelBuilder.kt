package ch.kleis.lcaplugin.language.ide.style

import ch.kleis.lcaplugin.LcaLanguage.Companion.INSTANCE
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings


class LcaFormattingModelBuilder : FormattingModelBuilder {

    companion object {
        private val IMPORT = LcaTypes.rule(LcaLangParser.RULE_pkgImport)
        private val UNIT_DEFINITION = LcaTypes.rule(LcaLangParser.RULE_unitDefinition)
        private val PROCESS = LcaTypes.rule(LcaLangParser.RULE_process)
        private val SUBSTANCE = LcaTypes.rule(LcaLangParser.RULE_substance)
        private val ASSIGNMENT = LcaTypes.rule(LcaLangParser.RULE_assignment)
        private val GLOBAL_ASSIGNMENT = LcaTypes.rule(LcaLangParser.RULE_globalAssignment)
        private val TECHNO_INPUT_EXCHANGE = LcaTypes.rule(LcaLangParser.RULE_technoInputExchange)
        private val TECHNO_PRODUCT_EXCHANGE = LcaTypes.rule(LcaLangParser.RULE_technoProductExchange)
        private val BIO_EXCHANGE = LcaTypes.rule(LcaLangParser.RULE_bioExchange)
        private val UNIT_REF = LcaTypes.rule(LcaLangParser.RULE_unitRef)
        private val PROCESS_TEMPLATE_REF = LcaTypes.rule(LcaLangParser.RULE_processTemplateRef)
        private val PARAMS = LcaTypes.rule(LcaLangParser.RULE_params)
        private val GLOBAL_VARIABLES = LcaTypes.rule(LcaLangParser.RULE_globalVariables)
        private val VARIABLES = LcaTypes.rule(LcaLangParser.RULE_variables)
        private val BLOCK_PRODUCTS = LcaTypes.rule(LcaLangParser.RULE_block_products)
        private val BLOCK_INPUTS = LcaTypes.rule(LcaLangParser.RULE_block_inputs)
        private val BLOCK_EMISSIONS = LcaTypes.rule(LcaLangParser.RULE_block_emissions)
        private val BLOCK_RESOURCES = LcaTypes.rule(LcaLangParser.RULE_block_resources)
        private val BLOCK_META = LcaTypes.rule(LcaLangParser.RULE_block_meta)
        private val PRODUCT_REF = LcaTypes.rule(LcaLangParser.RULE_productRef)
        private val SUBSTANCE_REF = LcaTypes.rule(LcaLangParser.RULE_substanceRef)
        private val INDICATOR_REF = LcaTypes.rule(LcaLangParser.RULE_indicatorRef)

        private val LBRACE = LcaTypes.token(LcaLangLexer.LBRACE)
        private val RBRACE = LcaTypes.token(LcaLangLexer.RBRACE)
        private val IDENTIFIER = LcaTypes.token(LcaLangLexer.ID)
        private val LINE_COMMENT = LcaTypes.token(LcaLangLexer.LINE_COMMENT)
        private val COMMENT = LcaTypes.token(LcaLangLexer.COMMENT)
        private val PLUS = LcaTypes.token(LcaLangLexer.PLUS)
        private val MINUS = LcaTypes.token(LcaLangLexer.MINUS)
        private val SLASH = LcaTypes.token(LcaLangLexer.SLASH)
        private val STAR = LcaTypes.token(LcaLangLexer.STAR)
        private val EQUAL = LcaTypes.token(LcaLangLexer.EQUAL)
        private val LPAREN = LcaTypes.token(LcaLangLexer.LPAREN)
        private val RPAREN = LcaTypes.token(LcaLangLexer.RPAREN)
        private val NUMBER = LcaTypes.token(LcaLangLexer.NUMBER)

        private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
            return SpacingBuilder(settings, INSTANCE)
                // Before Block
                .before(IMPORT)
                .spacing(0, 0, 0, true, 2)
                .before(UNIT_DEFINITION)
                .spacing(0, 0, 0, true, 1)
                .before(PROCESS)
                .spacing(0, 0, 0, true, 1)
                .before(SUBSTANCE)
                .spacing(0, 0, 0, true, 1)
                // Braces
                .before(RBRACE)
                .spacing(0, 0, 0, true, 0)
                .before(LBRACE)
                .spacing(1, 1, 0, false, 0)
                .between(LBRACE, ASSIGNMENT )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, GLOBAL_ASSIGNMENT )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, TECHNO_INPUT_EXCHANGE )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, TECHNO_PRODUCT_EXCHANGE )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, BIO_EXCHANGE )
                .spacing(0, 0, 1, true, 0)
                // Unit_Literal
                .aroundInside(UNIT_REF, UNIT_DEFINITION)
                .spaces(1)
                // PROCESS
                .aroundInside(PROCESS_TEMPLATE_REF, PROCESS)
                .spaces(1)
                .betweenInside(IDENTIFIER, LBRACE, PROCESS)
                .spacing(1, 1, 0, false, 0)
                .betweenInside(LBRACE, RBRACE, PROCESS)
                .spacing(0 , 0, 2, false, 0)
                .beforeInside(PARAMS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .before(GLOBAL_VARIABLES)
                .spacing(0 , 0, 0, true, 1)
                .before(VARIABLES)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_PRODUCTS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_INPUTS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_EMISSIONS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_RESOURCES, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_META, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                // Comments
                .before(COMMENT)
                .spaces(0)
                .before(LINE_COMMENT)
                .spaces(0)
                // Formula
                .around(PLUS).spaces(1)
                .around(MINUS).spaces(1)
                .around(SLASH).spaces(1)
                .around(STAR).spaces(1)
                .around(EQUAL).spaces(1)
                .after(LPAREN).spaces(1)
                .before(RPAREN).spaces(1)
                .around(NUMBER).spaces(1)
                .before(PRODUCT_REF)
                .spaces(1)
                .before(SUBSTANCE_REF)
                .spaces(1)
                // Substances
                .before(INDICATOR_REF)
                .spaces(1)
        }
    }

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                formattingContext.containingFile,
                LcaIndentBlock(
                    formattingContext.node,
                    createSpaceBuilder(formattingContext.codeStyleSettings)
                ),
                formattingContext.codeStyleSettings
            )
    }
}
