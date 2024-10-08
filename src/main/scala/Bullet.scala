import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scala.collection.mutable.ArrayBuffer
import GameConstants._
import GameLoop.currentBoss
import Player.player
import scalafx.scene.media.AudioClip

// Case class to represent a bullet with its direction
case class Bullet(shape: Circle, direction: Double)

object Bullet {
  // Bullet management
  val bullets = new ArrayBuffer[Bullet]()
  private var lastShotTime = 0L
  private val shootSound = new AudioClip(getClass.getResource("/shoot_sound.wav").toString)

  def shoot(): Unit = {
    val currentTime = System.currentTimeMillis()
    if (bullets.size < maxBullets && currentTime - lastShotTime >= shootDelay) {
      val bulletDirection = player.scaleX.value // Store the current facing direction of the player
      val bulletShape = new Circle {
        centerX = player.x.value + playerWidth / 2
        centerY = player.y.value + playerHeight / 2
        radius = 5
        fill = Color.Yellow
      }
      val bullet = Bullet(bulletShape, bulletDirection)
      bullets += bullet
      lastShotTime = currentTime
      shootSound.play()
    }
  }

  def updateBullets(): Unit = {
    // Move bullets
    bullets.foreach { bullet =>
      bullet.shape.centerX = bullet.shape.centerX.value + bulletSpeed * bullet.direction
    }

    // Check for collisions with the boss and remove off-screen bullets
    bullets --= bullets.filter { bullet =>
      val isOffScreen = bullet.shape.centerX.value < 0 || bullet.shape.centerX.value > gameWidth
      val hasHitBoss = bullet.shape.intersects(currentBoss.boss.boundsInParent())

      // Check for collisions with additional boss content (like pillars)
      val hasHitAdditionalContent = currentBoss.getAdditionalContent().exists { node =>
        node.visible.value && bullet.shape.intersects(node.boundsInParent())
      }

      if (hasHitBoss) {
        currentBoss.bossTakeDamage(10) // Deal damage to the boss
      }

      // Remove bullet if it hits the boss, additional content, or goes off-screen
      isOffScreen || hasHitBoss || hasHitAdditionalContent
    }
  }
}