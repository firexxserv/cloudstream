package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.nodes.Element

class SinemetrikProvider : MainAPI() {
    override var mainUrl = "https://sinemetrik.com"
    override var name = "Sinemetrik"
    override val hasMainPage = true
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.Movie)

    // 1. ARAMA İŞLEMİ
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        
        // DİKKAT: Buradaki "div.result-item" vb. CSS seçicilerini sitenin HTML yapısına göre değiştirmelisin
        return document.select("div.result-item").mapNotNull {
            val title = it.selectFirst("a")?.text() ?: return@mapNotNull null
            val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("src")
            
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
    }

    // 2. FİLM DETAY SAYFASI
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.title")?.text() ?: ""
        val poster = document.selectFirst("div.poster img")?.attr("src")
        val plot = document.selectFirst("div.plot")?.text()

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = plot
        }
    }

    // 3. VİDEO OYNATICI LİNKLERİNİ ÇEKME
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        callback: (ExtractorLink) -> Unit,
        subtitleCallback: (SubtitleFile) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        // Sitenin video oynatıcısı genellikle bir iframe içinde olur (vidmoly, trmplayer vs.)
        val iframeUrl = document.selectFirst("iframe")?.attr("src")
        
        // iframe adresini bulduktan sonra Cloudstream'in hazır Extractor'ları ile videoyu çözebilirsin
        // loadExtractor(iframeUrl, data, subtitleCallback, callback)

        return true
    }
}