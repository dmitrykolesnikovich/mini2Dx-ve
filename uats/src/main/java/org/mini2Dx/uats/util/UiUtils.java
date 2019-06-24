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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Pixmap;
import org.mini2Dx.core.controller.*;
import org.mini2Dx.core.controller.deadzone.RadialDeadZone;
import org.mini2Dx.core.exception.ControllerPlatformException;
import org.mini2Dx.core.graphics.CustomCursor;
import org.mini2Dx.ui.UiContainer;
import org.mini2Dx.ui.animation.TextAnimation;
import org.mini2Dx.ui.controller.ControllerUiInput;
import org.mini2Dx.ui.controller.PS4UiInput;
import org.mini2Dx.ui.controller.Xbox360UiInput;
import org.mini2Dx.ui.controller.XboxOneUiInput;
import org.mini2Dx.ui.element.Checkbox;
import org.mini2Dx.ui.element.Label;
import org.mini2Dx.ui.element.RadioButton;
import org.mini2Dx.ui.element.Select;
import org.mini2Dx.ui.element.TextBox;
import org.mini2Dx.ui.element.TextButton;
import org.mini2Dx.ui.element.Visibility;
import org.mini2Dx.ui.listener.ActionListener;
import org.mini2Dx.ui.navigation.UiNavigation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;

/**
 *
 */
public class UiUtils {
	private static Map<String, MdxController<?>> MAPPED_CONTROLLERS = new ConcurrentHashMap<String, MdxController<?>>();
	private static Map<String, ControllerUiInput<?>> MAPPED_CONTROLLER_INPUT = new ConcurrentHashMap<String, ControllerUiInput<?>>();
	private static CustomCursor customCursor;

	public static CustomCursor getCustomCursor(FileHandleResolver fileHandleResolver) {
		if(customCursor == null) {
			customCursor = new CustomCursor(new Pixmap(fileHandleResolver.resolve("default-mdx-cursor-up.png")),
					new Pixmap(fileHandleResolver.resolve("default-mdx-cursor-down.png")), 0, 0);
		}
		return customCursor;
	}
	
	public static ControllerUiInput<?> setUpControllerInput(Controller controller, UiContainer uiContainer) throws ControllerPlatformException {
		if(MAPPED_CONTROLLER_INPUT.containsKey(uiContainer.getId())) {
			return MAPPED_CONTROLLER_INPUT.get(uiContainer.getId());
		}
		switch(ControllerMapping.getControllerType(controller)) {
		case PS4:
			PS4Controller ps4Controller = null;
			if(MAPPED_CONTROLLERS.containsKey(controller.getName())) {
				ps4Controller = (PS4Controller) MAPPED_CONTROLLERS.get(controller.getName());
			} else {
				ps4Controller = ControllerMapping.ps4(controller, new RadialDeadZone(), new RadialDeadZone());
				MAPPED_CONTROLLERS.put(controller.getName(), ps4Controller);
			}
			PS4UiInput ps4UiInput = new PS4UiInput(uiContainer);
			ps4UiInput.setNavigateWithDPad(true);
			ps4Controller.addListener(ps4UiInput);

			MAPPED_CONTROLLER_INPUT.put(uiContainer.getId(), ps4UiInput);
			Gdx.app.log(UiUtils.class.getSimpleName(), "Set up PS4 controller UI input");
			return ps4UiInput;
		case XBOX_360:
			Xbox360Controller xbox360Controller = null;
			if(MAPPED_CONTROLLERS.containsKey(controller.getName())) {
				xbox360Controller = (Xbox360Controller) MAPPED_CONTROLLERS.get(controller.getName());
			} else {
				xbox360Controller = ControllerMapping.xbox360(controller, new RadialDeadZone(), new RadialDeadZone());
				MAPPED_CONTROLLERS.put(controller.getName(), xbox360Controller);
			}
			Xbox360UiInput xbox360UiInput = new Xbox360UiInput(uiContainer);
			xbox360UiInput.setNavigateWithDPad(true);
			xbox360Controller.addListener(xbox360UiInput);
			
			MAPPED_CONTROLLER_INPUT.put(uiContainer.getId(), xbox360UiInput);
			Gdx.app.log(UiUtils.class.getSimpleName(), "Set up Xbox 360 controller UI input");
			return xbox360UiInput;
		case XBOX_ONE:
			XboxOneController xboxOneController = null;
			if(MAPPED_CONTROLLERS.containsKey(controller.getName())) {
				xboxOneController = (XboxOneController) MAPPED_CONTROLLERS.get(controller.getName());
			} else {
				xboxOneController = ControllerMapping.xboxOne(controller, new RadialDeadZone(), new RadialDeadZone());
				MAPPED_CONTROLLERS.put(controller.getName(), xboxOneController);
			}
			
			XboxOneUiInput xboxOneUiInput = new XboxOneUiInput(uiContainer);
			xboxOneUiInput.setNavigateWithDPad(true);
			xboxOneController.addListener(xboxOneUiInput);
			
			MAPPED_CONTROLLER_INPUT.put(uiContainer.getId(), xboxOneUiInput);
			Gdx.app.log(UiUtils.class.getSimpleName(), "Set up Xbox One controller UI input");
			return xboxOneUiInput;
		case UNKNOWN:
		default:
			break;
		}
		return null;
	}

