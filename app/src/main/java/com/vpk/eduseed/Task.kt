package com.vpk.eduseed

class Task {
    var id: Long = 0
    var text: String
    var subtext: String

    constructor(id: Long, text: String, subtext: String) {
        this.id = id
        this.text = text
        this.subtext = subtext
    }

    constructor(text: String, subtext: String) {
        this.text = text
        this.subtext = subtext
    }
}