package com.gdgnantes.devfest.model.stubs

import com.gdgnantes.devfest.model.Category
import kotlinx.serialization.json.Json

const val CATEGORY_STUBS_JSON =
    "[{\"id\":\"mobile_iot\",\"label\":\"\uD83D\uDCF1 Mobile & IoT\"}," +
            "{\"id\":\"web\",\"label\":\"\uD83C\uDF0D Web\"}," +
            "{\"id\":\"discovery\",\"label\":\"\uD83D\uDCA1 Discovery\"}," +
            "{\"id\":\"cloud_devops\",\"label\":\"☁️ Cloud & DevOps\"}," +
            "{\"id\":\"languages\",\"label\":\"\uD83D\uDCDD Languages\"}," +
            "{\"id\":\"bigdata_ai\",\"label\":\"\uD83E\uDD16 BigData & AI\"}," +
            "{\"id\":\"security\",\"label\":\"\uD83D\uDC31\u200D\uD83D\uDCBB SECURITY\"}," +
            "{\"id\":\"ux_ui\",\"label\":\"\uD83D\uDC9A UX / UI\"}]"

val categoryStubs: List<Category> = Json.decodeFromString(CATEGORY_STUBS_JSON)
