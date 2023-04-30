package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiUrn(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getParts(): List<String> {
        val first = node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_uid))?.psi as PsiUID?
        val localRootName = first?.name ?: throw IllegalStateException()
        val next = node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_urn))?.psi as PsiUrn? ?: return listOf(localRootName)
        return listOf(localRootName) + next.getParts()
    }
}
