package com.rbkmoney.porter.service.model

import com.rbkmoney.geck.common.util.TypeUtil
import java.time.LocalDateTime

data class NotificationTemplateFilter(
    val title: String? = null,
    val content: String? = null,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val date: LocalDateTime? = null,
)

fun NotificationTemplateFilter.toKeyParams(): Map<String, String> {
    return HashMap<String, String>().apply {
        title?.let { put("title", title) }
        content?.let { put("content", content) }
        from?.let { put("from", TypeUtil.temporalToString(from)) }
        to?.let { put("to", TypeUtil.temporalToString(to)) }
        date?.let { put("date", TypeUtil.temporalToString(date)) }
    }
}
