package com.zachgodsell.bankitemvalue;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(BankItemValueConfig.GROUP)
public interface BankItemValueConfig extends Config
{
	String GROUP = "bankitemvalue";

	@ConfigSection(
		name = "General",
		description = "Price source, formatting and where the overlay applies",
		position = 0
	)
	String generalSection = "general";

	@ConfigSection(
		name = "Display",
		description = "How the value text is drawn on each item",
		position = 1
	)
	String displaySection = "display";

	@ConfigSection(
		name = "Highlighting",
		description = "How items are highlighted with their tier colour",
		position = 2
	)
	String highlightingSection = "highlighting";

	@ConfigSection(
		name = "Value tiers",
		description = "Value thresholds and the colour used for each tier",
		position = 3
	)
	String tiersSection = "tiers";

	// General

	@ConfigItem(
		keyName = "showValueText",
		name = "Show value text",
		description = "Draw the item's total value as text on the item",
		position = 0,
		section = generalSection
	)
	default boolean showValueText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "priceSource",
		name = "Price source",
		description = "Which price is used per item: Grand Exchange price, high alchemy value, or the highest of the two",
		position = 1,
		section = generalSection
	)
	default PriceSource priceSource()
	{
		return PriceSource.GRAND_EXCHANGE;
	}

	@ConfigItem(
		keyName = "valueFormat",
		name = "Value format",
		description = "How values are written: Short (73K), Decimal (73.5K) or Exact (73,542)",
		position = 2,
		section = generalSection
	)
	default ValueFormat valueFormat()
	{
		return ValueFormat.DECIMAL;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "hideUnderValue",
		name = "Hide under value",
		description = "Items whose total value is below this amount show no overlay at all. 0 shows everything",
		position = 3,
		section = generalSection
	)
	default int hideUnderValue()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "applyToInventory",
		name = "Apply to inventory",
		description = "Also overlay the inventory side panel while the bank is open",
		position = 4,
		section = generalSection
	)
	default boolean applyToInventory()
	{
		return false;
	}

	// Display

	@ConfigItem(
		keyName = "textPosition",
		name = "Text position",
		description = "Corner of the item where the value text is drawn. The game draws stack counts at the top left",
		position = 0,
		section = displaySection
	)
	default TextPosition textPosition()
	{
		return TextPosition.BOTTOM_LEFT;
	}

	@ConfigItem(
		keyName = "textShadow",
		name = "Text shadow",
		description = "Draw a one-pixel shadow under the value text for legibility",
		position = 1,
		section = displaySection
	)
	default boolean textShadow()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useSmallFont",
		name = "Use small font",
		description = "Use the small Runescape font so the text fits inside a bank item cell",
		position = 2,
		section = displaySection
	)
	default boolean useSmallFont()
	{
		return true;
	}

	// Highlighting

	@ConfigItem(
		keyName = "highlightMode",
		name = "Highlight mode",
		description = "How each item shows its tier colour: coloured text, a box outline, a filled box, outline plus text, or nothing",
		position = 0,
		section = highlightingSection
	)
	default HighlightMode highlightMode()
	{
		return HighlightMode.TEXT_ONLY;
	}

	@Range(min = 0, max = 255)
	@ConfigItem(
		keyName = "fillOpacity",
		name = "Fill opacity",
		description = "Opacity of the filled box in Box fill mode. 0 is invisible, 255 is solid",
		position = 1,
		section = highlightingSection
	)
	default int fillOpacity()
	{
		return 40;
	}

	// Value tiers

	@Alpha
	@ConfigItem(
		keyName = "defaultColor",
		name = "Default colour",
		description = "Colour for items below every enabled tier threshold",
		position = 0,
		section = tiersSection
	)
	default Color defaultColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "lowEnabled",
		name = "Low tier",
		description = "Enable the low value tier",
		position = 1,
		section = tiersSection
	)
	default boolean lowEnabled()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "lowThreshold",
		name = "Low threshold",
		description = "Items worth at least this amount use the low tier colour",
		position = 2,
		section = tiersSection
	)
	default int lowThreshold()
	{
		return 20_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "lowColor",
		name = "Low colour",
		description = "Colour for items in the low value tier",
		position = 3,
		section = tiersSection
	)
	default Color lowColor()
	{
		return new Color(0x66B2FF);
	}

	@ConfigItem(
		keyName = "mediumEnabled",
		name = "Medium tier",
		description = "Enable the medium value tier",
		position = 4,
		section = tiersSection
	)
	default boolean mediumEnabled()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "mediumThreshold",
		name = "Medium threshold",
		description = "Items worth at least this amount use the medium tier colour",
		position = 5,
		section = tiersSection
	)
	default int mediumThreshold()
	{
		return 100_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "mediumColor",
		name = "Medium colour",
		description = "Colour for items in the medium value tier",
		position = 6,
		section = tiersSection
	)
	default Color mediumColor()
	{
		return new Color(0x99FF99);
	}

	@ConfigItem(
		keyName = "highEnabled",
		name = "High tier",
		description = "Enable the high value tier",
		position = 7,
		section = tiersSection
	)
	default boolean highEnabled()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "highThreshold",
		name = "High threshold",
		description = "Items worth at least this amount use the high tier colour",
		position = 8,
		section = tiersSection
	)
	default int highThreshold()
	{
		return 1_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "highColor",
		name = "High colour",
		description = "Colour for items in the high value tier",
		position = 9,
		section = tiersSection
	)
	default Color highColor()
	{
		return new Color(0xFF9600);
	}

	@ConfigItem(
		keyName = "insaneEnabled",
		name = "Insane tier",
		description = "Enable the insane value tier",
		position = 10,
		section = tiersSection
	)
	default boolean insaneEnabled()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "insaneThreshold",
		name = "Insane threshold",
		description = "Items worth at least this amount use the insane tier colour",
		position = 11,
		section = tiersSection
	)
	default int insaneThreshold()
	{
		return 10_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "insaneColor",
		name = "Insane colour",
		description = "Colour for items in the insane value tier",
		position = 12,
		section = tiersSection
	)
	default Color insaneColor()
	{
		return new Color(0xFF66B2);
	}
}
