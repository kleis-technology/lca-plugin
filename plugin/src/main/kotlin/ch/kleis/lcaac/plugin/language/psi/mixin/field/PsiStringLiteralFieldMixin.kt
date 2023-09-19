package ch.kleis.lcaac.plugin.language.psi.mixin.field

import ch.kleis.lcaac.plugin.language.psi.type.field.PsiStringLiteralField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiStringLiteralFieldMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiStringLiteralField
