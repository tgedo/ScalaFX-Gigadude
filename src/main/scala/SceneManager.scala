import scalafx.scene.Scene
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import Player._
import Bullet._
import GameConstants.{groundLevel, jumpStrength}
import GameLoop.currentBoss
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

import scala.reflect.io.File

object SceneManager {
  val gameWidth: Double = 800
  val gameHeight: Double = 600

  // Load images
  private val menuBackground = new Image("menu_background.jpg")
  private val gameOverBackground = new Image("victory_background.jpg")
  private val victoryBackground = new Image("victory_background.jpg")
  private val darkmanThumbnail = new Image("boss.png")
  private val deathThumbnail = new Image("death.png")

  // Helper method to load media from resources
  private def getMediaResource(path: String): Media = {
    val resource = getClass.getResource(path)
    if (resource != null) {
      new Media(resource.toString)
    } else {
      throw new RuntimeException(s"Media resource not found: $path")
    }
  }

  // Load media from resources folder
  private val menuTheme = new MediaPlayer(getMediaResource("/menu_theme.mp3"))
  private val bossSelectTheme = new MediaPlayer(getMediaResource("/boss_select.mp3"))
  private val bossTheme = new MediaPlayer(getMediaResource("/boss_theme.mp3"))
  private val deathTheme = new MediaPlayer(getMediaResource("/death_theme.mp3"))
  private val gameOverSound = new MediaPlayer(getMediaResource("/gameover.mp3"))
  private val victorySound = new MediaPlayer(getMediaResource("/victory.mp3"))
  private val backgroundImage = new Image("background.png")

  val backgroundView: ImageView = new ImageView(backgroundImage) {
    fitWidth = gameWidth
    fitHeight = gameHeight
    preserveRatio = false
  }

  val ground: Rectangle = new Rectangle {
    width = gameWidth
    height = 100
    x = 0
    y = groundLevel + 50
    fill = Color.Green
  }

  // Main game scene
  val gameScene: Scene = new Scene(gameWidth, gameHeight) {
    content = List(
      backgroundView, // Background image
      ground,
      player,
      healthBar,
      currentBoss.boss,
      currentBoss.bossBullet,
      currentBoss.bossHealthBar,
    ) ++ bullets.map(_.shape)

    // Add key event handlers to track pressed keys
    onKeyPressed = (event: KeyEvent) => {
      InputHandler.pressedKeys.add(event.code)
      if (event.code == KeyCode.Z && onGround) {
        velocityY = jumpStrength
        onGround = false
      }
    }

    onKeyReleased = (event: KeyEvent) => {
      InputHandler.pressedKeys.remove(event.code)
      // Implement jump cutting: reduce upward velocity if jump key is released early
      if (event.code == KeyCode.Z && velocityY < 0) {
        velocityY /= 2
      }
    }
  }

  // Main Menu scene
  def createMainMenuScene(): Scene = {
    bossSelectTheme.stop()
    menuTheme.setCycleCount(MediaPlayer.Indefinite)
    menuTheme.play()

    val backgroundView = new ImageView(menuBackground){
      fitWidth = gameWidth
      fitHeight = gameHeight
    }

    val titleLabel = new Label("GIGADUDE") {
      font = new Font("Impact", 64)
      textFill = Color.Blue
    }
    val Instructions = new Label("Movement: Arrow Keys, Jump: Z, Shoot: X"){
      font = new Font("Impact", 24)
      textFill = Color.White
    }
    val startButton = new Button("Start Game") {
      onAction = _ => GigaDude.stage.scene = createBossSelectScene()
    }
    val exitButton = new Button("Exit") {
      onAction = _ => sys.exit()
    }
    val vbox = new VBox(20, titleLabel, Instructions,startButton, exitButton) {
      alignment = Pos.Center
      prefWidth = gameWidth
      prefHeight = gameHeight
    }

    new Scene(gameWidth, gameHeight) {
      content = List(backgroundView,vbox)
      fill = Color.Black
    }
  }

