package com.zachgodsell.bankitemvalue;

public enum HighlightMode
{
	TEXT_ONLY("Text only"),
	BOX_OUTLINE("Box outline"),
	BOX_FILL("Box fill"),
	OUTLINE_AND_TEXT("Outline and text"),
	NONE("None");

	private final String name;

	HighlightMode(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
