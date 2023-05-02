package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssignmentStubElementType
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitElementType

interface LcaStubElementTypes {
    companion object {
        val GLOBAL_ASSIGNMENT = GlobalAssignmentStubElementType("globalAssignment")
        val PROCESS = GlobalAssignmentStubElementType("process")
        val SUBSTANCE = GlobalAssignmentStubElementType("substance")
        val TECHNO_PRODUCT_EXCHANGE = GlobalAssignmentStubElementType("technoProductExchange")
        val UNIT = UnitElementType("unit")
    }
}
