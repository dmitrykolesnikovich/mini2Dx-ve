/**
 * Copyright (c) 2015 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.uats;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.geom.Polygon;
import org.mini2Dx.core.graphics.Animation;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.graphics.NinePatch;
import org.mini2Dx.core.graphics.RepeatedNinePatch;
import org.mini2Dx.core.graphics.Sprite;
import org.mini2Dx.core.graphics.TextureRegion;
import org.mini2Dx.core.screen.BasicGameScreen;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.core.screen.Transition;
import org.mini2Dx.core.screen.transition.FadeInTransition;
import org.mini2Dx.core.screen.transition.FadeOutTransition;
import org.mini2Dx.uats.util.ScreenIds;
import org.mini2Dx.uats.util.UATSelectionScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * A {@link GameScreen} that allows visual user acceptance testing of
 * {@link Graphics} functionality
 */
public class GraphicsUAT extends BasicGameScreen {
	private final AssetManager assetManager;
	private final FileHandleResolver fileHandleResolver;

	private int playerX, playerY;
	private float scaleX, scaleY;
	private int rotation;

	private Texture texture;
	private TextureRegion textureRegion, atlasTextureRegion;
	private Sprite spriteWithTexture, spriteWithTextureRegion;
	private Animation<Sprite> animation;
	private Polygon polygon;
	private TextureAtlas textureAtlas;
	private NinePatch ninePatch;
	private RepeatedNinePatch repeatedNinePatch;

	public GraphicsUAT(AssetManager assetManager, FileHandleResolver fileHandleResolver) {
		this.assetManager = assetManager;
		this.fileHandleResolver = fileHandleResolver;
	}

	@Override
	public void initialise(GameContainer gc) {
		playerX = 0;
		playerY = 0;
		scaleX = 2f;
		scaleY = 2f;
		rotation = 0;

		texture = new Texture(fileHandleResolver.resolve("tank.png"));
		textureRegion = new TextureRegion(texture);

		spriteWithTexture = new Sprite(texture);
		spriteWithTexture.setPosition(512, 0);

		spriteWithTextureRegion = new Sprite(textureRegion);

		animation = new Animation<Sprite>();
		animation.addFrame(new Sprite(texture), 1000);
		animation.setOriginX(spriteWithTexture.getWidth() / 2f);
		animation.setOriginY(0f);

		polygon = new Polygon(new float[] { 25f, 25f, 50f, 37.5f, 37.5f, 75f, 12.5f, 75f, 0f, 37.5f });
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> screenManager, float delta) {
		if (ninePatch == null) {
			textureAtlas = assetManager.get("default-mdx-theme.atlas", TextureAtlas.class);
			atlasTextureRegion = new TextureRegion(textureAtlas.findRegion("frame"));
			ninePatch = new NinePatch(atlasTextureRegion, 4, 4, 4, 4);
		}
		if (repeatedNinePatch == null) {
			repeatedNinePatch = new RepeatedNinePatch(
					new TextureRegion(new Texture(fileHandleResolver.resolve("repeatedNinePatch.png"))), 4, 4, 4, 4);
		}

		detectKeyPress(screenManager);
		rotation += 180f * delta;

		animation.setRotation(rotation);
	}

	@Override
	public void interpolate(GameContainer gc, float alpha) {
	}

	@Override
	public void render(GameContainer gc, Graphics g) {
		g.setBackgroundColor(Color.WHITE);

		g.setColor(Color.GREEN);
		for (int x = 0; x < 800 / 32; x++) {
			for (int y = 0; y < 600 / 32; y++) {
				g.drawRect(x * 32, y * 32, 32, 32);
			}
		}

		g.setColor(Color.RED);
		g.fillPolygon(polygon.getVertices(), polygon.getTriangles().items);
		g.setColor(Color.BLUE);
		g.drawPolygon(polygon.getVertices());

		g.setColor(Color.RED);
		g.fillTriangle(200f, 200f, 225f, 225f, 175f, 225f);
		g.setColor(Color.BLUE);
		g.drawTriangle(200f, 200f, 225f, 225f, 175f, 225f);

		g.drawRect(32, 32, 64, 64);
		g.scale(scaleX, scaleY);
		g.translate(-(playerX * 32), -(playerY * 32));

		g.rotate(rotation, 32 + 16, 32 + 16);
		g.fillRect(32, 32, 32, 32);
		g.rotate(-rotation, 32 + 16, 32 + 16);

		g.setColor(new Color(0f, 0f, 1f, 0.5f));
		g.setLineHeight(4);
		g.drawLineSegment(0f, 0f, 128f, 128f);
		g.setLineHeight(1);

		g.setColor(Color.RED);

		g.rotate(rotation, 0, 0);
		g.fillRect(128, 32, 64, 64);
		g.rotate(-rotation, 0, 0);

		g.drawCircle(32, 160, 32);
		g.fillCircle(128, 160, 32);

		g.drawString("Hello, world!", 0, 256);

		g.scale(0.5f, 0.5f);
		g.drawSprite(spriteWithTexture);
		g.drawSprite(spriteWithTexture, 512, 64);
		g.drawTexture(texture, 512, 128);
		g.drawTextureRegion(textureRegion, 512, 192);

		g.drawLineSegment(512, 128, 620, 160);

		animation.draw(g, 512, 256);
		g.drawSprite(spriteWithTextureRegion, 512, 320);

		g.drawLineSegment(512, 160, 620, 224);

		if (ninePatch != null) {
			g.drawTextureRegion(atlasTextureRegion, 0, 360);
			g.drawNinePatch(ninePatch, 512, 360, 128, 128);
		}
		if (repeatedNinePatch != null) {
			repeatedNinePatch.render(g, 512, 648, 128, 128);
		}
	}

	public void detectKeyPress(ScreenManager<? extends GameScreen> screenManager) {
		if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
			playerX++;
		} else if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
			playerX--;
		} else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
			playerY--;
		} else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
			playerY++;
		} else if (Gdx.input.isKeyJustPressed(Keys.E)) {
			scaleX++;
			scaleY++;
		} else if (Gdx.input.isKeyJustPressed(Keys.Q)) {
			scaleX--;
			scaleY--;
		} else if (Gdx.input.isKeyJustPressed(Keys.R)) {
			rotation++;
		} else if (Gdx.input.justTouched()) {
			screenManager.enterGameScreen(UATSelectionScreen.SCREEN_ID, new FadeOutTransition(),
					new FadeInTransition());
		}
	}

	@Override
	public void preTransitionIn(Transition transitionIn) {
	}

	@Override
	public void postTransitionIn(Transition transitionIn) {
	}

	@Override
	public void preTransitionOut(Transition transitionOut) {
	}

	@Override
	public void postTransitionOut(Transition transitionOut) {
	}

	@Override
	public int getId() {
		return ScreenIds.getScreenId(GraphicsUAT.class);
	}
}
