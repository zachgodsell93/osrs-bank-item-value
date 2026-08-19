package com.zachgodsell.bankitemvalue;

public enum PriceSource
{
	GRAND_EXCHANGE("Grand Exchange"),
	HIGH_ALCHEMY("High Alchemy"),
	HIGHEST("Highest of both");

	private final String name;

	PriceSource(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
