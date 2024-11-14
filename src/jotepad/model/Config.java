package jotepad.model;

import java.io.Serializable;

public class Config implements Serializable {
	private static final long serialVersionUID = 6066124863275515889L;

	private String themeName;
	private String fontName;
	private int fontSize;

	public Config(String themeName, String fontName, int fontSize) {
		this.themeName = themeName;
		this.fontName = fontName;
		this.fontSize = fontSize;
	}

	public String getThemeName() {
		return themeName;
	}

	public String getFontName() {
		return fontName;
	}

	public int getFontSize() {
		return fontSize;
	}

	@Override
	public String toString() {
		return String.format("%s %s %d", themeName, fontName, fontSize);
	}

}
