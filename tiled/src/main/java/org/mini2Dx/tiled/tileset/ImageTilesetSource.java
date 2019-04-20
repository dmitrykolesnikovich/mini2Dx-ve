/**
 * Copyright (c) 2017 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.tiled.tileset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.graphics.Sprite;
import org.mini2Dx.core.graphics.TextureRegion;
import org.mini2Dx.tiled.Tile;

/**
 * A {@link TilesetSource} referenced by image directly in a TMX file
 */
public class ImageTilesetSource extends TilesetSource {
	private final Tile[][] tiles;
	private final IntMap<Sprite> tileImages = new IntMap<Sprite>();
	private final int width, height;
	private final int tileWidth, tileHeight;
	private final int spacing, margin;
	
	private String name, tilesetImagePath, transparentColorValue;
	private ObjectMap<String, String> properties;
	private int widthInTiles, heightInTiles;

	private Texture backingTexture;
	private TextureRegion textureRegion;

	public ImageTilesetSource(int width, int height, int tileWidth, int tileHeight, int spacing, int margin) {
		super();
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.spacing = spacing;
		this.margin = margin;

		this.widthInTiles = -1;
		this.heightInTiles = -1;
		tiles = new Tile[getWidthInTiles()][getHeightInTiles()];
		for(int x = 0; x < getWidthInTiles(); x++) {
			for(int y = 0; y < getHeightInTiles(); y++) {
				tiles[x][y] = new Tile();
				tiles[x][y].setTileId(getTileId(x, y, 0));
			}
		}
	}
	
	private Texture modifyPixmapWithTransparentColor(Pixmap pixmap) {
		float r = Integer.parseInt(transparentColorValue.substring(0, 2), 16) / 255f;
		float g = Integer.parseInt(transparentColorValue.substring(2, 4), 16) / 255f;
		float b = Integer.parseInt(transparentColorValue.substring(4, 6), 16) / 155f;
		
		int transparentColor = Color.rgba8888(new Color(r, g, b, 1f));
		
		Pixmap updatedPixmap = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
		
		for(int x = 0; x < pixmap.getWidth(); x++) {
			for(int y = 0; y < pixmap.getHeight(); y++) {
				int pixelColor = pixmap.getPixel(x, y);
				if(pixelColor != transparentColor) {
					updatedPixmap.drawPixel(x, y, pixelColor);
				}
			}
		}
		
		final Texture result = new Texture(updatedPixmap);
		updatedPixmap.dispose();
		pixmap.dispose();
		return result;
	}
	