  def createBossSelectScene(): Scene = {
    // Stop menu theme, start boss select theme
    menuTheme.stop()
    bossSelectTheme.setCycleCount(MediaPlayer.Indefinite)
    bossSelectTheme.play()

    val backgroundView = new ImageView(menuBackground){
      fitWidth = gameWidth
      fitHeight = gameHeight
    }

    val titleLabel = new Label("SELECT BOSS") {
      font = new Font("Impact", 36)
      textFill = Color.Cyan
    }

    val darkmanButton = new Button("Darkman") {
      graphic = new ImageView(darkmanThumbnail) {
        fitWidth = 100
        fitHeight = 100
      }
      onAction = _ => startGameWithBoss(new Darkman())
    }

    val deathButton = new Button("Death") {
      graphic = new ImageView(deathThumbnail) {
        fitWidth = 100
        fitHeight = 100
      }
      onAction = _ => startGameWithBoss(new DeathBoss())
    }



    // Add more boss buttons as needed
    val backButton = new Button("Return to title") {
      onAction = _ => showMainMenu()
    }

    val vbox = new VBox(20, titleLabel, darkmanButton,deathButton, backButton) {
      alignment = Pos.Center
      prefWidth = gameWidth
      prefHeight = gameHeight
    }

    new Scene(gameWidth, gameHeight) {
      content = List(backgroundView, vbox)
      fill = Color.Black
    }
  }

  private def startGameWithBoss(boss: Boss): Unit = {
    // Stop any previous themes
    bossSelectTheme.stop()
    bossTheme.stop()
    deathTheme.stop()

    // If the boss is DeathBoss, play the death theme
    if (boss.isInstanceOf[DeathBoss]) {
      deathTheme.setCycleCount(MediaPlayer.Indefinite)
      deathTheme.play()
    } else {
      bossTheme.setCycleCount(MediaPlayer.Indefinite)
      bossTheme.play()
    }
    println(s"Starting game with boss: ${boss.getClass.getSimpleName}")
    GameLoop.setCurrentBoss(boss)
    GigaDude.startGame()
  }

  // Game Over scene
  def createGameOverScene(): Scene = {
    // Stop game music
    bossTheme.stop()
    deathTheme.stop()

    // Play game over sound (no loop)
    gameOverSound.play()
    val backgroundView = new ImageView(gameOverBackground) {
      fitWidth = gameWidth
      fitHeight = gameHeight
    }

    val gameOverLabel = new Label("Game Over") {
      font = new Font("Arial", 48)
      textFill = Color.Red
    }
    val mainMenuButton = new Button("Main Menu") {
      onAction = _ => SceneManager.showMainMenu()
    }
    val vbox = new VBox(20, gameOverLabel, mainMenuButton) {
      alignment = Pos.Center
      prefWidth = gameWidth
      prefHeight = gameHeight
    }

    new Scene(gameWidth, gameHeight) {
      content = List(backgroundView, vbox)
    }
  }

  // Win Screen scene
  def createWinScreen(clearTime: Long): Scene = {
    // Stop game music
    bossTheme.stop()
    deathTheme.stop()

    // Play victory sound (no loop)
    victorySound.play()

    val backgroundView = new ImageView(victoryBackground) {
      fitWidth = gameWidth
      fitHeight = gameHeight
    }

    val winLabel = new Label("You Win!") {
      font = new Font("Arial", 48)
      textFill = Color.LightGreen
    }
    val duration = java.time.Duration.ofMillis(clearTime)
    val seconds = duration.getSeconds
    val timeLabel = new Label(f"Clear Time: ${seconds / 60}%02d:${seconds % 60}%02d.") {
      font = new Font("Arial", 24)
      textFill = Color.White
    }
    val mainMenuButton = new Button("Main Menu") {
      onAction = _ => SceneManager.showMainMenu()
    }
    val vbox = new VBox(20, winLabel, timeLabel, mainMenuButton) {
      alignment = Pos.Center
      prefWidth = gameWidth
      prefHeight = gameHeight
    }

    new Scene(gameWidth, gameHeight) {
      content = List(backgroundView, vbox)
    }
  }

  def showMainMenu(): Unit = {
    InputHandler.clearInputs()
    GigaDude.stage.scene = createMainMenuScene()
  }
}
