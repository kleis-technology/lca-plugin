package ch.kleis.lcaac.plugin.e2e

import ch.kleis.lcaac.plugin.language.ide.structure.LcaStructureViewElement
import ch.kleis.lcaac.plugin.language.ide.structure.LcaStructureViewModel
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.ide.structureView.StructureViewFactory
import com.intellij.psi.PsiManager
import com.intellij.testFramework.FileEditorManagerTestCase
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Test

@RunWith(JUnit4::class)
class LcaStructureViewTest : FileEditorManagerTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_structureView() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process water_production {
                }
                
                substance co2 {
                    name = "co2"
                    type = Emission
                    compartment = "compartment"
                    reference_unit = kg
                    
                    impacts {
                        1 kg co2
                    }
                }
                
                variables {
                    density = 1 kg
                }
                
                process carrot_production {
                }
                
                substance nh4 {
                    name = "nh4"
                    type = Emission
                    compartment = "compartment"
                    reference_unit = kg
                    
                    impacts {
                        1 kg nh4
                    }
                }
                
                variables {
                    monad = 1 kg
                }
            """.trimIndent()
        )
        val lcaFile = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val editor = manager!!.openFile(vf).first()
        val model = LcaStructureViewModel(myFixture.editor, lcaFile)

        // when
        val view = StructureViewFactory.getInstance(project).createStructureView(editor, model, project)

        // then
        val root = view.treeModel.root as LcaStructureViewElement
        assertEquals("$pkgName.lca", root.presentation.presentableText)
        listOf(
            "water_production",
            "carrot_production",
            "co2",
            "nh4",
            "variables",
            "variables",
        ).forEachIndexed { index, s ->
            assertEquals(s, root.children[index].presentation.presentableText)
        }
    }

}
