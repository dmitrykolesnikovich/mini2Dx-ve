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
package org.mini2Dx.uats.util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import org.mini2Dx.core.Mdx;
import org.mini2Dx.core.exception.ControllerPlatformException;
import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.screen.BasicGameScreen;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.core.screen.Transition;
import org.mini2Dx.core.screen.transition.FadeInTransition;
import org.mini2Dx.core.screen.transition.FadeOutTransition;
import org.mini2Dx.uats.*;
import org.mini2Dx.ui.InputSource;
import org.mini2Dx.ui.UiContainer;
import org.mini2Dx.ui.animation.TypingTextAnimation;
import org.mini2Dx.ui.controller.ControllerUiInput;
import org.mini2Dx.ui.effect.SlideIn;
import org.mini2Dx.ui.element.Container;
import org.mini2Dx.ui.element.FlexRow;
import org.mini2Dx.ui.element.Visibility;
import org.mini2Dx.ui.event.ActionEvent;
import org.mini2Dx.ui.layout.HorizontalAlignment;
import org.mini2Dx.ui.layout.ScreenSize;
import org.mini2Dx.ui.layout.VerticalAlignment;
import org.mini2Dx.ui.listener.ActionListener;
import org.mini2Dx.ui.listener.ScreenSizeListener;
import org.mini2Dx.ui.navigation.VerticalUiNavigation;
import org.mini2Dx.ui.style.UiTheme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;

/**
 *
 * @author Thomas Cashman
 */
public class UATSelectionScreen extends BasicGameScreen implements ScreenSizeListener {
	public static final int SCREEN_ID = 1;

	private final AssetManager assetManager;
	private final FileHandleResolver fileHandleResolver;

	private UiContainer uiContainer;
	private VerticalUiNavigation uiNavigation;
	private ControllerUiInput<?> controllerInput;
	private Container uatsDialog;
	private InputSource lastInputSource = InputSource.KEYBOARD_MOUSE;
	private int nextScreenId = -1;

	public UATSelectionScreen(AssetManager assetManager, FileHandleResolver fileHandleResolver) {
		this.assetManager = assetManager;
		this.fileHandleResolver = fileHandleResolver;
	}

