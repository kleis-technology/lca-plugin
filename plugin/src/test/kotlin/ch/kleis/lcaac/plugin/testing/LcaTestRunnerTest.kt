package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class LcaTestRunnerTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun test_run_whenSuccess() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile("$pkgName.lca",
            """
                process p {
                    products {
                        1 kg out
                    }
                    impacts {
                        1 kg GWP
                    }
                }
                
                test p {
                    given {
                        2 kg out from p
                    }
                    assert {
                        GWP between 500 g and 2 kg
                    }
                }
            """.trimIndent())
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val target = file.getTests().first()
        val runner = LcaTestRunner(project)

        // when
        val result = runner.run(target)

        // then
        assertEquals(LcaTestSuccess, result)
    }

    @Test
    fun test_run_whenFailure() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile("$pkgName.lca",
            """
                process p {
                    products {
                        1 kg out
                    }
                    impacts {
                        1 kg GWP
                    }
                }
                
                test p {
                    given {
                        2 kg out from p
                    }
                    assert {
                        GWP between 500 kg and 2000 kg
                    }
                }
            """.trimIndent())
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val target = file.getTests().first()
        val runner = LcaTestRunner(project)

        // when
        val result = runner.run(target)

        // then
        assertEquals(LcaTestFailure, result)
    }
}
