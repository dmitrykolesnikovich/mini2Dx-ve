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

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import org.mini2Dx.core.Mdx;
import org.mini2Dx.core.exception.ControllerPlatformException;
import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.game.GameResizeListener;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.screen.BasicGameScreen;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.core.screen.Transition;
import org.mini2Dx.core.screen.transition.FadeInTransition;
import org.mini2Dx.core.screen.transition.FadeOutTransition;
import org.mini2Dx.uats.util.ScreenIds;
import org.mini2Dx.uats.util.UATApplication;
import org.mini2Dx.uats.util.UATSelectionScreen;
import org.mini2Dx.uats.util.UiUtils;
import org.mini2Dx.ui.UiContainer;
import org.mini2Dx.ui.controller.ControllerUiInput;
import org.mini2Dx.ui.element.*;
import org.mini2Dx.ui.event.ActionEvent;
import org.mini2Dx.ui.layout.FlexDirection;
import org.mini2Dx.ui.layout.HorizontalAlignment;
import org.mini2Dx.ui.layout.VerticalAlignment;
import org.mini2Dx.ui.listener.ActionListener;
import org.mini2Dx.ui.navigation.VerticalUiNavigation;
import org.mini2Dx.ui.style.UiTheme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;

/**
 * User acceptance test for the mini2Dx flex UI layout
 */
public class FlexUiUAT extends BasicGameScreen implements GameResizeListener {
	private final AssetManager assetManager;
	private final FileHandleResolver fileHandleResolver;

	private UiContainer uiContainer;
	private ControllerUiInput<?> controllerInput;
	
	private Container topLeftContainer, centerContainer, bottomRightContainer;
	
	private TabView tabView;
	private Select<String> select;
	private TextBox textBox;
	private Checkbox checkbox;
	private RadioButton radioButton;
	private Slider slider;
	private Label textBoxResult, checkboxResult, radioButtonResult, sliderResult;
	
	private int nextScreenId = -1;

	public FlexUiUAT(AssetManager assetManager, FileHandleResolver fileHandleResolver) {
		super();
		this.assetManager = assetManager;
		this.fileHandleResolver = fileHandleResolver;
	}

