package com.voc2048.sparkle_study.utils

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings
import com.voc2048.sparkle_study.appLanguageState
import com.voc2048.sparkle_study.changeLanguage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * App 語言與文字語言管理類。
 */
class Language {

    @Serializable
    enum class AppLanguage(val localeName: String, val folderName: String, val localeCode: String, val hoyolabName: String) {
        VOCCHINESE("粵語", "yue", "zh-MO", "zh-tw"),
        EN("English", "en", "en", "en-us"),
        ZH_CN("简体中文", "zh_cn", "zh-CN", "zh-cn"),
        ZH_HK("繁體中文", "zh_hk", "zh-HK", "zh-tw"),
        JP("日本語", "jp", "ja-JP", "ja-jp"),
        FR("Français", "fr", "fr-FR", "fr-fr"),
        RU("Русский", "ru", "ru-RU", "ru-ru"),
        DE("Deutsch", "de", "de-DE", "de-de"),
        PT("Português", "pt_pt", "pt-PT", "pt-pt"),
        VI("tiếng Việt", "vi", "vi", "vi-vn"),
        ES("Español", "es_es", "es-ES", "es-es"),
        KR("한국어", "kr", "ko-KR", "ko-kr"),
        TH("ภาษาไทย", "th", "th-TH", "th-th"),
        JYU_YAM("ㄓㄨˋ ㄧㄣ", "zh", "zh-TW", "zh-tw"),
        ARABIC_HALAL("عربي", "ar", "ar", "ar"),
        UK("Українська", "uk", "uk", "en");
    }

    object TextLanguageSerializer : KSerializer<TextLanguage> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TextLanguage", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: TextLanguage) = encoder.encodeString(value.name)
        override fun deserialize(decoder: Decoder): TextLanguage = TextLanguage.valueOf(decoder.decodeString())
    }

    @Serializable
    enum class TextLanguage(val localeName: String, val folderName: String, val hoyolabName: String, val langCode: String) {
        @SerialName("en") EN("English", "en", "en-us", "en"),
        @SerialName("zh_cn") ZH_CN("简体中文", "zh_cn", "zh-cn", "cn"),
        @SerialName("zh_hk") ZH_HK("繁體中文", "zh_hk", "zh-tw", "cht"),
        @SerialName("jp") JP("日本語", "jp", "ja-jp", "jp"),
        @SerialName("fr") FR("Français", "fr", "fr-fr", "fr"),
        @SerialName("ru") RU("Русский", "ru", "ru-ru", "ru"),
        @SerialName("de") DE("Deutsch", "de", "de-de", "de"),
        @SerialName("pt") PT("Português", "pt", "pt-pt", "pt"),
        @SerialName("vi") VI("tiếng Việt", "vi", "vi-vn", "vi"),
        @SerialName("es") ES("Español", "es", "es-es", "es"),
        @SerialName("kr") KR("한국어", "kr", "ko-kr", "kr"),
        @SerialName("th") TH("ภาษาไทย", "th", "th-th", "th"),
        @SerialName("it") IT("ITALIAN", "it", "it-it", "it"),
        @SerialName("tr") TR("TURKISH", "tr", "tr-tr", "tr"),
        @SerialName("id") ID("INDONESIAN", "id", "id-id", "id"),
    }

    companion object {
        var TextLanguageInstance = TextLanguage.valueOf(Settings().getString("textLanguage", TextLanguage.EN.name))
        var AppLanguageInstance = AppLanguage.valueOf(Settings().getString("appLanguage", AppLanguage.EN.name))

        fun getTextLanguageByLocaleName(localeName: String): TextLanguage {
            return TextLanguage.entries.firstOrNull { it.localeName == localeName } ?: TextLanguage.EN
        }
    }

    fun setAppLanguage(lang: AppLanguage = AppLanguageInstance, isFirstInit: Boolean = false) {
        Settings().putString("appLanguage", lang.name)
        val langC = lang.localeCode.split("-")
        changeLanguage(langC[0], langC.getOrNull(1))
        AppLanguageInstance = lang
        
        // 使用安全的方式更新全域狀態，避免 lateinit 未初始化錯誤
        updateLanguageState(lang)

        if (isFirstInit) {
            TextLanguageInstance = TextLanguage.entries.firstOrNull { it.folderName == lang.folderName } ?: TextLanguage.EN
            setTextLanguage(TextLanguageInstance, isFirstInit)
        }
    }

    private fun updateLanguageState(lang: AppLanguage) {
        try {
            appLanguageState.value = lang
        } catch (e: Exception) {
            // appLanguageState 可能尚未在 App() 中初始化
        }
    }

    fun setTextLanguage(lang: TextLanguage, isFirstInit: Boolean = false) {
        Settings().putString("textLanguage", lang.name)
        TextLanguageInstance = lang
    }

    fun getAppLangLocaleNameList(): List<String> = AppLanguage.entries.map { it.localeName }
    fun getAppLangEnumList(): List<AppLanguage> = AppLanguage.entries.toList()
    fun getTextLangLocaleNameList(): List<String> = TextLanguage.entries
        .filter { it != TextLanguage.IT && it != TextLanguage.TR && it != TextLanguage.ID }
        .map { it.localeName }
    fun getTextLangEnumList(): List<TextLanguage> = TextLanguage.entries.toList()
}

@Composable
fun removeStrQuote(stringResource: StringResource): String {
    return stringResource(stringResource).removePrefix("\"").removeSuffix("\"")
}
