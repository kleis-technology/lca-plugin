package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.psi.LcaFile
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
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
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
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.v4.runtime.Parser
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

        val tokens: List<TokenIElementType> = PSIElementTypeFactory.getTokenIElementTypes(LcaLanguage.INSTANCE)
        val rules: List<RuleIElementType> = PSIElementTypeFactory.getRuleIElementTypes(LcaLanguage.INSTANCE)

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
        val elType = node.elementType
        if (elType is TokenIElementType) {
            return ANTLRPsiNode(node)
        }
        if (elType !is RuleIElementType) {
            return ANTLRPsiNode(node)
        }
        return when (elType.ruleIndex) {
            LcaLangParser.RULE_pkg -> PsiPackage(node)
            LcaLangParser.RULE_pkgImport -> PsiImport(node)

            LcaLangParser.RULE_process -> PsiProcess(node)
            LcaLangParser.RULE_parameterRef -> PsiParameterRef(node)
            LcaLangParser.RULE_processTemplateRef -> PsiProcessTemplateRef(node)

            LcaLangParser.RULE_params -> PsiParameters(node)
            LcaLangParser.RULE_assignment -> PsiAssignment(node)

            LcaLangParser.RULE_variables -> PsiVariables(node)

            LcaLangParser.RULE_block_products -> PsiBlockProducts(node)
            LcaLangParser.RULE_technoProductExchange -> PsiTechnoProductExchange(node)
            LcaLangParser.RULE_technoProductExchangeWithAllocateField -> PsiTechnoProductExchangeWithAllocateField(node)

            LcaLangParser.RULE_block_inputs -> PsiBlockInputs(node)
            LcaLangParser.RULE_technoInputExchange -> PsiTechnoInputExchange(node)
            LcaLangParser.RULE_productRef -> PsiProductRef(node)
            LcaLangParser.RULE_fromProcessConstraint -> PsiFromProcessConstraint(node)
            LcaLangParser.RULE_comma_sep_arguments -> ANTLRPsiNode(node) // TODO: make rule private or introduce PsiXYZ
            LcaLangParser.RULE_argument -> PsiArgument(node)

            LcaLangParser.RULE_allocateField -> PsiAllocateField(node)

            LcaLangParser.RULE_block_emissions -> PsiBlockEmissions(node)
            LcaLangParser.RULE_block_land_use -> PsiBlockLandUse(node)
            LcaLangParser.RULE_block_resources -> PsiBlockResources(node)
            LcaLangParser.RULE_bioExchange -> PsiBioExchange(node)

            LcaLangParser.RULE_block_meta -> PsiBlockMeta(node)

            LcaLangParser.RULE_block_impacts -> PsiBlockImpacts(node)
            LcaLangParser.RULE_impactExchange -> PsiImpactExchange(node)
            LcaLangParser.RULE_indicatorRef -> PsiIndicatorRef(node)


            LcaLangParser.RULE_unitDefinition -> PsiUnitDefinition(node)
            LcaLangParser.RULE_substance -> PsiSubstance(node)
            LcaLangParser.RULE_globalVariables -> PsiGlobalVariables(node)
            LcaLangParser.RULE_globalAssignment -> PsiGlobalAssignment(node)
            LcaLangParser.RULE_substanceRef -> PsiSubstanceRef(node)

            LcaLangParser.RULE_nameField -> PsiStringLiteralField(node)
            LcaLangParser.RULE_typeField -> PsiStringLiteralField(node)
            LcaLangParser.RULE_compartmentField -> PsiStringLiteralField(node)
            LcaLangParser.RULE_subCompartmentField -> PsiStringLiteralField(node)
            LcaLangParser.RULE_dimField -> PsiStringLiteralField(node)
            LcaLangParser.RULE_symbolField -> PsiStringLiteralField(node)
            LcaLangParser.RULE_referenceUnitField -> PsiUnitField(node)
            LcaLangParser.RULE_aliasForField -> PsiAliasForField(node)


            LcaLangParser.RULE_meta_assignment -> PsiMetaAssignment(node)

            LcaLangParser.RULE_quantityRef -> PsiQuantityRef(node)
            LcaLangParser.RULE_quantity -> PsiQuantity(node)
            LcaLangParser.RULE_quantityTerm -> PsiQuantityTerm(node)
            LcaLangParser.RULE_quantityFactor -> PsiQuantityFactor(node)
            LcaLangParser.RULE_quantityPrimitive -> PsiQuantityPrimitive(node)

            LcaLangParser.RULE_unit -> PsiUnit(node)
            LcaLangParser.RULE_unitFactor -> PsiUnitFactor(node)
            LcaLangParser.RULE_unitPrimitive -> PsiUnitPrimitive(node)
            LcaLangParser.RULE_unitRef -> PsiUnitRef(node)

            LcaLangParser.RULE_urn -> PsiUrn(node)
            LcaLangParser.RULE_uid -> PsiUID(node)

            else -> ANTLRPsiNode(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LcaFile(viewProvider)
    }
}
