package ch.kleis.lcaplugin.language.ide.find_usage

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaLangTokenSets
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor

/*
    https://plugins.jetbrains.com/docs/intellij/find-usages.html
    https://plugins.jetbrains.com/docs/intellij/find-usages-provider.html#define-a-find-usages-provider
 */

class LcaFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner =
        DefaultWordsScanner(
            ANTLRLexerAdaptor(LcaLanguage.INSTANCE, LcaLangLexer(null)),
            LcaLangTokenSets.ID,
            LcaLangTokenSets.STRING_LITERALS,
            TokenSet.EMPTY,
        )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        when (psiElement) {
            is PsiTechnoProductExchange -> true
            is PsiSubstance -> true
            is PsiUnitDefinition -> true
            is PsiGlobalAssignment -> true
            else -> false
        }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String =
        when (element) {
            is PsiSubstance -> "Substance"
            is PsiTechnoInputExchange -> "Product"
            is PsiTechnoProductExchange -> "Product"
            is PsiUnitDefinition -> "Unit"
            is PsiGlobalAssignment -> "Quantity"
            else -> ""
        }

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? PsiNamedElement)?.name.orEmpty()

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as? PsiNamedElement)?.name.orEmpty()
}
