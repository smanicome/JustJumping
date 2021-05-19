import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.random.Random

class Jumper private constructor(): Container() {
    var score = 0.0
    lateinit var sprite: Sprite
    private var gravity = 1.0
    var velocity = 0.0;
    lateinit var jump: SpriteAnimation
    lateinit var fall: SpriteAnimation
    lateinit var roll: SpriteAnimation
    lateinit var idle: SpriteAnimation
    lateinit var disappearing: SpriteAnimation
    var isDisappearing = false
    var finalCentered = false
    var virtualHeight = 0.0
    var virtualWidth = 0.0

    fun hasWon(): Boolean {
        return score > 30_000
    }

    fun hasFailed(): Boolean {
        return y >= virtualHeight
    }

    fun addScore(gainedScore: Double) {
        score += gainedScore
    }

    fun stand() {
        sprite.playAnimationLooped(idle)
    }

    fun update(newX: Double, newY: Double) {
        velocity += gravity
        if(isFalling()) {
            sprite.playAnimationLooped(fall)
        } else {
            sprite.playAnimationLooped(jump)
        }
        when {
            newX < 0 -> {
                move(virtualWidth, newY)
            }
            newX > virtualWidth -> {
                move(0.0, newY)
            }
            else -> {
                move(newX, newY)
            }
        }
    }

    fun move(newX: Double, newY: Double) {
        xy(newX, newY)
    }

    fun roll() {
        sprite.playAnimationLooped(roll)
    }

    fun rollToCenter() {
        roll()
        var newJumperX = x
        var newJumperY = y

        val scaledWidth = width * scaleX
        val scaledHeight = height * scaleY

        if((x.toInt() + scaledWidth.toInt() / 2) < virtualWidth.toInt() / 2) {
            newJumperX++
        } else if ((x.toInt() + scaledWidth.toInt() / 2) > virtualWidth.toInt() / 2) {
            newJumperX--
        }

        if((y.toInt() + scaledHeight.toInt() / 2) < virtualHeight.toInt() / 2) {
            newJumperY++
        } else if ((y.toInt() + scaledHeight.toInt() / 2) > virtualHeight.toInt() / 2) {
            newJumperY--
        }

        move(newJumperX, newJumperY)
    }

    fun centered(): Boolean {
        if(finalCentered) return true
        val scaledWidth = width * scaleX
        val scaledHeight = height * scaleY
        finalCentered = (x.toInt() + scaledWidth.toInt() / 2) == virtualWidth.toInt() / 2 &&
                (y.toInt() + scaledHeight.toInt() / 2) == virtualHeight.toInt() / 2
        return finalCentered
    }

    fun disappear() {
        if (isDisappearing) return

        sprite.playAnimationForDuration(TimeSpan(1_000.0), idle)
        sprite.onAnimationCompleted {
            sprite.onAnimationCompleted.clear()
            sprite.playAnimation(disappearing)
            sprite.onAnimationCompleted {
                sprite.onAnimationCompleted.clear()
                sprite.removeFromParent()
            }

            val scaledWidth = width * scaleX
            val scaledHeight = height * scaleY
            move(virtualWidth / 2 - scaledWidth / 2, virtualHeight /2 - scaledHeight / 2)
        }
        isDisappearing = true
    }

    fun isFalling(): Boolean {
        return velocity > 0
    }

    private suspend fun init() {
        virtualHeight = views().virtualHeightDouble
        virtualWidth = views().virtualWidthDouble

        val jumperId = Random.nextInt(0, 4)

        jump = SpriteAnimation(
            resourcesVfs["Main Characters/$jumperId/Jump (32x32).png"].readBitmap(),
            spriteHeight = 32,
            spriteWidth = 32
        )

        fall = SpriteAnimation(
            resourcesVfs["Main Characters/$jumperId/Fall (32x32).png"].readBitmap(),
            spriteHeight = 32,
            spriteWidth = 32
        )

        roll = SpriteAnimation(
            resourcesVfs["Main Characters/$jumperId/Double Jump (32x32).png"].readBitmap(),
            spriteHeight = 32,
            spriteWidth = 32,
            columns = 6
        )

        idle = SpriteAnimation(
            resourcesVfs["Main Characters/$jumperId/Idle (32x32).png"].readBitmap(),
            spriteHeight = 32,
            spriteWidth = 32,
            columns = 11
        )

        disappearing = SpriteAnimation(
            resourcesVfs["Main Characters/Disappearing (96x96).png"].readBitmap(),
            spriteHeight = 96,
            spriteWidth = 96,
            columns = 7
        )

        sprite = sprite(fall).scale(virtualHeight * 0.07 / height)
        sprite.playAnimationLooped()

        val scaledWidth = width * scaleX
        val scaledHeight = height * scaleY

        x = views().virtualWidthDouble / 2 - scaledWidth / 2
        y = virtualHeight * 0.95 - scaledHeight

        gravity = virtualHeight * 0.5 / 1480
    }

    fun resetJumper() {
        score = 0.0
        velocity = 0.0
        isDisappearing = false
        finalCentered = false
        sprite.playAnimationLooped(fall)

        addChild(sprite)

        val scaledWidth = width * scaleX
        val scaledHeight = height * scaleY

        x = virtualWidth / 2 - scaledWidth / 2
        y = virtualHeight * 0.95 - scaledHeight
    }

    companion object {
        suspend fun create(): Jumper {
            return Jumper().also {
                it.init()
            }
        }
    }
}