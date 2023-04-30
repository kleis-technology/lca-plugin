package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.psi.LcaFile
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

        val ID : TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.ID,
        )

        val COMMENTS : TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.COMMENT,
            LcaLangLexer.LINE_COMMENT,
        )
        val STRING_LITERALS : TokenSet = PSIElementTypeFactory.createTokenSet(
            LcaLanguage.INSTANCE,
            LcaLangLexer.STRING_LITERAL,
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
            LcaLangParser.RULE_pkg -> TODO("implement me and other rules")
            else -> ANTLRPsiNode(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LcaFile(viewProvider)
    }
}
