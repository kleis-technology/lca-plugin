package task

class SubstanceWithImpact {

    private val factorRecords = mutableListOf<EFRecord>()
    private var substanceRecord: EFRecord? = null

    fun factor(element: EFRecord): SubstanceWithImpact {
        if (element.isSubstance()) {
            substanceRecord = element
        } else {
            factorRecords.add(element)
        }
        return this
    }

    private fun padStart(str: String, pad: Int): String {
        return str.padStart(str.length + pad)
    }

    @Suppress("SameParameterValue")
    private fun impactsContent(pad: Int = 4): String = factorRecords
        .filter { it.methodLocation().isBlank() }
        .map {
            "|".plus(
                padStart(
                    "${it.characterizationFactor()} ${it.unit()} ${it.methodName()}",
                    pad
                )
            )
        }
        .joinToString("\n")

    private val impactsSubsection: String
        get() = if (factorRecords.size > 0) {
            """
            |    impacts {
            ${impactsContent(8)}
            |    }
        """.trimMargin()
        } else ""

    private val substanceContent: String
        get() = """
            |substance ${substanceRecord?.substanceId()} {
            |
            |$substanceBody
            |
            |}
        """.trimMargin()

    private fun getSubCompartiment(): String {
        val sub = substanceRecord?.subCompartment()
        @Suppress("SameParameterValue")
        return if (sub.isNullOrBlank()) {
            ""
        } else {
            "    sub_compartment = \"$sub\""
        }
    }

    private val substanceBody: String
        get() = """
            |    name = "${substanceRecord?.substanceDisplayName()}"
            |    compartment = "${substanceRecord?.compartment()}"
            |${getSubCompartiment()}
            |    reference_unit = ${substanceRecord?.unit()}
            |
            |$impactsSubsection
            |
            |    meta {
            |        type = "${substanceRecord?.type()}"
            |        generator = "kleis-lca-generator"
            |        casNumber = "${substanceRecord?.casNumber()}"
            |        ecNumber = "${substanceRecord?.ecNumber()}"
            |    }
            """.trimMargin()

    val fileContent: String
        get() = substanceRecord?.run { substanceContent } ?: ""

    val lcaFileName: String
        get() = sanitizeString(substanceRecord?.lcaFileName() ?: factorRecords.first().lcaFileName())

}