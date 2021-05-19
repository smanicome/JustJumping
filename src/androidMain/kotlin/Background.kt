import com.soywiz.korge.view.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.degrees

class Background private constructor(): Container() {

    private suspend fun init() {
        createBackground()
    }

    companion object {
        suspend fun create(): Background {
            return Background().also {
                it.init()
            }
        }
    }

    suspend fun createBackground() {
        val sky = image(resourcesVfs["Background/sky.png"].readBitmap())
        sky.scale((views().virtualWidth) / sky.width, views().virtualHeight / sky.height)
        val mountains = image(resourcesVfs["Background/far-grounds.png"].readBitmap())
        mountains.scale(views().virtualWidth / mountains.width, 1.0).xy(0.0, views().virtualHeight * 0.8)
        val scaledMountainHeight = mountains.height * mountains.scaleY

        val sea = image(resourcesVfs["Background/sea.png"].readBitmap())
        sea.scale(views().virtualWidth / sea.width, (views().virtualHeight - (mountains.y + scaledMountainHeight)) / sea.height)
        sea.y = mountains.y + scaledMountainHeight
        val cloud = image(resourcesVfs["Background/clouds.png"].readBitmap()).xy(0.0, views().virtualHeight * -0.03)
        cloud.scale(views().virtualWidth / cloud.width, views().virtualHeight / 10 / cloud.height)
        val secondCloud = cloud.clone().xy(views().virtualWidth.toDouble(), views().virtualHeight * 0.15).rotation(180.0.degrees)
        addChild(secondCloud)
    }
}