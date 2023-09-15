package ch.kleis.lcaac.plugin.language.psi

import ch.kleis.lcaac.plugin.language.psi.type.PsiProcess
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/* This file contains functions that are used strictly more than once in two different places */

/**
 * Checks if a PsiElement is an LCA process.
 */
fun isProcess(element: PsiElement): Boolean =
    element.elementType == LcaTypes.PROCESS_KEYWORD && element.parent is PsiProcess
