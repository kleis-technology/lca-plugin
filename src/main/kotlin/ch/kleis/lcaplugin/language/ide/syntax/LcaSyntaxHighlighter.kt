package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.ALIAS_FOR_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.ALLOCATE_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_BLOCK_END
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_BLOCK_START
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_CONTENT
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMMENT_LINE
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.COMPARTMENT_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.DIMENSION_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.EMISSIONS_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.FROM_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.IDENTIFIER
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.IMPACTS_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.IMPORT_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.INDICATOR_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.INPUTS_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.LAND_USE_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.META_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.NAME_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.NUMBER
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.PACKAGE_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.PARAMETERS_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.PROCESS_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.PRODUCTS_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.REFERENCE_UNIT_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.RESOURCES_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.STRING_LITERAL
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.SUBSTANCE_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.SUB_COMPARTMENT_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.SYMBOL_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.TYPE_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.UNIT_KEYWORD
import ch.kleis.lcaplugin.psi.LcaTokenTypes.Companion.VARIABLES_KEYWORD
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
            PACKAGE_KEYWORD, PROCESS_KEYWORD, INDICATOR_KEYWORD, SUBSTANCE_KEYWORD, IMPACTS_KEYWORD, META_KEYWORD, PARAMETERS_KEYWORD,
            FROM_KEYWORD, ALLOCATE_KEYWORD,
            UNIT_KEYWORD, IMPORT_KEYWORD,
            VARIABLES_KEYWORD, LAND_USE_KEYWORD,
            PRODUCTS_KEYWORD, INPUTS_KEYWORD,
            EMISSIONS_KEYWORD, RESOURCES_KEYWORD -> KEYWORD_KEYS

            IDENTIFIER -> IDENTIFIER_KEYS

            STRING_LITERAL -> STRING_LITERAL_KEYS

            DIMENSION_KEYWORD, ALIAS_FOR_KEYWORD,
            REFERENCE_UNIT_KEYWORD,
            SYMBOL_KEYWORD, NAME_KEYWORD,
            TYPE_KEYWORD, COMPARTMENT_KEYWORD, SUB_COMPARTMENT_KEYWORD -> FIELD_KEYS

            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS

            NUMBER -> NUMBER_KEYS

            COMMENT_BLOCK_START, COMMENT_BLOCK_END, COMMENT_LINE, COMMENT_CONTENT -> BLOCK_COMMENT_KEYS

            else -> EMPTY_KEYS
        }
    }
}
