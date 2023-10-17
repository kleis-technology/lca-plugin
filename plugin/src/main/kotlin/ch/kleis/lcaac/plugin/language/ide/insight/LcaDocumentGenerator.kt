package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaac.plugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import java.awt.Font

object LcaDocumentGenerator {
    private const val SECTION_ROW_START = "<tr>\n<td valign='top' class='section'>"
    private const val SECTION_NEXT_COL = "</td>\n<td valign='top'>"
    private const val SECTION_ROW_END = "</td>\n</tr>\n"

    fun generateProcess(element: LcaProcess): String {
        val sb = StringBuilder()
        documentTitle(sb, "Process", element.getProcessRef().name)
        documentBlockLabels(sb, element)
        documentBlockMetaOwner(sb, element)
        documentProcessParams(sb, element)
        addSeparatorLine(sb)
        return sb.toString()
    }

    fun generateUnitDefinition(element: LcaUnitDefinition): String {
        val sb = StringBuilder()
        documentTitle(sb, "Unit", element.name)
        documentUnitData(sb, element)
        return sb.toString()
    }

    fun generateGlobalAssignment(element: LcaGlobalAssignment): String {
        val sb = StringBuilder()
        documentTitle(sb, "Global quantity", element.name)
        documentQuantityData(sb, element)
        return sb.toString()
    }

    fun generateAssignment(element: LcaAssignment): String {
        val sb = StringBuilder()
        documentTitle(sb, "Quantity", element.name)
        documentQuantityData(sb, element)
        return sb.toString()
    }

    fun generateProduct(element: LcaOutputProductSpec): String {
        val sb = StringBuilder()
        val process = PsiTreeUtil.getParentOfType(element, LcaProcess::class.java)
        documentProductTitle(sb, element.getProductRef(), process)
        documentBlockLabels(sb, process)
        documentBlockMetaOwner(sb, process)
        documentProcessParams(sb, process)
        addSeparatorLine(sb)
        return sb.toString()
    }

    fun generateSubstance(element: LcaSubstance): String {
        val sb = StringBuilder()
        documentTitle(sb, "Substance", element.getSubstanceRef().name)
        documentSubstanceData(sb, element)
        documentBlockMetaOwner(sb, element)
        return sb.toString()
    }


    private fun documentBlockLabels(sb: StringBuilder, lcaProcess: LcaProcess?) {
        if (lcaProcess == null || lcaProcess.blockLabelsList.isEmpty()) return

        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.GRAY
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Process Labels:", 1f)
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        lcaProcess.blockLabelsList.flatMap { it.labelAssignmentList }
            .forEach {
                addKeyValueSection("${it.name} = ", """"${it.getValue()}"""", sb)
            }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentProcessParams(sb: StringBuilder, lcaProcess: LcaProcess?) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.GRAY
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Process Parameters:", 1f)
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        lcaProcess?.blockParametersList?.flatMap { it.guardedAssignmentList}?.map { it.assignment }
            ?.forEach {
                addKeyValueSection("${it.name} = ", it.getValue().text, sb)
            }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentProductTitle(sb: StringBuilder, product: PsiUIDOwner, process: LcaProcess?) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.ORANGE
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Product", 1f)
        sb.append(" ")
        documentUid(sb, product.getUID().name, false)
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, " from ", 1f)
        if (process == null) {
            documentUid(sb, "unknown")
        } else {
            documentUid(sb, process.getProcessRef().name)
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

    private fun documentUnitData(sb: StringBuilder, element: LcaUnitDefinition) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", element.getSymbolField().getValue(), sb)
        if (element.getType() == UnitDefinitionType.LITERAL) {
            addKeyValueSection("Dimension", element.dimField!!.getValue(), sb)
        }
        if (element.getType() == UnitDefinitionType.ALIAS) {
            addKeyValueSection("Alias for", element.getAliasForField()!!.dataExpression.text, sb)
        }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentQuantityData(sb: StringBuilder, element: LcaGlobalAssignment) {
        documentQuantityData(sb, element.getDataRef().name, element.getValue().text)
    }

    private fun documentQuantityData(sb: StringBuilder, element: LcaAssignment) {
        documentQuantityData(sb, element.getDataRef().name, element.getValue().text)
    }

    private fun documentQuantityData(sb: StringBuilder, qtyName: String, value: String) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", qtyName, sb)
        addKeyValueSection("Value", value, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentSubstanceData(sb: StringBuilder, element: LcaSubstance) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Name", element.getNameField().getValue(), sb)
        addKeyValueSection("Type", element.getTypeField().getValue(), sb)
        addKeyValueSection("Compartment", element.getCompartmentField().getValue(), sb)
        addKeyValueSection("Sub-Compartment", element.getSubCompartmentField()?.getValue(), sb)
        addKeyValueSection("Reference Unit", element.getReferenceUnitField().dataExpression.text, sb)
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
                .flatMap { it.metaAssignmentList }
                .associate { Pair(it.getName(), it.getValue()) }
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
}