package ch.kleis.lcaac.plugin.language.psi.type.spec

import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaProductRef
import ch.kleis.lcaac.plugin.psi.LcaTechnoProductExchange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

interface PsiOutputProductSpec : PsiNameIdentifierOwner {
    fun getContainingProcess(): LcaProcess {
        return PsiTreeUtil.getParentOfType(this, LcaProcess::class.java) as LcaProcess
    }

    fun getContainingTechnoExchange(): LcaTechnoProductExchange {
        return PsiTreeUtil.getParentOfType(this, LcaTechnoProductExchange::class.java) as LcaTechnoProductExchange
    }

    fun getProductRef(): LcaProductRef {
        return PsiTreeUtil.getChildOfType(this, LcaProductRef::class.java) as LcaProductRef
    }

    override fun getName(): String {
        return getProductRef().name
    }

    override fun setName(name: String): PsiElement {
        getProductRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProductRef().nameIdentifier
    }
}
