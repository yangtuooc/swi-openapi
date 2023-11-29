package cn.haloop.swi.openapi.resovler

class SwiPayload {
    var query: MutableList<GoTypeSpecMetadata> = mutableListOf()
    var path: MutableList<GoTypeSpecMetadata> = mutableListOf()
    var body: MutableList<GoTypeSpecMetadata> = mutableListOf()

    companion object {
        fun empty(): SwiPayload = SwiPayload()
    }

    fun appendToBody(element: MutableList<GoTypeSpecMetadata>) {
        body.addAll(element)
    }
}