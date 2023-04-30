package ch.kleis.lcaplugin.language.psi.stub.process

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProcessStubImpl(
    parent: StubElement<PsiProcess>,
    override val fqn: String,
) :
        StubBase<PsiProcess>(parent, LcaTypes.rule(LcaLangParser.RULE_process) as IStubElementType<out StubElement<*>, *>),
        ProcessStub
