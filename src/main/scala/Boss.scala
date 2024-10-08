import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.ProgressBar
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import GameConstants._
import Player._
import scalafx.Includes._
import scalafx.scene.Node

abstract class Boss(initialX: Double, initialY: Double) {
  // Common boss properties
  protected val bossImage: Image
  protected val bossShootingImage: Image
  val boss: ImageView = new ImageView(bossImage) {
    x = initialX
    y = initialY
    fitWidth = 80
    fitHeight = playerHeight + 30
  }

  // Health management
  protected val bossMaxHealth: Int
  var bossCurrentHealth: Int = bossMaxHealth
  val bossHealthBar: ProgressBar = createHealthBar()

  // Bullet management
  val bossBullet: Circle = new Circle {
    radius = 10
    fill = Color.Red
  }
  protected var isBulletActive = false
  var bulletDirection: Double = -1.0

  // Action flags
  protected var isShooting = false
  protected var isTeleporting = false

  // Abstract methods for boss actions
  def shoot(): Unit
  def teleport(): Unit
  def specialAttack(): Unit // New method for boss-specific special attack

  // Common methods
  def updateBossBullet(): Unit = {
    if (isBulletActive) {
      bossBullet.centerX = bossBullet.centerX.value + bulletSpeed * bulletDirection

      if (bossBullet.intersects(player.boundsInParent())) {
        Player.takeDamage(20)
        isBulletActive = false
        resetBullet()
      }

      if (bossBullet.centerX.value < 0 || bossBullet.centerX.value > gameWidth) {
        isBulletActive = false
        resetBullet()
      }
    }

    if (boss.boundsInParent().intersects(player.boundsInParent())) {
      Player.takeDamage(30)
    }
  }

  def bossTakeDamage(amount: Int): Unit = {
    bossCurrentHealth -= amount
    if (bossCurrentHealth < 0) bossCurrentHealth = 0
    updateBossHealthBar()

    if (bossCurrentHealth == 0) {
      GigaDude.showWinScreen()
    }
  }

  def reset(): Unit = {
    bossCurrentHealth = bossMaxHealth
    boss.x = initialX
    boss.y = initialY
    updateBossHealthBar()
  }

  protected def resetBullet(): Unit = {
    bossBullet.centerX = -100
  }

  private def createHealthBar(): ProgressBar = new ProgressBar {
    prefWidth = 150
    prefHeight = 10
    progress = 1.0
    rotate = -90
    style = """
      -fx-accent: red;
      -fx-control-inner-background: black;
      -fx-background-color: black;
    """
    layoutX = gameWidth - 100
    layoutY = 75
  }

  protected def updateBossHealthBar(): Unit = {
    bossHealthBar.progress = bossCurrentHealth.toDouble / bossMaxHealth
  }

  def getAdditionalContent(): List[Node] = List() // Override this in subclasses if needed
}