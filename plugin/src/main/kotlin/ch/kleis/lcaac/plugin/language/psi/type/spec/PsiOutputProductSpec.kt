package ch.kleis.lcaac.plugin.language.psi.type.spec

import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaProductRef
import ch.kleis.lcaac.plugin.psi.LcaTechnoProductExchange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiOutputProductSpec : PsiNameIdentifierOwner {

    // XXX: Discuss with PBL on how to avoid using this method: any navigation "up" calls getNode() and forces a build
    // of the AST, which is *very* CPU and memory costly wrt down psi navigation.
    fun getContainingProcess(): LcaProcess

    fun getContainingTechnoExchange(): LcaTechnoProductExchange

    override fun getName(): String
}
