import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.ProgressBar
import GameConstants._
import scalafx.scene.input.KeyCode
import scalafx.scene.media.AudioClip
import scala.collection.mutable

object Player {
  // Load images for different states
  private val standImage = new Image("idle.png")
  private val jumpImage = new Image("jump.png")
  private val shootImage = new Image("shoot.png")
  private val jumpShootImage = new Image("j_shoot.png")
  private val walkImages = Array(
    new Image("walk1.png"),
    new Image("walk2.png"),
    new Image("walk3.png")
  )
  private val walkShootImages = Array(
    new Image("s_walk1.png"),
    new Image("s_walk2.png"),
    new Image("s_walk3.png")
  )

  // Load sound effects
  private val jumpSound = new AudioClip(getClass.getResource("/jump_sound.wav").toString)


  // Player setup
  val player: ImageView = new ImageView(standImage) {
    x = 100
    y = groundLevel
    fitWidth = playerWidth
    fitHeight = playerHeight
    preserveRatio = true
  }
  // Check for a Game Over
  var isGameOver = false

  // Player health
  val maxHealth = 100
  var currentHealth: Int = maxHealth

  // Health bar setup using ProgressBar
  val healthBar: ProgressBar = new ProgressBar {
    prefWidth = 150
    prefHeight = 10
    progress = 1.0 // Initially full health
    style = """
    -fx-accent: yellow;
    -fx-control-inner-background: black;
    -fx-background-color: black;
  """
    rotate = -90 // Rotate by -90 degrees to make it vertical
    layoutX = -50
    layoutY = 75
  }

  // Player velocity
  var velocityY = 0.0
  var onGround = true
  private var wasOnGround = true

  // Animation control
  private var walkFrame = 0
  private var lastFrameTime = System.currentTimeMillis()

  def applyGravity(): Unit = {
    if (!onGround) {
      velocityY += gravity
      player.y.value += velocityY

      // Check if player has landed on the ground
      if (player.y.value >= groundLevel) {
        player.y.value = groundLevel
        velocityY = 0
        onGround = true

        // Play landing sound if the player has just landed
        if (!wasOnGround) {
          jumpSound.play()
        }
      }
    }

    // Update the previous state after checking
    wasOnGround = onGround
  }

  def updatePlayerSprite(pressedKeys: mutable.Set[KeyCode]): Unit = {
    val isMoving = pressedKeys.contains(KeyCode.Left) || pressedKeys.contains(KeyCode.Right)
    val isShooting = pressedKeys.contains(KeyCode.X)
    val isJumping = !onGround

    if (isJumping && isShooting) {
      updateSprite(jumpShootImage, 29.0/30.0)
    } else if (isJumping) {
      updateSprite(jumpImage, 26.0/30.0)
    } else if (isMoving && isShooting) {
      updateWalkingAnimation(isShooting = true)
    } else if (isMoving) {
      updateWalkingAnimation(isShooting = false)
    } else if (isShooting) {
      updateSprite(shootImage, 31.0/24.0)
    } else {
      updateSprite(standImage, 21.0/24.0)
    }
  }

  private def updateWalkingAnimation(isShooting: Boolean): Unit = {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastFrameTime >= 100) { // Change frame every 0.1s
      lastFrameTime = currentTime
      walkFrame = (walkFrame + 1) % 3
    }
    val (currentImage, ratio) = if (isShooting) {
      walkFrame match {
        case 0 => (walkShootImages(0), 29.0/22.0)
        case 1 => (walkShootImages(1), 26.0/24.0)
        case 2 => (walkShootImages(2), 30.0/22.0)
      }
    } else {
      (walkImages(walkFrame), 21.0/24.0)
    }
    updateSprite(currentImage, ratio)
  }

  private def updateSprite(image: Image, widthToHeightRatio: Double): Unit = {
    player.image = image
    val newHeight = playerHeight
    val newWidth = newHeight * widthToHeightRatio
    player.fitHeight = newHeight
    player.fitWidth = newWidth
  }

  def updateHealthBar(): Unit = {
    healthBar.progress = currentHealth.toDouble / maxHealth
    println("current Health:" + currentHealth)
  }

  def takeDamage(amount: Int): Unit = {
    currentHealth -= amount
    if (currentHealth < 0) currentHealth = 0
    updateHealthBar()
    if (currentHealth == 0){
      isGameOver = true
    }
  }
}
