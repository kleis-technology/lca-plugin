package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssignmentStubElementType
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubElementType
import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceStubElementType
import ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange.TechnoProductExchangeElementType
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitElementType
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.language.psi.type.block.*
import ch.kleis.lcaplugin.language.psi.type.exchange.*
import ch.kleis.lcaplugin.language.psi.type.field.PsiAliasForField
import ch.kleis.lcaplugin.language.psi.type.field.PsiAllocateField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityFactor
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityPrimitive
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityTerm
import ch.kleis.lcaplugin.language.psi.type.ref.*
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitFactor
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitPrimitive
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.tree.TokenSet
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory
import org.antlr.intellij.adaptor.lexer.RuleIElementType
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import org.antlr.intellij.adaptor.parser.ANTLRParseTreeToPSIConverter
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

class LcaParserDefinition : ParserDefinition {
    companion object {
        init {
            PSIElementTypeFactory.defineLanguageIElementTypes(
                LcaLanguage.INSTANCE,
                LcaLangLexer.tokenNames,
                LcaLangParser.ruleNames
            )
        }

        /**
         * It is best to initialize element types in this companion object of parser definition,
         * because these element types must be created before index are initialized.
         */

        val tokens: List<TokenIElementType> = PSIElementTypeFactory.getTokenIElementTypes(LcaLanguage.INSTANCE)
        val rules: List<RuleIElementType> = PSIElementTypeFactory.getRuleIElementTypes(LcaLanguage.INSTANCE)

        val GLOBAL_ASSIGNMENT = GlobalAssignmentStubElementType("globalAssignment")
        val PROCESS = ProcessStubElementType("process")
        val SUBSTANCE = SubstanceStubElementType("substance")
        val TECHNO_PRODUCT_EXCHANGE = TechnoProductExchangeElementType("technoProductExchange")
        val UNIT = UnitElementType("unit")

        val ID: TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.ID,
        )

