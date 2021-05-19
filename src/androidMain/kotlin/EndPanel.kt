import com.soywiz.korge.view.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import java.util.*
import kotlin.random.Random

class EndPanel: Container() {
    val cols = 7
    val displayedCols = 5
    val lines = 15
    val tileSize = 64
    val lineQueue = LinkedList<LinkedList<Image>>()

    private suspend fun init() {
        val tileId = Random.nextInt(0, 7)
        val imageBitmap = resourcesVfs["Panels/$tileId.png"].readBitmap()
        for (i in 0..lines) {
            val list = LinkedList<Image>()
            for(j in 0..cols) {
                val img = image(imageBitmap)
                img.xy(views().virtualWidthDouble - (j * views().virtualWidthDouble / displayedCols), views().virtualHeightDouble - (i * views().virtualWidthDouble / displayedCols))
                img.scale(views().virtualWidthDouble / displayedCols / tileSize)
                list.add(img)
            }
            lineQueue.add(list)
        }
    }

    fun update(virtualHeight: Double, virtualWidth: Double) {
        lineQueue.forEach { list ->
            list.forEach { image ->
                image.y += 2
                image.x += 2
            }
        }

        for(i in 0..lines) {
            for (j in 0..cols) {
                if(lineQueue[i].first.x > virtualWidth) {
                    val img = lineQueue[i].removeFirst()
                    img.xy(lineQueue[i].last.x - virtualWidth / displayedCols, img.y)
                    lineQueue[i].add(img)
                }
            }

            if(lineQueue.first.first().y > virtualHeight) {
                val l = lineQueue.removeFirst()
                l.forEach { it.xy(it.x, lineQueue.last.first().y - virtualWidth / displayedCols) }
                lineQueue.add(l)
            }
        }
    }

    companion object {
        suspend fun create(): EndPanel {
            return EndPanel().also {
                it.init()
            }
        }
    }
}