package mmzk.rm.utilities

enum class OS {
    WINDOWS, LINUX, MAC, SOLARIS, OTHER
}

fun getOS(): OS {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> OS.WINDOWS
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> OS.LINUX
        os.contains("mac") -> OS.MAC
        os.contains("sunos") -> OS.SOLARIS
        else -> OS.OTHER
    }
}
