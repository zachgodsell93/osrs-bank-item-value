package com.zachgodsell.bankitemvalue;

public enum TextPosition
{
	TOP_LEFT("Top left"),
	TOP_RIGHT("Top right"),
	BOTTOM_LEFT("Bottom left"),
	BOTTOM_RIGHT("Bottom right"),
	CENTER("Center");

	private final String name;

	TextPosition(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
