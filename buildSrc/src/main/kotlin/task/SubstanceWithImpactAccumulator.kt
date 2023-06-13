package task

class SubstanceWithImpactAccumulator {

    private val factorRecords = mutableListOf<EFRecord>()
    private var substanceRecord: EFRecord? = null

    fun addElement(element: EFRecord): SubstanceWithImpactAccumulator {
        if (element.isSubstance()) {
            substanceRecord = element
        } else {
            factorRecords.add(element)
        }
        return this
    }


    val fileContent: String
        get() = substanceRecord?.run { substanceContent } ?: ""

    val lcaFileName: String
        get() = sanitizeString(substanceRecord?.lcaFileName() ?: factorRecords.first().lcaFileName())

    val substanceName: String
        get() = substanceRecord?.substanceName() ?: ""
    val substanceType: String
        get() = substanceRecord?.type() ?: ""
    val substanceCompartment: String
        get() = substanceRecord?.compartment() ?: ""
    val substanceSubCompartment: String
        get() = substanceRecord?.subCompartment() ?: ""

    private val substanceContent: String
        get() = """
            |substance ${substanceRecord?.sanitizedSubstanceName()} {
            |
            |$substanceBody
            |
            |}
        """.trimMargin()


    private val substanceBody: String
        get() = """
            |    name = "${substanceRecord?.substanceDisplayName()}"
            |    type = $substanceType
            |    compartment = "$substanceCompartment"
            |${getSubCompartment()}
            |    reference_unit = ${substanceRecord?.unit()}
            |
            |$impactsSubsection
            |
            |    meta {
            |        "generator" = "kleis-lca-generator"
            |        "casNumber" = "${substanceRecord?.casNumber()}"
            |        "ecNumber"  = "${substanceRecord?.ecNumber()}"
            |    }
            """.trimMargin()

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

    private fun getSubCompartment(): String {
        val sub = substanceSubCompartment
        @Suppress("SameParameterValue")
        return if (sub.isNullOrBlank()) {
            ""
        } else {
            "    sub_compartment = \"$sub\""
        }
    }


}
