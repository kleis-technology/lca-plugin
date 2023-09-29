package ch.kleis.lcaac.plugin.language.psi

import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/* This file contains functions that are used strictly more than once in two different places */

/**
 * Checks if a PsiElement is an LCA process.
 */
fun isProcess(element: PsiElement): Boolean =
    element.elementType == LcaTypes.PROCESS_KEYWORD && element.parent is LcaProcess

fun isTest(element: PsiElement): Boolean =
    element.elementType == LcaTypes.TEST_KEYWORD && element.parent is LcaTest
