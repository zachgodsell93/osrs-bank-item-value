package com.zachgodsell.bankitemvalue;

import java.awt.Color;
import java.util.List;

final class ValueTier
{
	final long threshold;
	final Color color;

	ValueTier(long threshold, Color color)
	{
		this.threshold = threshold;
		this.color = color;
	}

	/**
	 * Resolve a value to a tier colour. The winning tier is the highest
	 * threshold that is less than or equal to the value. {@code tiers} must be
	 * sorted by threshold, descending.
	 */
	static Color resolve(long value, List<ValueTier> tiers, Color defaultColor)
	{
		for (ValueTier tier : tiers)
		{
			if (value >= tier.threshold)
			{
				return tier.color;
			}
		}
		return defaultColor;
	}
}
