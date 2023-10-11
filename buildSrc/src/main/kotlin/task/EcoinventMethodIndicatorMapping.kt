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
                "acidification",
                "mol_H_p_Eq",
            )

            "Climate change" -> MethodIndicator(
                "climate_change",
                "kg_CO2_Eq",
            )

            "Climate change-Biogenic" -> MethodIndicator(
                "climate_change_biogenic",
                "kg_CO2_Eq",
            )

            "Climate change-Fossil" -> MethodIndicator(
                "climate_change_fossil",
                "kg_CO2_Eq",
            )

            "Climate change-Land use and land use change" -> MethodIndicator(
                "climate_change_land_use_and_land_use_change",
                "kg_CO2_Eq"
            )

            "Ecotoxicity, freshwater" -> MethodIndicator(
                "ecotoxicity_freshwater",
                "CTUe",
            )

            "Ecotoxicity, freshwater_inorganics" -> MethodIndicator(
                "ecotoxicity_freshwater_inorganics",
                "CTUe",
            )

            "Ecotoxicity, freshwater_metals" -> MethodIndicator(
                "ecotoxicity_freshwater_metals",
                "CTUe",
            )

            "Ecotoxicity, freshwater_organics" -> MethodIndicator(
                "ecotoxicity_freshwater_organics",
                "CTUe",
            )

            "EF-particulate Matter" -> MethodIndicator(
                "particulate_matter_formation",
                "disease_incidence",
            )

            "Eutrophication marine" -> MethodIndicator(
                "eutrophication_marine",
                "kg_N_Eq",
            )

            "Eutrophication, freshwater" -> MethodIndicator(
                "eutrophication_freshwater",
                "kg_P_Eq",
            )

            "Eutrophication, terrestrial" -> MethodIndicator(
                "eutrophication_terrestrial",
                "mol_N_Eq"
            )

            "Human toxicity, cancer" -> MethodIndicator(
                "human_toxicity_carcinogenic",
                "CTUh",
            )

            "Human toxicity, cancer_inorganics" -> MethodIndicator(
                "human_toxicity_carcinogenic_inorganics",
                "CTUh",
            )

            "Human toxicity, cancer_metals" -> MethodIndicator(
                "human_toxicity_carcinogenic_metals",
                "CTUh",
            )

            "Human toxicity, cancer_organics" -> MethodIndicator(
                "human_toxicity_carcinogenic_organics",
                "CTUh",
            )

            "Human toxicity, non-cancer" -> MethodIndicator(
                "human_toxicity_non_carcinogenic",
                "CTUh",
            )

            "Human toxicity, non-cancer_inorganics" -> MethodIndicator(
                "human_toxicity_non_carcinogenic_inorganics",
                "CTUh",
            )

            "Human toxicity, non-cancer_metals" -> MethodIndicator(
                "human_toxicity_non_carcinogenic_metals",
                "CTUh",
            )

            "Human toxicity, non-cancer_organics" -> MethodIndicator(
                "human_toxicity_non_carcinogenic_organics",
                "CTUh"
            )

            "Ionising radiation, human health" -> MethodIndicator(
                "ionising_radiation_human_health",
                "kBq_U235_Eq",
            )

            "Land use" -> MethodIndicator(
                "_land_use",
                "dimensionless",
            )

            "Ozone depletion" -> MethodIndicator(
                "ozone_depletion",
                "kg_CFC_11_Eq",
            )

            "Photochemical ozone formation - human health" -> MethodIndicator(
                "photochemical_oxidant_formation_human_health",
                "kg_NMVOC_Eq",
            )

            "Resource use, fossils" -> MethodIndicator(
                "energy_resources_non_renewable",
                "MJ_net_calorific_value",
            )

            "Resource use, minerals and metals" -> MethodIndicator(
                "material_resources_metals_sl_minerals",
                "kg_Sb_Eq",
            )

            "Water use" -> MethodIndicator(
                "water_use",
                "m3_world_eq_deprived",
            )

            else -> throw IllegalArgumentException("unknown indicator $s")
        }
    }
}