	public static Label createHeader(String text) {
		return createLabel(text, "header", Label.COLOR_BLACK);
	}
	
	public static Label createHeader(String text, TextAnimation textAnimation) {
		return createLabel(text, "header", Label.COLOR_BLACK, textAnimation);
	}
	
	public static Label createHeader(String text, TextAnimation textAnimation, boolean debug) {
		return createLabel(text, "header", Label.COLOR_BLACK, textAnimation, debug);
	}
	
	public static Label createLabel(String text) {
		return createLabel(text, "default", Label.COLOR_BLACK);
	}
	
	public static Label createLabel(String text, boolean debug) {
		return createLabel(text, "default", Label.COLOR_BLACK, null, debug);
	}
	
	public static Label createLabel(String text, TextAnimation textAnimation) {
		return createLabel(text, "default", Label.COLOR_BLACK, textAnimation);
	}
	
	public static Label createLabel(String text, TextAnimation textAnimation, boolean debug) {
		return createLabel(text, "default", Label.COLOR_BLACK, textAnimation, debug);
	}

	private static Label createLabel(String text, String styleId, Color color) {
		return createLabel(text, styleId, color, null);
	}
	
	private static Label createLabel(String text, String styleId, Color color, TextAnimation textAnimation) {
		return createLabel(text, styleId, color, textAnimation, false);
	}
	
	private static Label createLabel(String text, String styleId, Color color, TextAnimation textAnimation, boolean debug) {
		final String id;
		if(text == null || text.trim().isEmpty()) {
			id = null;
		} else {
			id = "Label: " + text;
		}
		Label label = new Label(id);
		label.setText(text);
		label.setStyleId(styleId);
		label.setColor(color);
		label.setVisibility(Visibility.VISIBLE);
		label.setTextAnimation(textAnimation);
		label.setDebugEnabled(debug);
		return label;
	}

	public static TextButton createButton(UiNavigation navigation, String text, ActionListener listener) {
		return createButton(navigation, text, false, listener);
	}
	
	public static TextButton createButton(UiNavigation navigation, String text, boolean debug, ActionListener listener) {
		final String id;
		if(text == null || text.isEmpty()) {
			id = null;
		} else {
			id = "TextButton: " + text;
		}
		TextButton button = new TextButton(id);
		button.setFlexLayout("flex-column:xs-12c");
		button.setText(text);
		if(listener != null) {
			button.addActionListener(listener);
		}
		button.setDebugEnabled(debug);
		button.setVisibility(Visibility.VISIBLE);
		
		if(navigation != null) {
			navigation.add(button);
		}
		return button;
	}
	
	public static TextBox createTextBox(UiNavigation navigation, String id, ActionListener listener) {
		TextBox textBox = new TextBox(id);
		textBox.setFlexLayout("flex-column:xs-12c");
		textBox.addActionListener(listener);
		textBox.setVisibility(Visibility.VISIBLE);
		
		if(navigation != null) {
			navigation.add(textBox);
		}
		return textBox;
	}
	
	public static Select<String> createSelect(UiNavigation navigation, String id, ActionListener listener) {
		Select<String> select = new Select<String>(id);
		select.setFlexLayout("flex-column:xs-12c");
		select.addActionListener(listener);
		select.setVisibility(Visibility.VISIBLE);
		
		if(navigation != null) {
			navigation.add(select);
		}
		return select;
	}
	
	public static Checkbox createCheckbox(UiNavigation navigation, String id, ActionListener listener) {
		Checkbox checkbox = new Checkbox(id);
		checkbox.addActionListener(listener);
		checkbox.setVisibility(Visibility.VISIBLE);
		
		if(navigation != null) {
			navigation.add(checkbox);
		}
		return checkbox;
	}
	
	public static RadioButton createRadioButton(UiNavigation navigation, String id, ActionListener listener) {
		RadioButton radioButton = new RadioButton(id);
		radioButton.addActionListener(listener);
		radioButton.setVisibility(Visibility.VISIBLE);
		
		if(navigation != null) {
			navigation.add(radioButton);
		}
		return radioButton;
	}
}
