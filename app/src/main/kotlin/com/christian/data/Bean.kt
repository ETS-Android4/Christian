package com.christian.data

data class Disciple(
        val id: String = "",
        val name: String = ""
)

data class Message(
        val id: String,
        var text: String = "",
        var name: String = "",
        var photoUrl: String = "",
        var imageUrl: String = "")

data class MeBean(
        val video: String = "",
        val id: String = "",
        val name: String = "",
        val desc: String = "",
        val content: String = "",
        val author: String = "",
        val church: String = "",
        val time: String = "",
        val userId: String = "",
        var detail: List<MeDetails> = arrayListOf()
)

data class MeDetails(
        val category: String = "",
        val type: String = "",
        val card: String = "",
        val desc: String = "",
        val name: String = "",
        val url: String = "",
        val author: String = ""
)

data class Setting(
        val name: String = "",
        val url: String = "",
        val desc: String = ""
)

// 头像点击开后的详情
data class Address(
        val street: String = "",
        val city: String = "",
        val country: String = ""
)