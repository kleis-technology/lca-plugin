package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.Reader

class HtmlAndPlainTextTransferableTest : DescribeSpec({
    describe("HtmlAndPlainTextTransferable") {
        val sut = HtmlAndPlainTextTransferable("plain", "<html>html</html>")

        it("should provide the html as String and Reader") {
            sut.getTransferData(DataFlavor("text/html;class=java.lang.String")) shouldBe "<html>html</html>"
            (sut.getTransferData(DataFlavor("text/html;class=java.io.Reader")) as Reader).readText() shouldBe "<html>html</html>"
        }

        it("should provide the plain text as String, Reader and Java String") {
            sut.getTransferData(DataFlavor("text/plain;class=java.lang.String")) shouldBe "plain"
            (sut.getTransferData(DataFlavor("text/plain;class=java.io.Reader")) as Reader).readText() shouldBe "plain"
            sut.getTransferData(DataFlavor.stringFlavor) shouldBe "plain"
        }

        it("should reject unsupported flavors") {
            sut.isDataFlavorSupported(DataFlavor.imageFlavor) shouldBe false
            shouldThrow<UnsupportedFlavorException> { sut.getTransferData(DataFlavor.imageFlavor) }
        }
    }
})
