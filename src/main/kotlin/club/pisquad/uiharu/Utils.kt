package club.pisquad.uiharu

fun String.trimQuotes(): String {
    return this.replace("\"", "")
}