        val COMMENTS: TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.COMMENT,
            LcaLangLexer.LINE_COMMENT,
        )
        val STRING_LITERALS: TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.STRING_LITERAL,
        )
        val WHITESPACES: TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.WS,
        )

        val FILE = IStubFileElementType<PsiFileStubImpl<LcaFile>>(LcaLanguage.INSTANCE)
    }

    override fun createLexer(project: Project?): Lexer {
        val lexer = LcaLangLexer(null)
        return ANTLRLexerAdaptor(LcaLanguage.INSTANCE, lexer)
    }

    override fun createParser(project: Project?): PsiParser {
        val parser = LcaLangParser(null)
        return object : ANTLRParserAdaptor(LcaLanguage.INSTANCE, parser) {
            override fun parse(parser: Parser?, root: IElementType?): ParseTree {
                if (root !is IFileElementType) {
                    throw UnsupportedOperationException()
                }
                return (parser as LcaLangParser).lcaFile()
            }

            override fun createListener(
                parser: Parser?,
                root: IElementType?,
                builder: PsiBuilder?
            ): ANTLRParseTreeToPSIConverter {
                return object : ANTLRParseTreeToPSIConverter(LcaLanguage.INSTANCE, parser, builder) {
                    /**
                     * Mark stub with the appropriate stub element type
                     * Cf. super.exitEveryRule(ctx)
                     */
                    override fun exitEveryRule(ctx: ParserRuleContext) {
                        ProgressIndicatorProvider.checkCanceled()
                        val marker = markers.pop()
                        when (val ruleIndex = ctx.ruleIndex) {
                            LcaLangParser.RULE_globalAssignment -> marker.done(GLOBAL_ASSIGNMENT)
                            LcaLangParser.RULE_process -> marker.done(PROCESS)
                            LcaLangParser.RULE_substance -> marker.done(SUBSTANCE)
                            LcaLangParser.RULE_technoProductExchange -> marker.done(TECHNO_PRODUCT_EXCHANGE)
                            LcaLangParser.RULE_unitDefinition -> marker.done(UNIT)
                            else -> marker.done(rules[ruleIndex])
                        }
                    }
                }
            }
        }
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITESPACES
    }

    override fun getStringLiteralElements(): TokenSet {
        return STRING_LITERALS
    }

    override fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            /**
             * Don't forget the stub element types
             * (set by the listener in the parser above)
             */
            GLOBAL_ASSIGNMENT -> PsiGlobalAssignment(node)
            PROCESS -> PsiProcess(node)
            SUBSTANCE -> PsiSubstance(node)
            TECHNO_PRODUCT_EXCHANGE -> PsiTechnoProductExchange(node)
            UNIT -> PsiUnitDefinition(node)

            rules[LcaLangParser.RULE_pkg] -> PsiPackage(node)
            rules[LcaLangParser.RULE_pkgImport] -> PsiImport(node)
            rules[LcaLangParser.RULE_process] -> PsiProcess(node)
            rules[LcaLangParser.RULE_parameterRef] -> PsiParameterRef(node)
            rules[LcaLangParser.RULE_processTemplateRef] -> PsiProcessTemplateRef(node)
            rules[LcaLangParser.RULE_params] -> PsiParameters(node)
            rules[LcaLangParser.RULE_assignment] -> PsiAssignment(node)
            rules[LcaLangParser.RULE_variables] -> PsiVariables(node)
            rules[LcaLangParser.RULE_block_products] -> PsiBlockProducts(node)
            rules[LcaLangParser.RULE_technoProductExchange] -> PsiTechnoProductExchange(node)
            rules[LcaLangParser.RULE_technoProductExchangeWithAllocateField] -> PsiTechnoProductExchangeWithAllocateField(
                node
            )

            rules[LcaLangParser.RULE_block_inputs] -> PsiBlockInputs(node)
            rules[LcaLangParser.RULE_technoInputExchange] -> PsiTechnoInputExchange(node)
            rules[LcaLangParser.RULE_productRef] -> PsiProductRef(node)
            rules[LcaLangParser.RULE_fromProcessConstraint] -> PsiFromProcessConstraint(node)
            rules[LcaLangParser.RULE_comma_sep_arguments] -> ANTLRPsiNode(node) // TODO: make rule private or introduce PsiXYZ
            rules[LcaLangParser.RULE_argument] -> PsiArgument(node)
            rules[LcaLangParser.RULE_allocateField] -> PsiAllocateField(node)
            rules[LcaLangParser.RULE_block_emissions] -> PsiBlockEmissions(node)
            rules[LcaLangParser.RULE_block_land_use] -> PsiBlockLandUse(node)
            rules[LcaLangParser.RULE_block_resources] -> PsiBlockResources(node)
            rules[LcaLangParser.RULE_bioExchange] -> PsiBioExchange(node)
            rules[LcaLangParser.RULE_block_meta] -> PsiBlockMeta(node)
            rules[LcaLangParser.RULE_block_impacts] -> PsiBlockImpacts(node)
            rules[LcaLangParser.RULE_impactExchange] -> PsiImpactExchange(node)
            rules[LcaLangParser.RULE_indicatorRef] -> PsiIndicatorRef(node)
            rules[LcaLangParser.RULE_unitDefinition] -> PsiUnitDefinition(node)
            rules[LcaLangParser.RULE_substance] -> PsiSubstance(node)
            rules[LcaLangParser.RULE_globalVariables] -> PsiGlobalVariables(node)
            rules[LcaLangParser.RULE_globalAssignment] -> PsiGlobalAssignment(node)
            rules[LcaLangParser.RULE_substanceRef] -> PsiSubstanceRef(node)
            rules[LcaLangParser.RULE_nameField] -> PsiStringLiteralField(node)
            rules[LcaLangParser.RULE_typeField] -> PsiStringLiteralField(node)
            rules[LcaLangParser.RULE_compartmentField] -> PsiStringLiteralField(node)
            rules[LcaLangParser.RULE_subCompartmentField] -> PsiStringLiteralField(node)
            rules[LcaLangParser.RULE_dimField] -> PsiStringLiteralField(node)
            rules[LcaLangParser.RULE_symbolField] -> PsiStringLiteralField(node)
            rules[LcaLangParser.RULE_referenceUnitField] -> PsiUnitField(node)
            rules[LcaLangParser.RULE_aliasForField] -> PsiAliasForField(node)
            rules[LcaLangParser.RULE_meta_assignment] -> PsiMetaAssignment(node)
            rules[LcaLangParser.RULE_quantityRef] -> PsiQuantityRef(node)
            rules[LcaLangParser.RULE_quantity] -> PsiQuantity(node)
            rules[LcaLangParser.RULE_quantityTerm] -> PsiQuantityTerm(node)
            rules[LcaLangParser.RULE_quantityFactor] -> PsiQuantityFactor(node)
            rules[LcaLangParser.RULE_quantityPrimitive] -> PsiQuantityPrimitive(node)
            rules[LcaLangParser.RULE_unit] -> PsiUnit(node)
            rules[LcaLangParser.RULE_unitFactor] -> PsiUnitFactor(node)
            rules[LcaLangParser.RULE_unitPrimitive] -> PsiUnitPrimitive(node)
            rules[LcaLangParser.RULE_unitRef] -> PsiUnitRef(node)
            rules[LcaLangParser.RULE_urn] -> PsiUrn(node)
            rules[LcaLangParser.RULE_uid] -> PsiUID(node)

            else -> ANTLRPsiNode(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LcaFile(viewProvider)
    }
}
