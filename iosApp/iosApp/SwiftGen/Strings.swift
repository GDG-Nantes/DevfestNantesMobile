// swiftlint:disable all
// Generated using SwiftGen — https://github.com/SwiftGen/SwiftGen

import Foundation

// swiftlint:disable superfluous_disable_command file_length implicit_return prefer_self_in_static_references

// MARK: - Strings

// swiftlint:disable explicit_type_interface function_parameter_count identifier_name line_length
// swiftlint:disable nesting type_body_length type_name vertical_whitespace_opening_braces
internal enum L10n {
  /// Code of conduct
  internal static let aboutCodeOfConduct = L10n.tr("Localizable", "about_code_of_conduct", fallback: "Code of conduct")
  /// Follow us
  internal static let aboutSocialTitle = L10n.tr("Localizable", "about_social_title", fallback: "Follow us")
  /// Terms of service
  internal static let aboutTermsOfService = L10n.tr("Localizable", "about_terms_of_service", fallback: "Terms of service")
  /// Website
  internal static let aboutWebsite = L10n.tr("Localizable", "about_website", fallback: "Website")
  /// Back
  internal static let actionBack = L10n.tr("Localizable", "action_back", fallback: "Back")
  /// empty
  internal static let agendaEmpty = L10n.tr("Localizable", "agenda_empty", fallback: "empty")
  /// DevFest Nantes
  internal static let appName = L10n.tr("Localizable", "app_name", fallback: "DevFest Nantes")
  /// Version %s (%d)
  internal static func appVersion(_ p1: UnsafePointer<CChar>, _ p2: Int) -> String {
    return L10n.tr("Localizable", "app_version", p1, p2, fallback: "Version %s (%d)")
  }
  /// Favorite
  internal static let bookmarked = L10n.tr("Localizable", "bookmarked", fallback: "Favorite")
  /// Cancel
  internal static let btnCancel = L10n.tr("Localizable", "btn_cancel", fallback: "Cancel")
  /// OK
  internal static let btnOk = L10n.tr("Localizable", "btn_ok", fallback: "OK")
  /// Customize
  internal static let buttonDialogDataCollectionConsentCustomize = L10n.tr("Localizable", "button_dialog_data_collection_consent_customize", fallback: "Customize")
  /// Advanced
  internal static let complexityAdvanced = L10n.tr("Localizable", "complexity_advanced", fallback: "Advanced")
  /// Beginer
  internal static let complexityBeginer = L10n.tr("Localizable", "complexity_beginer", fallback: "Beginer")
  /// Intermediate
  internal static let complexityIntermediate = L10n.tr("Localizable", "complexity_intermediate", fallback: "Intermediate")
  /// DevFest Nantes about logo header.
  internal static let contentDescriptionAboutHeader = L10n.tr("Localizable", "content_description_about_header", fallback: "DevFest Nantes about logo header.")
  /// Lock icon.
  internal static let contentDescriptionDataCollectionLogo = L10n.tr("Localizable", "content_description_data_collection_logo", fallback: "Lock icon.")
  /// Logo of the Local communities. Click to open the website.
  internal static let contentDescriptionLocalCommunitiesLogo = L10n.tr("Localizable", "content_description_local_communities_logo", fallback: "Logo of the Local communities. Click to open the website.")
  /// %1$s logo. Link to the web page.
  internal static func contentDescriptionLogo(_ p1: UnsafePointer<CChar>) -> String {
    return L10n.tr("Localizable", "content_description_logo", p1, fallback: "%1$s logo. Link to the web page.")
  }
  /// Github logo
  internal static let contentDescriptionLogoGithub = L10n.tr("Localizable", "content_description_logo_github", fallback: "Github logo")
  /// %1$s's profile picture.
  internal static func contentDescriptionSpeakerPicture(_ p1: UnsafePointer<CChar>) -> String {
    return L10n.tr("Localizable", "content_description_speaker_picture", p1, fallback: "%1$s's profile picture.")
  }
  /// Website icon. Link to the speaker's website.
  internal static let contentDescriptionSpeakerWebsiteIcon = L10n.tr("Localizable", "content_description_speaker_website_icon", fallback: "Website icon. Link to the speaker's website.")
  /// October 16, 2025
  internal static let day1 = L10n.tr("Localizable", "day1", fallback: "October 16, 2025")
  /// October 17, 2025
  internal static let day2 = L10n.tr("Localizable", "day2", fallback: "October 17, 2025")
  /// Sorry, there is no conference available for the selected filters.
  internal static let emptyDay = L10n.tr("Localizable", "empty_day", fallback: "Sorry, there is no conference available for the selected filters.")
  /// Clear
  internal static let filterClear = L10n.tr("Localizable", "filter_clear", fallback: "Clear")
  /// Favorites
  internal static let filterFavorites = L10n.tr("Localizable", "filter_favorites", fallback: "Favorites")
  /// Fork me on github
  internal static let forkMeOnGithub = L10n.tr("Localizable", "fork_me_on_github", fallback: "Fork me on github")
  /// english
  internal static let languageEnglish = L10n.tr("Localizable", "language_english", fallback: "english")
  /// french
  internal static let languageFrench = L10n.tr("Localizable", "language_french", fallback: "french")
  /// Accept all
  internal static let legalDataCollectionAcceptAll = L10n.tr("Localizable", "legal_data_collection_accept_all", fallback: "Accept all")
  /// The anonymous data you may share with us allows us to improve the app.
  internal static let legalDataCollectionBody = L10n.tr("Localizable", "legal_data_collection_body", fallback: "The anonymous data you may share with us allows us to improve the app.")
  /// We use third party tools to measure and improve performances. No commercial use is made of this data. By clicking on "Consent" or by activating these tools in the settings, you consent to the use of these services and technologies.
  internal static let legalDataCollectionConsentDialogBody = L10n.tr("Localizable", "legal_data_collection_consent_dialog_body", fallback: "We use third party tools to measure and improve performances. No commercial use is made of this data. By clicking on \"Consent\" or by activating these tools in the settings, you consent to the use of these services and technologies.")
  /// Consent
  internal static let legalDataCollectionConsentDialogButtonConsent = L10n.tr("Localizable", "legal_data_collection_consent_dialog_button_consent", fallback: "Consent")
  /// We use Firebase Crashlytics to collect anonymous data when a bug occurs. It helps us to improve the stability of the app.
  internal static let legalDataCollectionFirebaseCrashlyticsDescription = L10n.tr("Localizable", "legal_data_collection_firebase_crashlytics_description", fallback: "We use Firebase Crashlytics to collect anonymous data when a bug occurs. It helps us to improve the stability of the app.")
  /// Firebase Crashlytics
  internal static let legalDataCollectionFirebaseCrashlyticsTitle = L10n.tr("Localizable", "legal_data_collection_firebase_crashlytics_title", fallback: "Firebase Crashlytics")
  /// We use Google Analytics to collect anonymous data to measure how users interact with the app.
  internal static let legalDataCollectionGoogleAnalyticsDescription = L10n.tr("Localizable", "legal_data_collection_google_analytics_description", fallback: "We use Google Analytics to collect anonymous data to measure how users interact with the app.")
  /// Google Analytics
  internal static let legalDataCollectionGoogleAnalyticsTitle = L10n.tr("Localizable", "legal_data_collection_google_analytics_title", fallback: "Google Analytics")
  /// Loading...
  internal static let loading = L10n.tr("Localizable", "loading", fallback: "Loading...")
  /// Local communities
  internal static let localCommunitiesTitle = L10n.tr("Localizable", "local_communities_title", fallback: "Local communities")
  /// EXCLUSIVE PLATINIUM PARTNER
  internal static let partnersExclusivePlatiniumTitle = L10n.tr("Localizable", "partners_exclusive_platinium_title", fallback: "EXCLUSIVE PLATINIUM PARTNER")
  /// GOLD
  internal static let partnersGoldTitle = L10n.tr("Localizable", "partners_gold_title", fallback: "GOLD")
  /// PLATINIUM
  internal static let partnersPlatiniumTitle = L10n.tr("Localizable", "partners_platinium_title", fallback: "PLATINIUM")
  /// Partners
  internal static let partnersTitle = L10n.tr("Localizable", "partners_title", fallback: "Partners")
  /// VELOTYPE
  internal static let partnersVelotypeTitle = L10n.tr("Localizable", "partners_velotype_title", fallback: "VELOTYPE")
  /// VIRTUAL
  internal static let partnersVirtualTitle = L10n.tr("Localizable", "partners_virtual_title", fallback: "VIRTUAL")
  /// Session details
  internal static let placeholderSessionDetails = L10n.tr("Localizable", "placeholder_session_details", fallback: "Session details")
  /// Plan of the conference
  internal static let plan = L10n.tr("Localizable", "plan", fallback: "Plan of the conference")
  /// Powered by OpenFeedback
  internal static let poweredOpenfeedback = L10n.tr("Localizable", "powered_openfeedback", fallback: "Powered by OpenFeedback")
  /// About
  internal static let screenAbout = L10n.tr("Localizable", "screen_about", fallback: "About")
  /// The Devfest (i.e Developers Festival), is a technical conference destined to developers. It's meant for students, professionals, or any curious techie
  /// This year step into the magical world of legends and tales!
  internal static let screenAboutHeaderBody = L10n.tr("Localizable", "screen_about_header_body", fallback: "The Devfest (i.e Developers Festival), is a technical conference destined to developers. It's meant for students, professionals, or any curious techie\nThis year step into the magical world of legends and tales!")
  /// Agenda
  internal static let screenAgenda = L10n.tr("Localizable", "screen_agenda", fallback: "Agenda")
  /// Data collection
  internal static let screenDataCollection = L10n.tr("Localizable", "screen_data_collection", fallback: "Data collection")
  /// Home
  internal static let screenHome = L10n.tr("Localizable", "screen_home", fallback: "Home")
  /// Session details
  internal static let screenSession = L10n.tr("Localizable", "screen_session", fallback: "Session details")
  /// Settings
  internal static let screenSettings = L10n.tr("Localizable", "screen_settings", fallback: "Settings")
  /// Speaker
  internal static let screenSpeaker = L10n.tr("Localizable", "screen_speaker", fallback: "Speaker")
  /// Speakers
  internal static let screenSpeakers = L10n.tr("Localizable", "screen_speakers", fallback: "Speakers")
  /// Venue
  internal static let screenVenue = L10n.tr("Localizable", "screen_venue", fallback: "Venue")
  /// Provide feedback
  internal static let sessionFeedbackLabel = L10n.tr("Localizable", "session_feedback_label", fallback: "Provide feedback")
  /// Open session filters
  internal static let sessionFiltersAction = L10n.tr("Localizable", "session_filters_action", fallback: "Open session filters")
  /// Complexity
  internal static let sessionFiltersDrawerComplexityLabel = L10n.tr("Localizable", "session_filters_drawer_complexity_label", fallback: "Complexity")
  /// Languages
  internal static let sessionFiltersDrawerLanguagesLabel = L10n.tr("Localizable", "session_filters_drawer_languages_label", fallback: "Languages")
  /// Rooms
  internal static let sessionFiltersDrawerRoomsLabel = L10n.tr("Localizable", "session_filters_drawer_rooms_label", fallback: "Rooms")
  /// Filters
  internal static let sessionFiltersDrawerTitle = L10n.tr("Localizable", "session_filters_drawer_title", fallback: "Filters")
  /// Session type
  internal static let sessionFiltersDrawerTypeLabel = L10n.tr("Localizable", "session_filters_drawer_type_label", fallback: "Session type")
  /// Codelab
  internal static let sessionTypeCodelab = L10n.tr("Localizable", "session_type_codelab", fallback: "Codelab")
  /// Conference
  internal static let sessionTypeConference = L10n.tr("Localizable", "session_type_conference", fallback: "Conference")
  /// Quickie
  internal static let sessionTypeQuickie = L10n.tr("Localizable", "session_type_quickie", fallback: "Quickie")
  /// Open application settings.
  internal static let settingsAction = L10n.tr("Localizable", "settings_action", fallback: "Open application settings.")
  /// Customize data collection
  internal static let settingsDataCollectionSubtitle = L10n.tr("Localizable", "settings_data_collection_subtitle", fallback: "Customize data collection")
  /// Legal notices
  internal static let settingsLegal = L10n.tr("Localizable", "settings_legal", fallback: "Legal notices")
  /// Version
  internal static let settingsVersionLabel = L10n.tr("Localizable", "settings_version_label", fallback: "Version")
  /// Support
  internal static let settingsVersionSupport = L10n.tr("Localizable", "settings_version_support", fallback: "Support")
  /// Report an issue.
  internal static let settingsVersionSupportSubtitle = L10n.tr("Localizable", "settings_version_support_subtitle", fallback: "Report an issue.")
  /// MainActivity
  internal static let titleActivityMain = L10n.tr("Localizable", "title_activity_main", fallback: "MainActivity")
  /// Go to
  internal static let venueGoToButton = L10n.tr("Localizable", "venue_go_to_button", fallback: "Go to")
  /// Picture of the conference's venue.
  internal static let venueImageContentDescription = L10n.tr("Localizable", "venue_image_content_description", fallback: "Picture of the conference's venue.")
  /// Plan of the conference's venue. Shows the different spaces and conferences rooms.
  internal static let venuePlanContentDescription = L10n.tr("Localizable", "venue_plan_content_description", fallback: "Plan of the conference's venue. Shows the different spaces and conferences rooms.")
  /// You will be redirected to an external web page. We can not be held responsible for the content available on the site you are going to visit.
  internal static let webExternalDisclaimerText = L10n.tr("Localizable", "web_external_disclaimer_text", fallback: "You will be redirected to an external web page. We can not be held responsible for the content available on the site you are going to visit.")
  /// Redirection
  internal static let webExternalDisclaimerTitle = L10n.tr("Localizable", "web_external_disclaimer_title", fallback: "Redirection")
}
// swiftlint:enable explicit_type_interface function_parameter_count identifier_name line_length
// swiftlint:enable nesting type_body_length type_name vertical_whitespace_opening_braces

// MARK: - Implementation Details

extension L10n {
  private static func tr(_ table: String, _ key: String, _ args: CVarArg..., fallback value: String) -> String {
    let format = BundleToken.bundle.localizedString(forKey: key, value: value, table: table)
    return String(format: format, locale: Locale.current, arguments: args)
  }
}

// swiftlint:disable convenience_type
private final class BundleToken {
  static let bundle: Bundle = {
    #if SWIFT_PACKAGE
    return Bundle.module
    #else
    return Bundle(for: BundleToken.self)
    #endif
  }()
}
// swiftlint:enable convenience_type
