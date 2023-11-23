package cn.haloop.swi.helper.service

/**
 * @author yangtuo
 */
open class SwiConversionService {

    fun convert(text: String?): String {
        return text?.replace(" ", "_") ?: ""
    }
}