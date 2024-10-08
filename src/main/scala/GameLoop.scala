import scalafx.animation.AnimationTimer
import InputHandler._
import Bullet._
import Player.{healthBar, player}
import scalafx.Includes._

object GameLoop {
  var currentBoss: Boss = new Darkman() // Default boss

  def setCurrentBoss(boss: Boss): Unit = {
    currentBoss = boss
  }

  val gameLoop: AnimationTimer = AnimationTimer { _ =>
    if (!Player.isGameOver && currentBoss.bossCurrentHealth != 0) {
      handlePlayerMovement()
      updateBullets()
      currentBoss.updateBossBullet()
      currentBoss.specialAttack()

      // Update the scene content
      GigaDude.stage.scene().content = List(
        SceneManager.backgroundView,
        player,
        healthBar,
        currentBoss.boss,
        currentBoss.bossBullet,
        currentBoss.bossHealthBar
      ) ++ bullets.map(_.shape) ++ currentBoss.getAdditionalContent()

    } else if (currentBoss.bossCurrentHealth == 0) {
      gameLoop.stop()
      GigaDude.showWinScreen()
    } else if (Player.currentHealth == 0){
      gameLoop.stop()
      GigaDude.showGameOverScreen()
    }
  }
}
