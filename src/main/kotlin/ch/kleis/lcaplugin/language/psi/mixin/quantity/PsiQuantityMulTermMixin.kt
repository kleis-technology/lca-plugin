package ch.kleis.lcaplugin.language.psi.mixin.quantity

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantityMulTerm
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityMulTermMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityMulTerm
