import GameConstants._
import Player.{player, takeDamage}
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.scene.image.{Image, ImageView}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.Node
import scalafx.util.Duration

class Darkman extends Boss(gameWidth - playerWidth - 100, groundLevel - 30) {
  override protected val bossImage = new Image("boss.png")
  override protected val bossShootingImage = new Image("boss_shoot.png")
  override protected val bossMaxHealth = 350
  private val pillarImage = new Image("pillar.png")
  private val pillarWidth = 20.0
  private val pillarHeight = 75.0
  var frontPillar: ImageView = createPillar()
  var backPillar: ImageView = createPillar()
  private var arePillarsActive = false
  private var isSummoningPillars = false
  private var pillarMovementTimeline: Option[Timeline] = None

  private val actionTimer = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      KeyFrame(Duration(1000), onFinished = (_: ActionEvent) => if (!isTeleporting || !isSummoningPillars) shoot()),
      KeyFrame(Duration(6000), onFinished = (_: ActionEvent) => if (!isSummoningPillars || !isShooting) teleport()),
      KeyFrame(Duration(3000), onFinished = (_: ActionEvent) => if (!isTeleporting || !isShooting) summonPillars())
    )
  }
  actionTimer.play()

  override def shoot(): Unit = {
    if (!isBulletActive) {
      boss.image = bossShootingImage
      boss.fitWidth = 90
      bossBullet.centerX = boss.x.value + boss.fitWidth.value / 2
      bossBullet.centerY = boss.y.value + boss.fitHeight.value / 2
      isBulletActive = true

      val revertSpriteTimeline = new Timeline {
        cycleCount = 1
        keyFrames = Seq(
          KeyFrame(Duration(500), onFinished = (_: ActionEvent) => {
            boss.image = bossImage
            boss.fitWidth = 80
          })
        )
      }
      revertSpriteTimeline.play()
    }
  }

  override def teleport(): Unit = {
    val minX = 45
    val maxX = 710 - boss.fitWidth.value.toInt
    var randomX = scala.util.Random.nextInt(maxX - minX + 1) + minX

    while (randomX >= player.x.value - 100 && randomX <= player.x.value + playerWidth + 100) {
      randomX = scala.util.Random.nextInt(maxX - minX + 1) + minX
    }

    boss.x.value = randomX

    if (boss.x.value > player.x.value) {
      boss.scaleX = 1
      bulletDirection = -1.0
    } else {
      boss.scaleX = -1
      bulletDirection = 1.0
    }
  }

  override def updateBossBullet(): Unit = {
    super.updateBossBullet()
    // Check if the player intersects with any pillars
    if (arePillarsActive) {
      if (frontPillar.intersects(player.boundsInParent()) || backPillar.intersects(player.boundsInParent())) {
        takeDamage(50)
        removePillar(frontPillar)
        removePillar(backPillar)
      }
    }
  }

  private def summonPillars(): Unit = {
    if (!isSummoningPillars) {
      isSummoningPillars = true
      if (!arePillarsActive) {
        // Position the pillars in front of the boss
        positionPillar(frontPillar, boss.x.value - pillarWidth - 10) // Left pillar
        positionPillar(backPillar, boss.x.value + boss.fitWidth.value + 10) // Right pillar

        // Show pillars
        setPillarsVisibility(true)
        arePillarsActive = true

        // Set a timer to make the pillars shoot out after 1 second
        val shootPillarsTimer = new Timeline {
          cycleCount = 1
          keyFrames = Seq(
            KeyFrame(Duration(1500), onFinished = (_: ActionEvent) => shootPillars())
          )
        }
        shootPillarsTimer.play()
      }
      isSummoningPillars = false // Reset the summoning flag after summoning
    }
  }

  private def shootPillars(): Unit = {
    // Stop the previous timeline if it's already running
    pillarMovementTimeline.foreach(_.stop())

    // Create a new timeline for pillar movement
    val shootTimeline = new Timeline {
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(
        KeyFrame(Duration(40), onFinished = (_: ActionEvent) => {
          // Move the front pillar to the left
          frontPillar.x.value -= bulletSpeed
          if (frontPillar.x.value < -pillarWidth) {
            removePillar(frontPillar)
          }

          // Move the back pillar to the right
          backPillar.x.value += bulletSpeed
          if (backPillar.x.value > gameWidth) {
            removePillar(backPillar)
          }
        })
      )
    }

    // Start the new timeline and store it
    shootTimeline.play()
    pillarMovementTimeline = Some(shootTimeline)
  }
  // Method to position a pillar
  private def positionPillar(pillar: ImageView, xPosition: Double): Unit = {
    pillar.x = xPosition
    pillar.y = groundLevel - 30
  }

  private def createPillar(): ImageView = {
    new ImageView(pillarImage) {
      fitWidth = pillarWidth
      fitHeight = pillarHeight
      visible = false
    }
  }

  private def removePillar(pillar: ImageView): Unit = {
    pillar.visible = false
    if (pillar == frontPillar || pillar == backPillar) {
      if (!frontPillar.visible.value && !backPillar.visible.value) {
        arePillarsActive = false
        pillarMovementTimeline.foreach(_.stop())
        pillarMovementTimeline = None
      }
    }
  }

  // Method to set visibility for both pillars
  private def setPillarsVisibility(visible: Boolean): Unit = {
    frontPillar.visible = visible
    backPillar.visible = visible
  }

  override def specialAttack(): Unit = {
    summonPillars()
  }

  override def getAdditionalContent(): List[Node] = List(frontPillar, backPillar)
}