package ch.kleis.lcaplugin.language.parser

interface LcaStubElementTypes {
    companion object {
        val GLOBAL_ASSIGNMENT = LcaParserDefinition.GLOBAL_ASSIGNMENT
        val PROCESS = LcaParserDefinition.PROCESS
        val SUBSTANCE = LcaParserDefinition.SUBSTANCE
        val TECHNO_PRODUCT_EXCHANGE = LcaParserDefinition.TECHNO_PRODUCT_EXCHANGE
        val UNIT = LcaParserDefinition.UNIT
    }
}