	@Override
	public void initialise(GameContainer gc) {
		uiContainer = new UiContainer(gc, assetManager);
		uiContainer.addScreenSizeListener(this);
		uiContainer.setNavigationMode(UATApplication.NAVIGATION_MODE);
		uiContainer.addActionKey(Input.Keys.E);
		initialiseUi();
		
		if(Controllers.getControllers().size > 0) {
			try {
				controllerInput = UiUtils.setUpControllerInput(Controllers.getControllers().get(0), uiContainer);
			} catch (ControllerPlatformException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> screenManager, float delta) {
		uiContainer.update(delta);

		if(!lastInputSource.equals(uiContainer.getLastInputSource())) {
			lastInputSource = uiContainer.getLastInputSource();
		}

		if (nextScreenId > -1) {
			screenManager.enterGameScreen(nextScreenId, new FadeOutTransition(), new FadeInTransition());
			nextScreenId = -1;
		}
	}

	@Override
	public void interpolate(GameContainer gc, float alpha) {
		uiContainer.interpolate(alpha);
	}

	@Override
	public void render(GameContainer gc, Graphics g) {
		g.clearBlendFunction();
		g.clearShaderProgram();
		g.removeClip();
		g.setBackgroundColor(Color.WHITE);
		g.setColor(Color.BLUE);

		uiContainer.render(g);
	}

	@Override
	public void preTransitionIn(Transition transitionIn) {
		nextScreenId = -1;
		if (!UiContainer.isThemeApplied()) {
			UiContainer.setTheme(assetManager.get(UiTheme.DEFAULT_THEME_FILENAME, UiTheme.class));
		}

		final InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(UiUtils.getCustomCursor(fileHandleResolver));
		inputMultiplexer.addProcessor(uiContainer);
		Gdx.input.setInputProcessor(inputMultiplexer);

		uatsDialog.applyEffect(new SlideIn());
	}

	@Override
	public void postTransitionIn(Transition transitionIn) {
		uiContainer.setActiveNavigation(uatsDialog);
		uiNavigation.resetCursor(true);
		if(controllerInput != null) {
			controllerInput.enable();
		}
	}

	@Override
	public void preTransitionOut(Transition transitionOut) {
		if(controllerInput != null) {
			controllerInput.disable();
		}
	}

	@Override
	public void postTransitionOut(Transition transitionOut) {
		uatsDialog.setVisibility(Visibility.HIDDEN);
	}

	@Override
	public int getId() {
		return SCREEN_ID;
	}

	private void initialiseUi() {
		uatsDialog = new Container("uats-dialog");
		uatsDialog.setFlexLayout("flex-column:xs-12c sm-10c md-8c lg-6c sm-offset-1c md-offset-2c lg-offset-3c");
		uiNavigation = new VerticalUiNavigation();
		
		uatsDialog.add(FlexRow.withElements("row-os", UiUtils.createLabel("Detected OS: " + Mdx.os)));
		uatsDialog.add(FlexRow.withElements("row-header", UiUtils.createHeader("User Acceptance Tests", new TypingTextAnimation())));
		uatsDialog.add(FlexRow.withElements("row-blending", UiUtils.createButton(uiNavigation, "Blending", false, new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(BlendingUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-clip", UiUtils.createButton(uiNavigation, "Graphics.clip()", false, new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(ClippingUAT.class);
					}
				})));
		uatsDialog.add(FlexRow.withElements("row-geometry", UiUtils.createButton(uiNavigation, "Geometry", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(GeometryUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-graphics", UiUtils.createButton(uiNavigation, "Graphics", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(GraphicsUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-textureregion", UiUtils.createButton(uiNavigation, "Texture Regions", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(TextureRegionUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-sprite", UiUtils.createButton(uiNavigation, "Sprites", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(SpriteUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-viewports", UiUtils.createButton(uiNavigation, "Viewports", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(ViewportUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-monospace", UiUtils.createButton(uiNavigation, "Monospace Font", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(MonospaceFontUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-audio", UiUtils.createButton(uiNavigation, "Audio", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(AudioUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-orthogonal-tiledmap",
				UiUtils.createButton(uiNavigation, "Orthogonal TiledMap (No Caching)", new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(OrthogonalTiledMapNoCachingUAT.class);
					}
				})));
		uatsDialog.add(FlexRow.withElements("row-orthogonal-tiledmap-caching",
				UiUtils.createButton(uiNavigation, "Orthogonal TiledMap (With Caching)", new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(OrthogonalTiledMapWithCachingUAT.class);
					}
				})));
		uatsDialog.add(FlexRow.withElements("row-isometric-tiledmap",
				UiUtils.createButton(uiNavigation, "Isometric TiledMap (No Caching)", new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(IsometricTiledMapUAT.class);
					}
				})));
		uatsDialog.add(FlexRow.withElements("row-isometric-tiledmap",
				UiUtils.createButton(uiNavigation, "Hexagonal TiledMap (No Caching)", new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(HexagonalTiledMapUAT.class);
					}
				})));
		uatsDialog.add(
				FlexRow.withElements("row-particle-effects", UiUtils.createButton(uiNavigation, "Particle Effects", new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(ParticleEffectsUAT.class);
					}
				})));
		uatsDialog.add(FlexRow.withElements("row-controllers", UiUtils.createButton(uiNavigation, "Controllers", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(ControllerUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-ui-flex", UiUtils.createButton(uiNavigation, "Flex UI Layout", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(FlexUiUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-ui-pixel", UiUtils.createButton(uiNavigation, "Pixel UI Layout", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(PixelUiUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-ui-serialization", UiUtils.createButton(uiNavigation, "UI Serialization", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(UiSerializationUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-stage", UiUtils.createButton(uiNavigation, "LibGDX Stage", new ActionListener() {
			@Override
			public void onActionBegin(ActionEvent event) {
			}

			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = ScreenIds.getScreenId(StageUAT.class);
			}
		})));
		uatsDialog.add(FlexRow.withElements("row-utilities", UiUtils.createHeader("Utilities")));
		uatsDialog.add(
				FlexRow.withElements("row-controller-mapping", UiUtils.createButton(uiNavigation, "Controller Mapping", new ActionListener() {
					@Override
					public void onActionBegin(ActionEvent event) {
					}

					@Override
					public void onActionEnd(ActionEvent event) {
						nextScreenId = ScreenIds.getScreenId(ControllerMapping.class);
					}
				})));
		uatsDialog.setVisibility(Visibility.VISIBLE);
		uatsDialog.setNavigation(uiNavigation);

		uiContainer.add(uatsDialog);

		uatsDialog.snapTo(uiContainer, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
	}

	@Override
	public void onResize(int width, int height) {
		uiContainer.set(width, height);
	}
	
	@Override
	public void onScreenSizeChanged(ScreenSize screenSize) {
		System.out.println("Current Screen Size: " + screenSize);
	}
}
