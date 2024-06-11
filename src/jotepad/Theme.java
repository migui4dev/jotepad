package jotepad;

import java.awt.Color;

public class Theme {
	private final String name;
	private final Color fgColor, bgColor;

	/**
	 * @param name    is the name of the theme. You can put whatever you want.
	 * @param fgColor is the Foreground/Font color.
	 * @param bgColor is the Background color.
	 * @author sirmigui
	 */
	public Theme(String name, Color fgColor, Color bgColor) {
		this.name = name;
		this.fgColor = fgColor;
		this.bgColor = bgColor;
	}

	public String getName() {
		return name;
	}

	public Color getFgColor() {
		return fgColor;
	}

	public Color getBgColor() {
		return bgColor;
	}

}
