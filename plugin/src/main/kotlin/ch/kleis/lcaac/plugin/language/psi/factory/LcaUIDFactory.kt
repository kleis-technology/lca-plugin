package ch.kleis.lcaac.plugin.language.psi.factory

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.type.PsiUID

class LcaUIDFactory(
    private val createFile: (content: String) -> LcaFile
) {
    fun createUid(name: String): PsiUID {
        val content = """
            package dummy
            variables {
                $name = 1 kg
            }
        """.trimIndent()
        val file = createFile(content)
        val varBlock = file.getBlocksOfGlobalVariables().first()
        val assignment = varBlock.globalAssignmentList.first()
        return assignment.getDataRef().getUID()
    }
}
