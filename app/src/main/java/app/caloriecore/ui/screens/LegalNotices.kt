package app.caloriecore.ui.screens

import app.caloriecore.ui.text.CalorieCoreStrings

internal enum class LegalNoticeKind {
    DataSource,
    SoftwareLicense,
    ServiceTerms
}

internal data class LegalNotice(
    val id: String,
    val kind: LegalNoticeKind,
    val title: String,
    val terms: String,
    val details: String,
    val url: String
)

internal fun legalNotices(strings: CalorieCoreStrings): List<LegalNotice> = listOf(
    LegalNotice(
        id = "adult-compendium-2024",
        kind = LegalNoticeKind.DataSource,
        title = "2024 Adult Compendium of Physical Activities",
        terms = "Herrmann SD et al. Journal of Sport and Health Science, 2024;13(1):6-12.",
        details = strings.compendiumLegalText,
        url = "https://pacompendium.com/"
    ),
    LegalNotice(
        id = "open-food-facts",
        kind = LegalNoticeKind.DataSource,
        title = "Open Food Facts",
        terms = "Open Database License (ODbL)",
        details = strings.openFoodFactsLegalText,
        url = "https://world.openfoodfacts.org/terms-of-use"
    ),
    LegalNotice(
        id = "calorie-api",
        kind = LegalNoticeKind.DataSource,
        title = "Calorie API",
        terms = "API Terms and Conditions",
        details = strings.calorieApiLegalText,
        url = "https://calorieapi.com/terms"
    ),
    LegalNotice(
        id = "android-libraries",
        kind = LegalNoticeKind.SoftwareLicense,
        title = "AndroidX and Jetpack Compose",
        terms = "Apache License 2.0",
        details = strings.androidLibrariesLegalText,
        url = "https://www.apache.org/licenses/LICENSE-2.0"
    ),
    LegalNotice(
        id = "kotlin",
        kind = LegalNoticeKind.SoftwareLicense,
        title = "Kotlin",
        terms = "Apache License 2.0",
        details = strings.kotlinLegalText,
        url = "https://www.apache.org/licenses/LICENSE-2.0"
    ),
    LegalNotice(
        id = "google-play-services",
        kind = LegalNoticeKind.ServiceTerms,
        title = "Google Play services",
        terms = "Google APIs Terms of Service",
        details = strings.googleServicesLegalText,
        url = "https://developers.google.com/terms"
    )
)
