import android.app.Activity
import android.content.Intent
import android.util.DisplayMetrics
import com.soywiz.klock.NumberOfTimes
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.infiniteTimes
import com.soywiz.korau.sound.*
import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.internal.KorgeInternal
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.paint.ColorPaint
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.android.androidContext
import com.soywiz.korio.android.withAndroidContext
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.SizeInt
import kotlinx.coroutines.coroutineScope

suspend fun main() = Korge(Korge.Config(module = JumperModule.create()))

class JumperModule private constructor() : Module() {
	override val mainScene = PlayingScene::class
	override var size = SizeInt(1920, 1080)
	override val bgcolor = Colors["#2b2b2b"]

	private suspend fun init() {
		val displayMetrics = DisplayMetrics()
		(androidContext() as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics);
		val height = displayMetrics.heightPixels;
		val width = displayMetrics.widthPixels;
		size = SizeInt(width, height)
	}

	companion object {
		suspend fun create(): JumperModule {
			return JumperModule().also {
				it.init()
			}
		}
	}

	override suspend fun AsyncInjector.configure() {

		mapInstance(Jumper.create())
		mapPrototype { PlayingScene(get()) }
		mapPrototype { EndScene(get()) }
	}
}

class PlayingScene(val jumper: Jumper) : Scene() {
	private lateinit var channel: SoundChannel
	override suspend fun sceneAfterInit() {
		super.sceneAfterInit()
		val music = resourcesVfs["playing_music.mp3"].readMusic()
		channel = music.play(PlaybackTimes.INFINITE)
	}

	@OptIn(KorgeInternal::class)
	override suspend fun Container.sceneInit() {
		val background = Background.create()
		addChild(background)

		val platforms = Platforms.create()
		addChild(platforms)

		addChild(jumper)

		val scoreText = text(jumper.score.toInt().toString()).xy(30,60)
		scoreText.color = ColorPaint(0xFF000000.toInt())
		scoreText.scale(views.virtualHeight * 0.05 / scoreText.height)

		val goalText = text("Goal: 30 000")
		goalText.scale(views.virtualHeight * 0.05 / scoreText.height)
		val goalX = views.virtualWidth - goalText.width * goalText.scaleX - views.virtualWidth * 0.05

		goalText.xy(goalX,60.0)
		goalText.color = ColorPaint(0xFF000000.toInt())

		val jumpHeight = views.virtualHeight  * -20.0 / 1480
		val scaledJumperHeight = jumper.height * jumper.scaleY

		jumper.onCollision({ platforms.children.contains(it) }) {
			val scaledPlatformHeight = it.height * it.scaleY
			if (jumper.isFalling() && jumper.y + scaledJumperHeight <= it.y + scaledPlatformHeight / 2) {
				jumper.velocity = jumpHeight
			}
		}

		val tiltManager = TiltManager(androidContext())
		val tiltScale = 0.007 * views.virtualWidth



		addUpdater {
			if(jumper.hasFailed()) {
				channel.stop()
				launchImmediately { sceneContainer.changeTo<EndScene>() }
			} else {
				val newJumperX = jumper.x - tiltManager.tilt * tiltScale
				val newJumperY: Double
				val remaining = (views.virtualHeightDouble / 2) - (jumper.y + jumper.velocity)

				newJumperY = if(remaining > 0) {
					jumper.addScore(remaining)
					platforms.scrollDown(remaining)

					views.virtualHeightDouble / 2
				} else {
					jumper.y + jumper.velocity
				}

				jumper.update(newJumperX, newJumperY)

				scoreText.setText(jumper.score.toInt().toString())

				platforms.update(virtualHeight = views.virtualHeightDouble, virtualWidth = views.virtualWidthDouble)

			}
		}
	}
}

class EndScene(val jumper: Jumper): Scene() {
	private lateinit var channel: SoundChannel

	override suspend fun sceneAfterInit() {
		super.sceneAfterInit()
		val music = resourcesVfs["end_music.mp3"].readSound()
		channel = music.play(PlaybackTimes.INFINITE)
	}

	override suspend fun Container.sceneInit() {
		val background = Background.create()
		addChild(background)

		val victoryPanel = EndPanel.create()
		addChild(victoryPanel)

		addChild(jumper)
		var scaledWidth = jumper.width * jumper.scaleX
		val scaledHeight = jumper.height * jumper.scaleY
		jumper.move(views.virtualWidthDouble / 2 - scaledWidth / 2,
			views.virtualHeightDouble / 2 - scaledHeight / 2)
		jumper.stand()


		val scoreLabel = text("Score")
		scoreLabel.scale(views.virtualWidth * 0.2 / scoreLabel.width)

		scaledWidth = scoreLabel.width * scoreLabel.scaleX
		scoreLabel.xy(views.virtualWidthDouble / 2 - scaledWidth / 2, views.virtualHeightDouble * 0.15)

		val score = text(jumper.score.toInt().toString())
		score.scale(views.virtualWidth * 0.3 / score.width)

		scaledWidth = score.width * score.scaleX
		score.xy(views.virtualWidthDouble / 2 - scaledWidth / 2, views.virtualHeightDouble * 0.2)

		val stopBitmap = resourcesVfs["stop.png"].readBitmap()
		val stop = image(stopBitmap)
		stop.scale(views.virtualWidth * 0.2 / stop.width)

		scaledWidth = stop.width * stop.scaleX
		stop.xy(views.virtualWidthDouble / 2 - scaledWidth / 2, views.virtualHeightDouble * 0.7)

		stop.onClick {
			channel.stop()
			val activity = androidContext() as Activity
			activity.setResult(if(jumper.hasWon()) 1 else 2)
			activity.finish()
		}

		if(!jumper.hasWon()) {
			jumper.disappear()
		}

		addUpdater {
			victoryPanel.update(views.virtualHeightDouble, views.virtualWidthDouble)
		}
	}
}