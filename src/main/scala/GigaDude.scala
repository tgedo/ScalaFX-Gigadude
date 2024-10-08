import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import SceneManager._
import GameLoop._
import InputHandler._
import Bullet.bullets

object GigaDude extends JFXApp {
  private var startTime: Long = 0L // Track the start time of the game

  // Method to start the game from the main menu
  def startGame(): Unit = {
    startTime = System.currentTimeMillis()
    restartGame()
    gameLoop.start()
  }

  // Method to restart the game
  private def restartGame(): Unit = {
    resetPlayer()
    bullets.clear()
    GameLoop.currentBoss.reset()
    stage.scene = gameScene
  }

  // Set initial scene to the Main Menu
  stage = new PrimaryStage {
    title = "GigaDude"
    resizable = false
    scene = createMainMenuScene()
  }

  // Method to show the Win Screen
  def showWinScreen(): Unit = {
    val clearTime = System.currentTimeMillis() - startTime
    println(s"Clear time: $clearTime ms")
    stage.scene = createWinScreen(clearTime)
  }

  // Method to show the Game Over screen
  def showGameOverScreen(): Unit = {
    stage.scene = createGameOverScene()
  }

  // Set initial scene to the Main Menu
  stage.scene = createMainMenuScene()
}
