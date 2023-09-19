package ch.kleis.lcaac.plugin.language.psi.stub.global_assignment

import ch.kleis.lcaac.plugin.psi.LcaGlobalAssignment
import com.intellij.psi.stubs.StubElement

interface GlobalAssignmentStub : StubElement<LcaGlobalAssignment> {
    val fqn: String
}
