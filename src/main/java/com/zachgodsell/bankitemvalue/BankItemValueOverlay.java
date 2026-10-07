package com.zachgodsell.bankitemvalue;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.QuantityFormatter;

@Singleton
class BankItemValueOverlay extends WidgetItemOverlay
{
	private static final CacheEntry HIDDEN = new CacheEntry(0, null, null, null);

	private final ItemManager itemManager;
	private final BankItemValueConfig config;

	private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();

	// Config resolved into fields on start up and on config change; the render
	// path never reads the config proxy.
	private volatile boolean showValueText;
	private volatile PriceSource priceSource;
	private volatile ValueFormat valueFormat;
	private volatile long hideUnderValue;
	private volatile boolean applyToInventory;
	private volatile TextPosition textPosition;
	private volatile boolean textShadow;
	private volatile boolean useSmallFont;
	private volatile HighlightMode highlightMode;
	private volatile int fillOpacity;
	private volatile Color defaultColor;
	private volatile List<ValueTier> tiers = new ArrayList<>();

	@Inject
	private BankItemValueOverlay(ItemManager itemManager, BankItemValueConfig config)
	{
		this.itemManager = itemManager;
		this.config = config;
		showOnBank();
		showOnInterfaces(InterfaceID.BANKSIDE);
		updateConfig();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (highlightMode == HighlightMode.NONE)
		{
			return;
		}

		// A bank slot with quantity 0 is a placeholder
		final int quantity = widgetItem.getQuantity();
		if (quantity <= 0)
		{
			return;
		}

		final int interfaceId = widgetItem.getWidget().getId() >>> 16;
		if (interfaceId == InterfaceID.BANKSIDE && !applyToInventory)
		{
			return;
		}

		final long key = ((long) itemId << 32) | (quantity & 0xffffffffL);
		CacheEntry entry = cache.get(key);
		if (entry == null)
		{
			entry = computeEntry(itemId, quantity);
			cache.put(key, entry);
		}

		if (entry == HIDDEN)
		{
			return;
		}

		final Rectangle bounds = widgetItem.getCanvasBounds();
		final Font originalFont = graphics.getFont();
		final Color originalColor = graphics.getColor();
		try
		{
			if (highlightMode == HighlightMode.BOX_FILL)
			{
				graphics.setColor(entry.fillColor);
				graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
				return;
			}

			if (highlightMode == HighlightMode.BOX_OUTLINE || highlightMode == HighlightMode.OUTLINE_AND_TEXT)
			{
				graphics.setColor(entry.color);
				graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
			}

			final boolean drawText = showValueText
				&& (highlightMode == HighlightMode.TEXT_ONLY || highlightMode == HighlightMode.OUTLINE_AND_TEXT);
			if (!drawText)
			{
				return;
			}

			graphics.setFont(useSmallFont ? FontManager.getRunescapeSmallFont() : FontManager.getRunescapeFont());

			final FontMetrics metrics = graphics.getFontMetrics();
			final int textWidth = metrics.stringWidth(entry.text);
			final int textX;
			final int textY;
			switch (textPosition)
			{
				case TOP_LEFT:
					textX = bounds.x;
					textY = bounds.y + metrics.getAscent();
					break;
				case TOP_RIGHT:
					textX = bounds.x + bounds.width - textWidth;
					textY = bounds.y + metrics.getAscent();
					break;
				case BOTTOM_RIGHT:
					textX = bounds.x + bounds.width - textWidth;
					textY = bounds.y + bounds.height - 1;
					break;
				case CENTER:
					textX = bounds.x + (bounds.width - textWidth) / 2;
					textY = bounds.y + (bounds.height + metrics.getAscent()) / 2;
					break;
				case BOTTOM_LEFT:
				default:
					textX = bounds.x;
					textY = bounds.y + bounds.height - 1;
					break;
			}

			if (textShadow)
			{
				graphics.setColor(Color.BLACK);
				graphics.drawString(entry.text, textX + 1, textY + 1);
			}
			graphics.setColor(entry.color);
			graphics.drawString(entry.text, textX, textY);
		}
		finally
		{
			graphics.setFont(originalFont);
			graphics.setColor(originalColor);
		}
	}

	private CacheEntry computeEntry(int itemId, int quantity)
	{
		final long unitPrice = unitPrice(itemId);
		if (unitPrice <= 0)
		{
			return HIDDEN;
		}

		final long value = unitPrice * quantity;
		if (value < hideUnderValue)
		{
			return HIDDEN;
		}

		final Color color = ValueTier.resolve(value, tiers, defaultColor);
		final Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), fillOpacity);
		final String text = formatValue(value, valueFormat);
		return new CacheEntry(value, text, color, fillColor);
	}

	private long unitPrice(int itemId)
	{
		// getItemPrice resolves noted items to their unnoted counterpart itself
		final long gePrice = priceSource == PriceSource.HIGH_ALCHEMY ? 0 : itemManager.getItemPrice(itemId);

		int haPrice = 0;
		if (priceSource != PriceSource.GRAND_EXCHANGE)
		{
			net.runelite.api.ItemComposition composition = itemManager.getItemComposition(itemId);
			if (composition.getNote() != -1)
			{
				composition = itemManager.getItemComposition(composition.getLinkedNoteId());
			}
			haPrice = composition.getHaPrice();
		}

		return Math.max(gePrice, haPrice);
	}

	static String formatValue(long value, ValueFormat format)
	{
		switch (format)
		{
			case EXACT:
				return QuantityFormatter.formatNumber(value);
			case SHORT:
			{
				final String text = QuantityFormatter.quantityToStackSize(value);
				final int dot = text.indexOf('.');
				if (dot < 0)
				{
					return text;
				}
				final char suffix = text.charAt(text.length() - 1);
				return Character.isLetter(suffix)
					? text.substring(0, dot) + suffix
					: text.substring(0, dot);
			}
			case DECIMAL:
			default:
				return QuantityFormatter.quantityToStackSize(value);
		}
	}

	void updateConfig()
	{
		showValueText = config.showValueText();
		priceSource = config.priceSource();
		valueFormat = config.valueFormat();
		hideUnderValue = config.hideUnderValue();
		applyToInventory = config.applyToInventory();
		textPosition = config.textPosition();
		textShadow = config.textShadow();
		useSmallFont = config.useSmallFont();
		highlightMode = config.highlightMode();
		fillOpacity = config.fillOpacity();
		defaultColor = config.defaultColor();

		final List<ValueTier> newTiers = new ArrayList<>();
		if (config.lowEnabled())
		{
			newTiers.add(new ValueTier(config.lowThreshold(), config.lowColor()));
		}
		if (config.mediumEnabled())
		{
			newTiers.add(new ValueTier(config.mediumThreshold(), config.mediumColor()));
		}
		if (config.highEnabled())
		{
			newTiers.add(new ValueTier(config.highThreshold(), config.highColor()));
		}
		if (config.insaneEnabled())
		{
			newTiers.add(new ValueTier(config.insaneThreshold(), config.insaneColor()));
		}
		newTiers.sort(Comparator.comparingLong(tier -> -tier.threshold));
		tiers = newTiers;

		cache.clear();
	}

	void clearCache()
	{
		cache.clear();
	}

	private static final class CacheEntry
	{
		private final long value;
		private final String text;
		private final Color color;
		private final Color fillColor;

		private CacheEntry(long value, String text, Color color, Color fillColor)
		{
			this.value = value;
			this.text = text;
			this.color = color;
			this.fillColor = fillColor;
		}
	}
}
