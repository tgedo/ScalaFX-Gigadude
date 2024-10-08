import GameConstants._
import Player.player
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.scene.image.Image
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.util.Duration
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

class DeathBoss extends Boss(gameWidth / 2 , gameHeight / 2) {
  override protected val bossImage = new Image("death.png")
  override protected val bossShootingImage = new Image("death_shoot.png")
  override protected val bossMaxHealth = 500
  override val bossBullet: Circle = new Circle{
    radius = 10
    fill = Color.Purple
  }

  private var bulletMovementTimeline: Option[Timeline] = None

  // Timeline for movement, runs continuously
  private val movementTimer = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      KeyFrame(Duration(100), onFinished = (_: ActionEvent) => moveTowardsPlayer())
    )
  }
  movementTimer.play()

  // Timeline for shooting, fires every 3 seconds
  private val shootingTimer = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      KeyFrame(Duration(1000), onFinished = (_: ActionEvent) => shoot())
    )
  }
  shootingTimer.play()

  override def shoot(): Unit = {
    if (!isBulletActive) {
      boss.image = bossShootingImage

      // Calculate direction towards the player at the time of shooting
      val playerX = player.x.value
      val playerY = player.y.value
      val directionX = playerX - (boss.x.value + boss.fitWidth.value / 2.0)
      val directionY = playerY - (boss.y.value + boss.fitHeight.value / 2.0)

      // Normalize the direction vector
      val length = Math.sqrt(directionX * directionX + directionY * directionY)
      val normalizedDirectionX = directionX / length
      val normalizedDirectionY = directionY / length

      // Set bullet position to boss center
      bossBullet.centerX = boss.x.value + boss.fitWidth.value / 2.0
      bossBullet.centerY = boss.y.value + boss.fitHeight.value / 2.0
      isBulletActive = true

      // Define bullet speed
      val bulletSpeed = 5.0

      // Stop any previous bullet movement timeline
      bulletMovementTimeline.foreach(_.stop())

      // Move the bullet in a fixed direction
      val newBulletMovementTimeline = new Timeline {
        cycleCount = Timeline.Indefinite
        keyFrames = Seq(
          KeyFrame(Duration(16), onFinished = (_: ActionEvent) => {
            bossBullet.centerX.value += normalizedDirectionX * bulletSpeed
            bossBullet.centerY.value += normalizedDirectionY * bulletSpeed
          })
        )
      }
      newBulletMovementTimeline.play()

      bulletMovementTimeline = Some(newBulletMovementTimeline)

      // Revert boss sprite after shooting
      val revertSpriteTimeline = new Timeline {
        cycleCount = 1
        keyFrames = Seq(
          KeyFrame(Duration(500), onFinished = (_: ActionEvent) => {
            boss.image = bossImage
          })
        )
      }
      revertSpriteTimeline.play()
    }
  }
  private def moveTowardsPlayer(): Unit = {
    // Get the player's current position
    val playerX = player.x.value
    val playerY = player.y.value

    // Calculate the direction vector from the boss to the player
    val directionX = playerX - boss.x.value
    val directionY = playerY - boss.y.value

    // Normalize the direction vector to get the unit vector
    val length = Math.sqrt(directionX * directionX + directionY * directionY)
    val normalizedDirectionX = directionX / length
    val normalizedDirectionY = directionY / length

    // Speed at which the boss moves towards the player
    val speed = 2.0

    // Update the boss's position
    boss.x.value += normalizedDirectionX * speed
    boss.y.value += normalizedDirectionY * speed

    // Flip the boss to face the player
    if (playerX < boss.x.value) {
      boss.scaleX = 1 // Face left (default sprite facing left)
    } else {
      boss.scaleX = -1 // Face right
    }

  }

  override def updateBossBullet(): Unit = {
    super.updateBossBullet()
  }

  override def teleport(): Unit = {
    moveTowardsPlayer()
  }

  override def getAdditionalContent(): List[Node] = List()

  override def specialAttack(): Unit = {
    boss.image = bossImage // If this isn't here, it becomes invisible
  }

}
