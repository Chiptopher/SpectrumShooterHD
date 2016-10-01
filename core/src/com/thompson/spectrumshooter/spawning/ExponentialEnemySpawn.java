package com.thompson.spectrumshooter.spawning;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.thompson.spectrumshooter.gameobject.GameObject;
import com.thompson.spectrumshooter.gameobject.GameObjectFactory;

/**
 * ExponentialEnemySpawn.java
 * 
 * 
 * 
 * @author Zachary Thompson
 */
public class ExponentialEnemySpawn implements EnemySpawning 
{
	private float spawnTime;
	private GameObjectFactory gameObjectFactory;
	private float previousTime;
	public ExponentialEnemySpawn()
	{
		this(15);
	}
	
	public ExponentialEnemySpawn(float spawnTime)
	{
		this.spawnTime = spawnTime;
		this.gameObjectFactory = new GameObjectFactory();
		this.previousTime = spawnTime;
	}
	
	@Override
	public Array<GameObject> update(Array<GameObject> enemies, World world, float deltaTime) 
	{
		previousTime = previousTime + deltaTime;
		
		if (previousTime > spawnTime)
		{
			enemies.add(gameObjectFactory.makeBasicEnemy(world));
			previousTime = previousTime - spawnTime;
		}
		
		for (GameObject enemy : enemies)
		{
			if (!enemy.isAlive)
			{
				world.destroyBody(enemy.getFixture().getBody());
				enemy.dispose();
				// false indicates using .equals; true indicated using ==
				enemies.removeValue(enemy, false);
				enemies.add(gameObjectFactory.makeBasicEnemy(world));
			}
		}
		return enemies;
	}

}
