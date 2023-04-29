package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.PsiAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchangeWithAllocateField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import java.awt.Font

class LcaDocumentationProvider : AbstractDocumentationProvider() {
    private companion object {
        private var SECTION_ROW_START = "<tr>\n<td valign='top' class='section'>"
        private var SECTION_NEXT_COL = "</td>\n<td valign='top'>"
        private var SECTION_ROW_END = "</td>\n</tr>\n"
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is PsiSubstance -> {
                val sb = StringBuilder()
                documentTitle(sb, "Substance", element.getSubstanceRef().name)
                documentSubstanceData(sb, element)
                documentBlockMetaOwner(sb, element)
                sb.toString()
            }

            is PsiTechnoProductExchange -> {
                val sb = StringBuilder()
                val processProducer = getProcessProducer(element)
                documentProductTitle(sb, element.getProductRef(), processProducer)
                documentBlockMetaOwner(sb, processProducer)
                documentProcessParams(sb, processProducer)
                addSeparatorLine(sb)
                sb.toString()
            }

            is PsiProcess -> {
                val sb = StringBuilder()
                documentTitle(sb, "Process", element.getProcessTemplateRef().name)
                documentBlockMetaOwner(sb, element)
                documentProcessParams(sb, element)
                addSeparatorLine(sb)
                sb.toString()
            }

            is PsiQuantityRef -> {
                when (val target = element.reference.resolve()) {
                    is PsiUnitDefinition -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Unit", element.name)
                        documentUnitData(sb, target)
                        sb.toString()
                    }

                    is PsiGlobalAssignment -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Global quantity", element.name)
                        documentQuantityData(sb, target)
                        sb.toString()
                    }

                    is PsiAssignment -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Quantity", element.name)
                        documentQuantityData(sb, target)
                        sb.toString()
                    }

                    else -> super.generateDoc(element, originalElement)
                }
            }

            is PsiParameterRef -> {
                when (val target = element.reference.resolve()) {
                    is PsiAssignment -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Quantity", element.name)
                        documentQuantityData(sb, target)
                        sb.toString()
                    }

                    else -> super.generateDoc(element, originalElement)
                }
            }

            else -> super.generateDoc(element, originalElement)
        }
    }

    private fun documentProcessParams(sb: StringBuilder, lcaProcess: PsiProcess?) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.GRAY
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Process Parameters:", 1f)
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        lcaProcess?.getParameters()
            ?.forEach {
                addKeyValueSection("${it.key} = ", it.value.text, sb)
            }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentProductTitle(sb: StringBuilder, product: PsiProductRef, process: PsiProcess?) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.ORANGE
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Product", 1f)
        sb.append(" ")
        documentUid(sb, product.name, false)
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, " from ", 1f)
        if (process == null) {
            documentUid(sb, "unknown")
        } else {
            documentUid(sb, process.getProcessTemplateRef().name)
        }
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }


    private fun documentTitle(sb: StringBuilder, type: String, name: String) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.ORANGE
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, type, 1f)
        sb.append(" ")
        documentUid(sb, name)
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }

    private fun documentUnitData(sb: StringBuilder, element: PsiUnitDefinition) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", element.getSymbolField().getValue(), sb)
        if (element.getType() == UnitDefinitionType.LITERAL) {
            addKeyValueSection("Dimension", element.getDimensionField().getValue(), sb)
        }
        if (element.getType() == UnitDefinitionType.ALIAS) {
            addKeyValueSection("Alias for", element.getAliasForField().getValue().text, sb)
        }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentQuantityData(sb: StringBuilder, element: PsiGlobalAssignment) {
        documentQuantityData(sb, element.getQuantityRef().name, element.getValue().text)
    }

    private fun documentQuantityData(sb: StringBuilder, element: PsiAssignment) {
        documentQuantityData(sb, element.getQuantityRef().name, element.getValue().text)
    }

    private fun documentQuantityData(sb: StringBuilder, qtyName: String, value: String) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", qtyName, sb)
        addKeyValueSection("Value", value, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentSubstanceData(sb: StringBuilder, element: PsiSubstance) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Name", element.getNameField().getValue(), sb)
        addKeyValueSection("Type", element.getTypeField().getType(), sb)
        addKeyValueSection("Compartment", element.getCompartmentField().getValue(), sb)
        addKeyValueSection("Sub-Compartment", element.getSubcompartmentField()?.getValue(), sb)
        addKeyValueSection("Reference Unit", element.getReferenceUnitField().getValue().text, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentUid(sb: StringBuilder, name: String, withCR: Boolean = true) {
        val att = TextAttributes()
        att.foregroundColor = JBColor.BLUE
        att.fontType = Font.BOLD
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, name, 0.5f)
        if (withCR) sb.append("\n")
    }

    private fun documentBlockMetaOwner(sb: StringBuilder, blockOwner: BlockMetaOwner?) {
        if (blockOwner != null) {
            val meta = blockOwner.getBlockMetaList()
                .flatMap { it.getAssignments() }
                .associate { Pair(it.name, it.getValue()) }
            val desc = meta["description"]
            if (desc != null) {
                sb.append(DocumentationMarkup.CONTENT_START).append("\n")
                HtmlSyntaxInfoUtil.appendStyledSpan(sb, TextAttributes(), desc.replace("\n", "<br>"), 0.5f)
                sb.append(DocumentationMarkup.CONTENT_END).append("\n")
            }
            val author = meta["author"]
            if (author != null) {
                sb.append(DocumentationMarkup.CONTENT_START).append("\n")
                sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
                addKeyValueSection("Author", author, sb)
                sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
                sb.append(DocumentationMarkup.CONTENT_END).append("\n")
            }
        }
    }

    private fun addKeyValueSection(key: String, value: String?, sb: StringBuilder) {
        if (value != null) {
            sb.append(SECTION_ROW_START)
            sb.append(key)
            sb.append(SECTION_NEXT_COL)
            sb.append(value)
            sb.append(SECTION_ROW_END)
        }
    }

    private fun addSeparatorLine(sb: StringBuilder) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }

    private fun getProcessProducer(element: PsiElement): PsiProcess?{
        if (element.parent is PsiTechnoProductExchangeWithAllocateField){
            return element.parent.parent?.parent as PsiProcess?
        }
        return element.parent?.parent as PsiProcess?
    }
}
