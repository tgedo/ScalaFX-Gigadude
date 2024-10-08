import Bullet.shoot
import scalafx.scene.input.{KeyCode, KeyEvent}
import scala.collection.mutable
import GameConstants._
import Player.{applyGravity, player, updatePlayerSprite}

object InputHandler {
  val pressedKeys: mutable.Set[KeyCode] = mutable.Set[KeyCode]()

  def handlePlayerMovement(): Unit = {
    if (pressedKeys.contains(KeyCode.Left)) {
      player.x.value -= horizontalSpeed
      if (player.x.value < 45) player.x.value = 45
      player.scaleX = -1 // Flip sprite to face left
    }
    if (pressedKeys.contains(KeyCode.Right)) {
      player.x.value += horizontalSpeed
      if (player.x.value + playerWidth > 755) player.x.value = 755 - playerWidth
      player.scaleX = 1 // Flip sprite to face right
    }
    if (pressedKeys.contains(KeyCode.X)) {
      shoot()
    }
    applyGravity()
    updatePlayerSprite(pressedKeys)
  }

  def resetPlayer(): Unit = {
    Player.currentHealth = Player.maxHealth
    Player.isGameOver = false
    Player.updateHealthBar()
    Player.player.x = 100
    Player.player.y = groundLevel
    Player.velocityY = 0.0
    Player.onGround = true
  }
  def clearInputs(): Unit = {
    pressedKeys.clear()
  }
}
