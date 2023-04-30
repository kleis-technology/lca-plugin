package ch.kleis.lcaplugin.language.psi.stub.global_assignment

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class GlobalAssignmentStubImpl(
    parent: StubElement<PsiGlobalAssignment>,
    override val fqn: String,
) : StubBase<PsiGlobalAssignment>(parent, LcaTypes.rule(LcaLangParser.RULE_globalAssignment) as IStubElementType<out StubElement<*>, *>),
    GlobalAssignmentStub
