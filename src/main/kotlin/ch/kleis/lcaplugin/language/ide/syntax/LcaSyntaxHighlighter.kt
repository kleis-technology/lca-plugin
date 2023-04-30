package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor


class LcaSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val KEYWORD_KEYS = arrayOf(createTextAttributesKey("LCA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD))
        val IDENTIFIER_KEYS = arrayOf(
            createTextAttributesKey(
                "IDENTIFIER",
                DefaultLanguageHighlighterColors.IDENTIFIER
            )
        )
        val EMPTY_KEYS = emptyArray<TextAttributesKey>()
        val BAD_CHARACTER_KEYS = arrayOf(createTextAttributesKey("SIMPLE_BAD_CHARACTER", BAD_CHARACTER))
        val STRING_LITERAL_KEYS =
            arrayOf(createTextAttributesKey("STRING_LITERAL", DefaultLanguageHighlighterColors.STRING))
        val FIELD_KEYS = arrayOf(createTextAttributesKey("FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD))
        val NUMBER_KEYS = arrayOf(createTextAttributesKey("UNIT", DefaultLanguageHighlighterColors.NUMBER))
        val BLOCK_COMMENT_KEYS =
            arrayOf(createTextAttributesKey("BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT))
    }

    override fun getHighlightingLexer(): Lexer {
        return ANTLRLexerAdaptor(LcaLanguage.INSTANCE, LcaLangLexer(null))
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            LcaTypes.token(LcaLangLexer.PACKAGE_KEYWORD),
            LcaTypes.token(LcaLangLexer.PROCESS_KEYWORD),
            LcaTypes.token(LcaLangLexer.SUBSTANCE_KEYWORD),
            LcaTypes.token(LcaLangLexer.IMPACTS_KEYWORD),
            LcaTypes.token(LcaLangLexer.META_KEYWORD),
            LcaTypes.token(LcaLangLexer.PARAMETERS_KEYWORD),
            LcaTypes.token(LcaLangLexer.FROM_KEYWORD),
            LcaTypes.token(LcaLangLexer.ALLOCATE_KEYWORD),
            LcaTypes.token(LcaLangLexer.UNIT_KEYWORD),
            LcaTypes.token(LcaLangLexer.IMPORT_KEYWORD),
            LcaTypes.token(LcaLangLexer.VARIABLES_KEYWORD),
            LcaTypes.token(LcaLangLexer.LAND_USE_KEYWORD),
            LcaTypes.token(LcaLangLexer.PRODUCTS_KEYWORD),
            LcaTypes.token(LcaLangLexer.INPUTS_KEYWORD),
            LcaTypes.token(LcaLangLexer.EMISSIONS_KEYWORD),
            LcaTypes.token(LcaLangLexer.RESOURCES_KEYWORD) -> KEYWORD_KEYS

            LcaTypes.token(LcaLangLexer.ID) -> IDENTIFIER_KEYS

            LcaTypes.token(LcaLangLexer.STRING_LITERAL) -> STRING_LITERAL_KEYS

            LcaTypes.token(LcaLangLexer.DIMENSION_KEYWORD),
            LcaTypes.token(LcaLangLexer.ALIAS_FOR_KEYWORD),
            LcaTypes.token(LcaLangLexer.REFERENCE_UNIT_KEYWORD),
            LcaTypes.token(LcaLangLexer.SYMBOL_KEYWORD),
            LcaTypes.token(LcaLangLexer.NAME_KEYWORD),
            LcaTypes.token(LcaLangLexer.TYPE_KEYWORD),
            LcaTypes.token(LcaLangLexer.COMPARTMENT_KEYWORD),
            LcaTypes.token(LcaLangLexer.SUB_COMPARTMENT_KEYWORD) -> FIELD_KEYS

            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS

            LcaTypes.token(LcaLangLexer.NUMBER) -> NUMBER_KEYS

            LcaTypes.token(LcaLangLexer.COMMENT),
            LcaTypes.token(LcaLangLexer.LINE_COMMENT) -> BLOCK_COMMENT_KEYS

            else -> EMPTY_KEYS
        }
    }
}