	@Override
	public void initialise(GameContainer gc) {
		uiContainer = new UiContainer(gc, assetManager);
		uiContainer.setNavigationMode(UATApplication.NAVIGATION_MODE);
		gc.addResizeListener(this);
		initialiseUi();
		
		if(Controllers.getControllers().size > 0) {
			try {
				System.out.println(uiContainer.getId());
				controllerInput = UiUtils.setUpControllerInput(Controllers.getControllers().get(0), uiContainer);
				if(controllerInput != null) {
					controllerInput.disable();
				}
			} catch (ControllerPlatformException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onResize(int width, int height) {
		centerContainer.snapTo(uiContainer, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
		bottomRightContainer.snapTo(uiContainer, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);
		uiContainer.set(width, height);
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> screenManager, float delta) {
		uiContainer.update(delta);
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
		g.setBackgroundColor(Color.WHITE);
		g.setColor(Color.BLACK);
		Mdx.performanceTracker.drawInTopRight(g);
		
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

		centerContainer.snapTo(uiContainer, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
		bottomRightContainer.snapTo(uiContainer, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);
	}
	
	@Override
	public void postTransitionIn(Transition transitionIn) {
		uiContainer.setActiveNavigation(tabView);
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
	public int getId() {
		return ScreenIds.getScreenId(FlexUiUAT.class);
	}

	private void initialiseUi() {
		topLeftContainer = new Container("top-left-frame");
		topLeftContainer.setFlexLayout("flex-col:xs-12c sm-6c md-4c lg-3c");
		topLeftContainer.setStyleId("no-background");

		final Label headerLabel = UiUtils.createLabel("UI UAT");
		FlexRow topLeftHeaderFlexRow = FlexRow.withElements("top-left-header", headerLabel);
		topLeftHeaderFlexRow.setFlexLayout("flex-center:xs-12c,xs-20px");
		topLeftContainer.add(topLeftHeaderFlexRow);

		uiContainer.deferUntilUpdate(new Runnable() {
			@Override
			public void run() {
				headerLabel.setStyleId("header");
			}
		},10f);
		uiContainer.deferUntilUpdate(new Runnable() {
			@Override
			public void run() {
				topLeftContainer.setVisibility(Visibility.VISIBLE);
			}
		},5f);
		
		Button backRowButton = UiUtils.createButton(null, "", new ActionListener() {
			
			@Override
			public void onActionEnd(ActionEvent event) {
			}
			
			@Override
			public void onActionBegin(ActionEvent event) {
			}
		});
		backRowButton.setFlexLayout("flex-center:xs-12c,xs-20px");
		FlexRow backFlexRow = FlexRow.withElements("behind-header", backRowButton);
		backFlexRow.setZIndex(-1);
		topLeftContainer.add(backFlexRow);

		uiContainer.add(topLeftContainer);
		
		VerticalUiNavigation tab1Navigation = new VerticalUiNavigation();
		textBox = UiUtils.createTextBox(tab1Navigation, "textbox", new ActionListener() {
			
			@Override
			public void onActionEnd(ActionEvent event) {
				textBoxResult.setText(textBox.getValue());
			}
			
			@Override
			public void onActionBegin(ActionEvent event) {
			}
		});
		select = UiUtils.createSelect(tab1Navigation, "select", new ActionListener() {
			
			@Override
			public void onActionEnd(ActionEvent event) {
				System.out.println("Selected value: " + select.getSelectedOption().getValue());
			}
			
			@Override
			public void onActionBegin(ActionEvent event) {
			}
		});
		textBoxResult = UiUtils.createLabel("");
		checkboxResult = UiUtils.createLabel("Radio Button::responsive = false");
		checkbox = UiUtils.createCheckbox(tab1Navigation, "checkbox", new ActionListener() {
			
			@Override
			public void onActionEnd(ActionEvent event) {
				if(checkbox.isChecked()) {
					radioButton.setResponsive(true);
				} else {
					radioButton.setResponsive(false);
				}
				checkboxResult.setText("Radio Button::responsive = " + checkbox.isChecked());
			}
			
			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		
		radioButtonResult = UiUtils.createLabel("");
		radioButton = UiUtils.createRadioButton(tab1Navigation, "radioButton", new ActionListener() {
			
			@Override
			public void onActionEnd(ActionEvent event) {
				switch(radioButton.getSelectedOption()) {
				case "FlexRow Layout":
					radioButton.setFlexDirection(FlexDirection.ROW);
					break;
				case "FlexRow-Reverse Layout":
					radioButton.setFlexDirection(FlexDirection.ROW_REVERSE);
					break;
				case "Div Layout":
					radioButton.setFlexDirection(FlexDirection.COLUMN);
					break;
				case "Div-Reverse Layout":
					radioButton.setFlexDirection(FlexDirection.COLUMN_REVERSE);
					break;
				}
				radioButtonResult.setText(radioButton.getSelectedOption());
			}
			
			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		radioButton.addOption("FlexRow Layout");
		radioButton.addOption("FlexRow-Reverse Layout");
		radioButton.addOption("Column Layout");
		radioButton.addOption("Column-Reverse Layout");
		
		slider = new Slider();
		slider.setVisibility(Visibility.VISIBLE);
		slider.addActionListener(new ActionListener() {
			
			@Override
			public void onActionEnd(ActionEvent event) {
				sliderResult.setText(String.valueOf(slider.getValue()));
			}
			
			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		sliderResult = UiUtils.createLabel("0.0");
		
		TextButton returnButton = UiUtils.createButton(tab1Navigation, "Return to UAT Selection Screen", new ActionListener() {
			
			@Override
			public void onActionBegin(ActionEvent event) {}
			
			@Override
			public void onActionEnd(ActionEvent event) {
				nextScreenId = UATSelectionScreen.SCREEN_ID;
			}
		});
		
		select.addOption("Item 1", "1");
		select.addOption("Item 2", "2");
		select.addOption("Item 3", "3");
		
		centerContainer = new Container("main-centerContainer");
		centerContainer.setFlexLayout("flex-column:xs-12c md-8c lg-6c md-offset-2c lg-offset-3c");
		
		tabView = new TabView("tabView");
		tabView.setFlexLayout("flex-column:xs-12c");
		tabView.setVisibility(Visibility.VISIBLE);
		
		Tab tab1 = new Tab("tab1", "Tab 1");
		tab1.add(FlexRow.withElements("row-textbox", textBox, textBoxResult));
		tab1.add(FlexRow.withElements("row-select", select));
		tab1.add(FlexRow.withElements("row-checkbox", checkbox, checkboxResult));
		tab1.add(FlexRow.withElements("row-radioButton", radioButton, radioButtonResult));
		tab1.add(FlexRow.withElements("row-slider", slider, sliderResult));
		tab1.add(FlexRow.withElements("row-return-button", returnButton));
		tab1.setNavigation(tab1Navigation);
		tabView.add(tab1);
		
		Tab tab2 = new Tab("tab2", "Tab 2");
		Div xsHiddenDiv = Div.withElements("col-not-visible-xs", UiUtils.createLabel("Not visible on XS screen size"));
		xsHiddenDiv.setFlexLayout("flex-column:xs-0c sm-12c");

		ProgressBar progressBar = new ProgressBar();
		progressBar.setFlexLayout("flex-column:xs-12c");
		progressBar.setValue(0.4f);
		progressBar.setVisibility(Visibility.VISIBLE);
		tab2.add(FlexRow.withElements(progressBar));

		tab2.add(FlexRow.withElements("row-not-visible-xs", xsHiddenDiv));

		Button reAddElementsButton =  UiUtils.createButton(null, "Re-add tabview", new ActionListener() {

			@Override
			public void onActionEnd(ActionEvent event) {
				centerContainer.remove(tabView);
				centerContainer.add(tabView);
			}

			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		tab2.add(FlexRow.withElements(reAddElementsButton));

		tabView.add(tab2);

		Tab tab3 = new Tab("tab3", "Tab 3");
		Button hiddenButton = UiUtils.createButton(null, "Hidden", new ActionListener() {

			@Override
			public void onActionEnd(ActionEvent event) {}

			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		hiddenButton.setVisibility(Visibility.HIDDEN);
		tab3.add(FlexRow.withElements(hiddenButton));
		final ScrollBox scrollBox = new ScrollBox("scrollBox");
		scrollBox.setFlexLayout("flex-column:xs-12c");
		scrollBox.setVisibility(Visibility.VISIBLE);
		scrollBox.setMaxHeight(300f);
		for(int i = 0; i < 30; i++) {
			if(i % 2 == 0) {
				Label label = UiUtils.createLabel("Label " + i);
				FlexRow flexRow = FlexRow.withElements(label);
				flexRow.setVisibility(Visibility.VISIBLE);
				scrollBox.add(flexRow);
			} else {
				Button button = UiUtils.createButton(null, "Test", null);
				FlexRow flexRow = FlexRow.withElements(button);
				flexRow.setVisibility(Visibility.VISIBLE);
				scrollBox.add(flexRow);
			}
		}
		tab3.add(FlexRow.withElements(scrollBox));

		Button scrollToTopButton = UiUtils.createButton(null, "Scroll to top (immediate)", new ActionListener() {

			@Override
			public void onActionEnd(ActionEvent event) {
				scrollBox.scrollToTop(true);
			}

			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		tab3.add(FlexRow.withElements(scrollToTopButton));

		Button scrollToBottomButton = UiUtils.createButton(null, "Scroll to bottom (immediate)", new ActionListener() {

			@Override
			public void onActionEnd(ActionEvent event) {
				scrollBox.scrollToBottom(true);
			}

			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		tab3.add(FlexRow.withElements(scrollToBottomButton));

		scrollToTopButton = UiUtils.createButton(null, "Scroll to top (smooth)", new ActionListener() {

			@Override
			public void onActionEnd(ActionEvent event) {
				scrollBox.scrollToTop(false);
			}

			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		tab3.add(FlexRow.withElements(scrollToTopButton));

		scrollToBottomButton = UiUtils.createButton(null, "Scroll to bottom (smooth)", new ActionListener() {

			@Override
			public void onActionEnd(ActionEvent event) {
				scrollBox.scrollToBottom(false);
			}

			@Override
			public void onActionBegin(ActionEvent event) {}
		});
		tab3.add(FlexRow.withElements(scrollToBottomButton));

		tabView.add(tab3);
		
		tabView.setNextTabHotkey(Keys.E);
		tabView.setPreviousTabHotkey(Keys.Q);
		
		centerContainer.add(tabView);
		centerContainer.setVisibility(Visibility.VISIBLE);
		centerContainer.setNavigation(tabView.getNavigation());
		uiContainer.add(centerContainer);
		
		bottomRightContainer = new Container("bottom-right-frame");
		bottomRightContainer.setFlexLayout("flex-column:xs-12c sm-6c md-4c lg-3c");
		bottomRightContainer.setVisibility(Visibility.VISIBLE);
		FlexRow bottomFrameFlexRow = FlexRow.withElements("row-os", UiUtils.createHeader("OVERFLOW CLIPPED"));
		bottomFrameFlexRow.setFlexLayout("flex-col-r:xs-12c,xs-12px");
		bottomFrameFlexRow.setOverflowClipped(true);
		bottomRightContainer.add(bottomFrameFlexRow);
		uiContainer.add(bottomRightContainer);

		centerContainer.snapTo(uiContainer, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
		bottomRightContainer.snapTo(uiContainer, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);
	}
}
