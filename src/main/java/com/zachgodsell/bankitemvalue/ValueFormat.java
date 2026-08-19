package com.zachgodsell.bankitemvalue;

public enum ValueFormat
{
	SHORT("Short (73K)"),
	DECIMAL("Decimal (73.5K)"),
	EXACT("Exact (73,542)");

	private final String name;

	ValueFormat(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
