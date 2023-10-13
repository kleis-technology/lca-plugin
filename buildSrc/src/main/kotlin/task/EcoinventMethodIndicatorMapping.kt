package task

data class MethodIndicator(
    val categoryName: String,
    val unit: String,
)

/*
    TODO: Seems like a bad idea ... but how can we make it work with ecoinvent lcia or upr ?
 */
object EcoinventMethodIndicatorMapping {
    operator fun get(s: String): MethodIndicator {
        return when (s) {
            "Acidification" -> MethodIndicator(
                sanitize("acidification"),
                sanitize("mol H+-Eq", toLowerCase = false),
            )

            "Climate change" -> MethodIndicator(
                sanitize("climate change"),
                sanitize("kg CO2-Eq", toLowerCase = false),
            )

            "Climate change-Biogenic" -> MethodIndicator(
                sanitize("climate change: biogenic"),
                sanitize("kg CO2-Eq", toLowerCase = false),
            )

            "Climate change-Fossil" -> MethodIndicator(
                sanitize("climate change: fossil"),
                sanitize("kg CO2-Eq", toLowerCase = false),
            )

            "Climate change-Land use and land use change" -> MethodIndicator(
                sanitize("climate change: land use and land use change"),
                sanitize("kg CO2-Eq", toLowerCase = false),
            )

            "Ecotoxicity, freshwater" -> MethodIndicator(
                sanitize("ecotoxicity: freshwater"),
                sanitize("CTUe", toLowerCase = false),
            )

            "Ecotoxicity, freshwater_inorganics" -> MethodIndicator(
                sanitize("ecotoxicity: freshwater, inorganics"),
                sanitize("CTUe", toLowerCase = false),
            )

            "Ecotoxicity, freshwater_metals" -> MethodIndicator(
                sanitize("ecotoxicity: freshwater, metals"),
                sanitize("CTUe", toLowerCase = false),
            )

            "Ecotoxicity, freshwater_organics" -> MethodIndicator(
                sanitize("ecotoxicity: freshwater, organics"),
                sanitize("CTUe", toLowerCase = false),
            )

            "EF-particulate Matter" -> MethodIndicator(
                sanitize("particulate matter formation"),
                sanitize("disease incidence", toLowerCase = false),
            )

            "Eutrophication marine" -> MethodIndicator(
                sanitize("eutrophication: marine"),
                sanitize("kg N-Eq", toLowerCase = false),
            )

            "Eutrophication, freshwater" -> MethodIndicator(
                sanitize("eutrophication: freshwater"),
                sanitize("kg P-Eq", toLowerCase = false),
            )

            "Eutrophication, terrestrial" -> MethodIndicator(
                sanitize("eutrophication: terrestrial"),
                sanitize("mol N-Eq", toLowerCase = false),
            )

            "Human toxicity, cancer" -> MethodIndicator(
                sanitize("human toxicity: carcinogenic"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, cancer_inorganics" -> MethodIndicator(
                sanitize("human toxicity: non-carcinogenic, inorganics"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, cancer_metals" -> MethodIndicator(
                sanitize("human toxicity: carcinogenic, metals"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, cancer_organics" -> MethodIndicator(
                sanitize("human toxicity: carcinogenic, organics"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, non-cancer" -> MethodIndicator(
                sanitize("human toxicity: non-carcinogenic"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, non-cancer_inorganics" -> MethodIndicator(
                sanitize("human toxicity: non-carcinogenic, inorganics"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, non-cancer_metals" -> MethodIndicator(
                sanitize("human toxicity: non-carcinogenic, metals"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Human toxicity, non-cancer_organics" -> MethodIndicator(
                sanitize("human toxicity: non-carcinogenic, organics"),
                sanitize("CTUh", toLowerCase = false),
            )

            "Ionising radiation, human health" -> MethodIndicator(
                sanitize("ionising radiation: human health"),
                sanitize("kBq U235-Eq", toLowerCase = false),
            )

            "Land use" -> MethodIndicator(
                sanitize("_land use"),
                sanitize("dimensionless", toLowerCase = false),
            )

            "Ozone depletion" -> MethodIndicator(
                sanitize("ozone depletion"),
                sanitize("kg CFC-11-Eq", toLowerCase = false),
            )

            "Photochemical ozone formation - human health" -> MethodIndicator(
                sanitize("photochemical oxidant formation: human health"),
                sanitize("kg NMVOC-Eq", toLowerCase = false),
            )

            "Resource use, fossils" -> MethodIndicator(
                sanitize("energy resources: non-renewable"),
                sanitize("MJ, net calorific value", toLowerCase = false),
            )

            "Resource use, minerals and metals" -> MethodIndicator(
                sanitize("material resources: metals/minerals"),
                sanitize("kg Sb-Eq", toLowerCase = false),
            )

            "Water use" -> MethodIndicator(
                sanitize("water use"),
                sanitize("m3 world eq. deprived", toLowerCase = false),
            )

            else -> throw IllegalArgumentException("unknown indicator $s")
        }
    }
}

// TODO: Find a way to write this function once and for all
fun sanitize(s: String, toLowerCase: Boolean = true): String {
    if (s.isBlank()) {
        return s
    }

    val r = if (s[0].isDigit()) "_$s" else s
    val spaces = """\s+""".toRegex()
    val nonAlphaNumeric = """[^a-zA-Z0-9_]+""".toRegex()
    val underscores = Regex("_{2,}")

    return r.let {
        if (toLowerCase) it.lowercase()
        else it
    }.trim()
        .replace(spaces, "_")
        .replace("*", "_m_")
        .replace("+", "_p_")
        .replace("&", "_a_")
        .replace(">", "_gt_")
        .replace("<", "_lt_")
        .replace("/", "_sl_")
        .replace(nonAlphaNumeric, "_")
        .replace(underscores, "_")
        .trimEnd('_')
}
