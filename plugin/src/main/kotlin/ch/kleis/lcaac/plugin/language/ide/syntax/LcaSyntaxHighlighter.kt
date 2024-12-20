package ch.kleis.lcaac.plugin.language.ide.syntax

import ch.kleis.lcaac.plugin.language.loader.LcaLexerAdapter
import ch.kleis.lcaac.plugin.psi.LcaTypes.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType


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
        return LcaLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            PACKAGE_KEYWORD, PROCESS_KEYWORD, INDICATOR_KEYWORD,
            SUBSTANCE_KEYWORD, IMPACTS_KEYWORD, META_KEYWORD, PARAMETERS_KEYWORD,
            LABELS_KEYWORD, MATCH_KEYWORD, FROM_KEYWORD, ALLOCATE_KEYWORD,
            UNIT_KEYWORD, IMPORT_KEYWORD,
            VARIABLES_KEYWORD, LAND_USE_KEYWORD,
            PRODUCTS_KEYWORD, INPUTS_KEYWORD,
            EMISSIONS_KEYWORD, RESOURCES_KEYWORD,
            TEST_KEYWORD, GIVEN_KEYWORD, ASSERT_KEYWORD, BETWEEN_KEYWORD, AND_KEYWORD,
            DATASOURCE_KEYWORD, SCHEMA_KEYWORD, FOR_EACH_KEYWORD -> KEYWORD_KEYS

            IDENTIFIER -> IDENTIFIER_KEYS

            STRING_LITERAL -> STRING_LITERAL_KEYS

            DIMENSION_KEYWORD, ALIAS_FOR_KEYWORD,
            REFERENCE_UNIT_KEYWORD,
            SYMBOL_KEYWORD, NAME_KEYWORD,
            TYPE_KEYWORD, COMPARTMENT_KEYWORD, SUB_COMPARTMENT_KEYWORD,
            LOCATION_KEYWORD, SUM_KEYWORD, LOOKUP_KEYWORD, DEFAULT_RECORD_KEYWORD -> FIELD_KEYS

            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS

            NUMBER -> NUMBER_KEYS

            COMMENT_BLOCK_START, COMMENT_BLOCK_END, COMMENT_LINE, COMMENT_CONTENT -> BLOCK_COMMENT_KEYS

            else -> EMPTY_KEYS
        }
    }
}
