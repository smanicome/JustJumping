import com.soywiz.korge.view.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.random.get
import java.util.*
import kotlin.random.Random

class Platforms private constructor(): Container() {
    var gap = 0.0
    val platformCount = 4
    val platforms = LinkedList<Image>()

    private suspend fun init() {
        gap = (views().virtualHeight / platformCount).toDouble()
        for (i in 0..platformCount) {
            val platform = image(resourcesVfs["Background/platform.png"].readBitmap())
            platform.scale(views().virtualHeight * 0.07 / platform.height)
            val scaledWidth = platform.width * platform.scaleX
            platform.xy((Random[0.0, views().virtualWidth - scaledWidth]), (views().virtualHeight - i * gap) )
            platforms.add(platform)
        }

        val scaledWidth = platforms.first.width * platforms.first.scaleX
        platforms.first.xy(views().virtualWidth / 2 - scaledWidth / 2, views().virtualHeight * 0.95)
    }

    fun scrollDown(offset: Double) {
        platforms.forEach { it.xy(it.x, it.y + offset) }
    }

    fun update(virtualHeight: Double, virtualWidth: Double) {
        for(i in 0..platformCount) {
            if(platforms[i].y > virtualHeight) {
                val p = platforms.removeFirst()
                val scaledWidth = p.width * platforms.first.scaleX
                p.xy((Random[0.0, virtualWidth - scaledWidth]), platforms.last().y - gap)
                platforms.add(p)
            }
        }
    }

    companion object {
        suspend fun create(): Platforms {
            return Platforms().also {
                it.init()
            }
        }
    }
}