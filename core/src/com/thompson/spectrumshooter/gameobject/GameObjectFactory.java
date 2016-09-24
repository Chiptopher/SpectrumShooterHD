package com.thompson.spectrumshooter.gameobject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.thompson.spectrumshooter.color.ColorWheel;
import com.thompson.spectrumshooter.util.Constants;


/**
 * A factory that allows for the creation of enemies.
 *
 * @author cb9619
 */
public class GameObjectFactory
{

	private static final int PIXMAP_RADIUS = 150;
	private ColorWheel colorWheel;

	private static final short CATEGORY_ENEMY = 0x0002;
	private static final short CATEGORY_HERO_PROJECTILE = 0x0001;
	
	private static final int OUTWARDS = 1;
	private static final int INWARDS = -1;
	private static final int STATIONARY = 1;

	public GameObjectFactory()
	{
		this.colorWheel = new ColorWheel();
	}

	/**
	 * Make a new Enemy that moves linearly from it's spawn location towards
	 * the center of the screen.
	 * @param world		the world where the Enemy exists
	 * @return			the new basic Enemy
	 */
	public Enemy makeBasicEnemy(World world)
	{
		int colorCode = colorWheel.random();
		Texture texture = new Texture(createPixmap(colorCode));

		float spriteSize =  MathUtils.random(0.25f, 0.75f);

		Fixture fixture = createDynamicFixture(world,
											   generateRandomSpawnLocation(Constants.ENEMY_RADIUS),
											   spriteSize,
											   INWARDS);
		Filter filter = new Filter();
		filter.categoryBits = CATEGORY_ENEMY;
		filter.maskBits = ~CATEGORY_ENEMY;
		fixture.setFilterData(filter);

		Enemy enemy = new Enemy(colorCode, 3, fixture, texture,spriteSize);

		enemy.setOrigin(enemy.getWidth() / 2.0f, enemy.getHeight() / 2.0f);

		return enemy;
	}

	public Hero makeHero(World world)
	{
		int colorCode = colorWheel.random();

		Texture texture = new Texture(createPixmap(colorCode));
		float spriteSize = 0.75f;
		Fixture fixture = createStaticFixture(world, new Vector2(0, 0), spriteSize, STATIONARY);
		Filter filter = new Filter();
		filter.categoryBits = CATEGORY_HERO_PROJECTILE;
		filter.maskBits = ~CATEGORY_HERO_PROJECTILE;
		fixture.setFilterData(filter);

		Hero hero = new Hero(colorCode, 15, fixture, texture, 0.0f);

		hero.setPosition(fixture.getBody().getPosition().x - 3,
 		  		 		 fixture.getBody().getPosition().y - 3);

		hero.setOrigin(hero.getWidth() / 2.0f, hero.getHeight() / 2.0f);

		hero.setSize(spriteSize, spriteSize);

		// hero does this when enemy/projectile don't becuase the enemies call their update
		// method which does this.
		hero.setPosition(fixture.getBody().getPosition().x - spriteSize/2.0f,
 		  		 		 fixture.getBody().getPosition().y - spriteSize/2.0f);

		return hero;
	}

	public Projectile makeProjectile(World world, float mouseX, float mouseY)
	{
		int colorCode = colorWheel.random();
		Texture texture = new Texture(createPixmap(colorCode));
		float spriteSize = 0.2f;
		Fixture fixture = createDynamicFixture(world, generateProjectilePosition( Constants.PROJECTILE_REDIUS,
											   mouseX, mouseY), spriteSize, OUTWARDS);
		Filter filter = new Filter();
		filter.categoryBits = CATEGORY_HERO_PROJECTILE;
		filter.maskBits = ~CATEGORY_HERO_PROJECTILE;
		fixture.setFilterData(filter);

		Projectile projectile = new Projectile(colorCode, 1, fixture, texture, spriteSize);

		projectile.setOrigin(projectile.getWidth() / 2.0f, projectile.getHeight() / 2.0f);

		return projectile;
	}


