val md = "This is a note with #politics and #política_2024\n#end"
val tags = Regex("""#[\p{L}\p{N}_-]+""").findAll(md).map { it.value }.distinct().toList()
println(tags)
