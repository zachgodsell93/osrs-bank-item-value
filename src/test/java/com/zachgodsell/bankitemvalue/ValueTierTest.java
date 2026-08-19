package com.zachgodsell.bankitemvalue;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ValueTierTest
{
	private static final Color DEFAULT = Color.WHITE;
	private static final Color LOW = new Color(0x66B2FF);
	private static final Color MEDIUM = new Color(0x99FF99);
	private static final Color HIGH = new Color(0xFF9600);
	private static final Color INSANE = new Color(0xFF66B2);

	private static final List<ValueTier> TIERS = Arrays.asList(
		new ValueTier(10_000_000, INSANE),
		new ValueTier(1_000_000, HIGH),
		new ValueTier(100_000, MEDIUM),
		new ValueTier(20_000, LOW)
	);

	@Test
	public void exactThresholdWinsTheTier()
	{
		assertEquals(HIGH, ValueTier.resolve(1_000_000, TIERS, DEFAULT));
		assertEquals(MEDIUM, ValueTier.resolve(100_000, TIERS, DEFAULT));
		assertEquals(LOW, ValueTier.resolve(20_000, TIERS, DEFAULT));
		assertEquals(INSANE, ValueTier.resolve(10_000_000, TIERS, DEFAULT));
	}

	@Test
	public void belowThresholdFallsThrough()
	{
		assertEquals(MEDIUM, ValueTier.resolve(999_999, TIERS, DEFAULT));
		assertEquals(LOW, ValueTier.resolve(99_999, TIERS, DEFAULT));
		assertEquals(DEFAULT, ValueTier.resolve(19_999, TIERS, DEFAULT));
	}

	@Test
	public void disabledTierFallsToNextTierDown()
	{
		final List<ValueTier> withoutHigh = Arrays.asList(
			new ValueTier(10_000_000, INSANE),
			new ValueTier(100_000, MEDIUM),
			new ValueTier(20_000, LOW)
		);
		assertEquals(MEDIUM, ValueTier.resolve(1_000_000, withoutHigh, DEFAULT));
	}

	@Test
	public void noTiersUsesDefault()
	{
		assertEquals(DEFAULT, ValueTier.resolve(50_000_000, Collections.emptyList(), DEFAULT));
	}

	@Test
	public void formatShort()
	{
		assertEquals("73K", BankItemValueOverlay.formatValue(73_542, ValueFormat.SHORT));
		assertEquals("1M", BankItemValueOverlay.formatValue(1_000_000, ValueFormat.SHORT));
		assertEquals("999", BankItemValueOverlay.formatValue(999, ValueFormat.SHORT));
	}

	@Test
	public void formatDecimal()
	{
		assertEquals("73.5K", BankItemValueOverlay.formatValue(73_542, ValueFormat.DECIMAL));
		assertEquals("1M", BankItemValueOverlay.formatValue(1_000_000, ValueFormat.DECIMAL));
		assertEquals("999", BankItemValueOverlay.formatValue(999, ValueFormat.DECIMAL));
	}

	@Test
	public void formatExact()
	{
		assertEquals("73,542", BankItemValueOverlay.formatValue(73_542, ValueFormat.EXACT));
		assertEquals("1,000,000", BankItemValueOverlay.formatValue(1_000_000, ValueFormat.EXACT));
		assertEquals("999", BankItemValueOverlay.formatValue(999, ValueFormat.EXACT));
	}
}