	@Override
	public Array<AssetDescriptor> getDependencies(FileHandle tmxPath) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		dependencies.add(new AssetDescriptor(tilesetImagePath, Pixmap.class));
		return dependencies;
	}

	@Override
	public void loadTexture(FileHandle tmxPath) {
		if(textureRegion != null) {
			return;
		}
		switch(tmxPath.type()) {
		case Classpath:
			loadTileImages(new Pixmap(Gdx.files.classpath(tilesetImagePath)));
			break;
		case Internal:
			loadTileImages(new Pixmap(Gdx.files.internal(tilesetImagePath)));
			break;
		case External:
			loadTileImages(new Pixmap(Gdx.files.external(tilesetImagePath)));
			break;
		case Absolute:
			loadTileImages(new Pixmap(Gdx.files.absolute(tilesetImagePath)));
			break;
		case Local:
			loadTileImages(new Pixmap(Gdx.files.local(tilesetImagePath)));
			break;
		}
	}
	
	@Override
	public void loadTexture(AssetManager assetManager, FileHandle tmxPath) {
		if(textureRegion != null) {
			return;
		}
		loadTileImages(assetManager.get(tilesetImagePath, Pixmap.class));
	}

	@Override
	public void loadTexture(TextureAtlas textureAtlas) {
		if(textureRegion != null) {
			return;
		}
		final TextureAtlas.AtlasRegion atlasRegion = textureAtlas.findRegion(tilesetImagePath);
		if(atlasRegion == null && tilesetImagePath.lastIndexOf('.') > -1) {
			loadTileImages(new TextureRegion(textureAtlas.findRegion(
					tilesetImagePath.substring(0, tilesetImagePath.lastIndexOf('.')))));
		} else {
			loadTileImages(new TextureRegion(atlasRegion));
		}
	}

	private void loadTileImages(TextureRegion textureRegion) {
		if(transparentColorValue != null) {
			backingTexture = modifyPixmapWithTransparentColor(textureRegion.toPixmap());
			this.textureRegion = new TextureRegion(backingTexture);
		} else {
			this.textureRegion = textureRegion;
		}
		cutTiles();
	}

	private void loadTileImages(Pixmap pixmap) {
		if(transparentColorValue != null) {
			backingTexture = modifyPixmapWithTransparentColor(pixmap);
			textureRegion = new TextureRegion(backingTexture);
		} else {
			backingTexture = new Texture(pixmap);
			textureRegion = new TextureRegion(backingTexture);
			pixmap.dispose();
		}
		cutTiles();
	}

	private void cutTiles() {
		for (int x = 0; x < getWidthInTiles(); x++) {
			for (int y = 0; y < getHeightInTiles(); y++) {
				int tileX = margin + (x * spacing) + (x * tileWidth);
				int tileY = margin + (y * spacing) + (y * tileHeight);
				Sprite tileImage = new Sprite(textureRegion, tileX, tileY,
						tileWidth, tileHeight);
				tileImages.put(tiles[x][y].getTileId(0), tileImage);
			}
		}
	}

	@Override
	public boolean isTextureLoaded() {
		return textureRegion != null;
	}
	
	@Override
	public Sprite getTileImage(int tileId) {
		return tileImages.get(tileId);
	}

	@Override
	public int getWidthInTiles() {
		if (widthInTiles < 0) {
			int result = 0;
			for (int x = margin; x <= width - tileWidth; x += tileWidth + spacing) {
				result++;
			}
			widthInTiles = result;
		}
		return widthInTiles;
	}

	@Override
	public int getHeightInTiles() {
		if (heightInTiles < 0) {
			int result = 0;
			for (int y = margin; y <= height - tileHeight; y += tileHeight + spacing) {
				result++;
			}
			heightInTiles = result;
		}
		return heightInTiles;
	}
	
	@Override
	public Tile getTileByPosition(int x, int y) {
		return tiles[x][y];
	}
	
	@Override
	public Tile getTile(int tileId, int firstGid) {
		int tileX = getTileX(tileId, firstGid);
		int tileY = getTileY(tileId, firstGid);
		return tiles[tileX][tileY];
	}

	@Override
	public void drawTile(Graphics g, int tileId, int firstGid, int renderX, int renderY) {
		int tileX = getTileX(tileId, firstGid);
		int tileY = getTileY(tileId, firstGid);
		tiles[tileX][tileY].draw(g, renderX, renderY); 
	}

	@Override
	public void drawTileset(Graphics g, int renderX, int renderY) {
		for (int y = 0; y < getHeightInTiles(); y++) {
			for (int x = 0; x < getWidthInTiles(); x++) {
				tiles[x][y].draw(g, renderX + (x * getTileWidth()), renderY
						+ (y * getTileHeight())); 
			}
		}
	}
	
	@Override
	public boolean containsProperty(String propertyName) {
		if(properties == null)
			return false;
		return properties.containsKey(propertyName);
	}

	@Override
	public String getProperty(String propertyName) {
		if(properties == null)
			return null;
		return properties.get(propertyName);
	}
	
	@Override
	public void setProperty(String propertyName, String value) {
		if(properties == null)
			properties = new ObjectMap<String, String>();
		properties.put(propertyName, value);
	}
	
	@Override
	public ObjectMap<String, String> getProperties() {
		return properties;
	}
	
	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getTileWidth() {
		return tileWidth;
	}

	@Override
	public int getTileHeight() {
		return tileHeight;
	}
	
	@Override
	public int getSpacing() {
		return spacing;
	}

	@Override
	public int getMargin() {
		return margin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getInternalUuid() {
		return tilesetImagePath;
	}

	public String getTilesetImagePath() {
		return tilesetImagePath;
	}

	public void setTilesetImagePath(String tilesetImagePath) {
		this.tilesetImagePath = tilesetImagePath;
	}

	public String getTransparentColorValue() {
		return transparentColorValue;
	}

	public void setTransparentColorValue(String transparentColorValue) {
		this.transparentColorValue = transparentColorValue;
	}

	@Override
	public void dispose() {
		for (int x = 0; x < getWidthInTiles(); x++) {
			for (int y = 0; y < getHeightInTiles(); y++) {
				if(tiles[x][y] != null) {
					tiles[x][y].dispose();
				}
			}
		}
		textureRegion = null;

		if(backingTexture == null) {
			return;
		}
		backingTexture.dispose();
		backingTexture = null;
	}
}
