package jp.ac.neec.it.k023c0039.jobcalendar

data class Company(
    val id: Long,
    val name: String,     // 会社名
    val details: String?  // Geminiが生成した詳細テキスト
)