package com.zachgodsell.bankitemvalue;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Bank Item Value",
	description = "Overlays bank items with their value and colour-coded highlighting",
	tags = {"bank", "value", "price", "overlay", "ge", "alch"}
)
public class BankItemValuePlugin extends Plugin
{
	private static final int SHARED_BANK_GROUP = InterfaceID.SharedBank.ITEMS >>> 16;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BankItemValueOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		overlay.updateConfig();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlay.clearCache();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(BankItemValueConfig.GROUP))
		{
			return;
		}

		overlay.updateConfig();
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN || event.getGroupId() == SHARED_BANK_GROUP)
		{
			overlay.clearCache();
		}
	}

	@Provides
	BankItemValueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankItemValueConfig.class);
	}
}