	/**
	 * Create the pixmaps used for our GameObjects.
	 * @param colorCode the color code from the color wheel of this Pixmap
	 * @return a new pixmap
	 */
	private Pixmap createPixmap(int colorCode)
	{
		Pixmap pixmap = new Pixmap(300, 300, Format.RGBA8888);
		pixmap.setColor(colorWheel.getRedValue(colorCode),
						colorWheel.getGreenValue(colorCode),
						colorWheel.getBlueValue(colorCode),
						1);
		pixmap.fillCircle(150, 150, PIXMAP_RADIUS);
		return pixmap;
	}


	/**
	 * Create a Dynamic Fixture at the given position and size.
	 * @param world				the world the fixture will exist in
	 * @param spawnPosition		the location the fixture will spawn at
	 * @param spriteSize		the size of the sprite corresponding to the fixture
	 * @return					a new dynaic Fixture
	 */
	private Fixture createDynamicFixture(World world, Vector2 spawnPosition, float spriteSize, int direction)
	{
		BodyDef bodyDef = new BodyDef();
		// Dynamic implies that things can move.
		bodyDef.type = BodyType.DynamicBody;

		return createFixture(world, spawnPosition, spriteSize, bodyDef, direction);
	}

	/**
	 * Creat a new static Fixture at the given spawn position and sprite size.
	 * @param world				the world the fixture will exist in
	 * @param spawnPosition		the location the fixture will spawn at
	 * @param spriteSize		the size of the sprite corresponding to the fixture
	 * @return					a new static Fixture
	 */
	private Fixture createStaticFixture(World world, Vector2 spawnPosition, float spriteSize, int direction)
	{
		BodyDef bodyDef = new BodyDef();
		// Static implies that things cannot move
		bodyDef.type = BodyType.StaticBody;

		return createFixture(world, spawnPosition, spriteSize, bodyDef, direction);
	}

	/**
	 * Create a new Fixture for a game object based on the given body definition.
	 * @param world		game world where the Enemy exists
	 * @return			new Fixture
	 */
	private Fixture createFixture(World world, Vector2 spawnPosition, float spriteSize, BodyDef bodyDef, int direction)
	{
		bodyDef.position.set(spawnPosition);

		Body body = world.createBody(bodyDef);
		body.setLinearVelocity((direction *spawnPosition.x) * 0.1f, (direction * spawnPosition.y * 0.1f));

		CircleShape circle = new CircleShape();
		circle.setRadius(spriteSize * Constants.BOX2D_CONVERSION);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;

		Fixture fixture = body.createFixture(fixtureDef);
		// remove the CircleShape resources for some reason
		circle.dispose();
		return fixture;
	}

	/**
	 * Generate a random spawn location for an Enemy.
	 * @return	a random spawn location for an Enemy
	 */
	private Vector2 generateRandomSpawnLocation(float spawnRadius)
	{
		float theta = MathUtils.random(0f, 360.0f);

		Vector2 randomLocation = createPosition(spawnRadius, theta);

		return randomLocation;
	}

	/**
	 * Create a position for a projectile.
	 * @param spawnRadius	the distance the projectile should be from the origin
	 * @param mouseX		the x location of the mouse when pressed
	 * @param mouseY		the y location of the mouse when pressed
	 * @return				the Vector2 location of the projectile
	 */
	private Vector2 generateProjectilePosition(float spawnRadius, float mouseX, float mouseY)
	{
		float theta = MathUtils.radiansToDegrees * MathUtils.atan2(mouseY, mouseX);
		if (mouseY < 0)
		{
			theta = 180 + (180 - (theta * -1));
		}
		return createPosition(spawnRadius, theta);
	}

	/**
	 * Create a Vector2 position at the given radius and given degree around the origin.
	 * @param spawnRadius	the distance from the center the position will be at
	 * @param theta			the rotation around the origin the position will be at
	 * @return				a Vector2 for the given position
	 */
	private Vector2 createPosition(float spawnRadius, float theta)
	{
		return new Vector2(spawnRadius * MathUtils.cosDeg(theta), 
						   spawnRadius * MathUtils.sinDeg(theta));
	}

}



















