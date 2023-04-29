package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaLangTokenSets
import ch.kleis.lcaplugin.language.psi.type.PsiMetaAssignment
import com.intellij.psi.PsiElement

interface PsiBlockMeta: PsiElement {
    fun getAssignments(): Collection<PsiMetaAssignment> {
        return node.getChildren(LcaLangTokenSets.create(LcaLangParser.RULE_meta_assignment))
            .map { it as PsiMetaAssignment }
    }
}
