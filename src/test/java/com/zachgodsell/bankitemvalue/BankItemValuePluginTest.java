package com.zachgodsell.bankitemvalue;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankItemValuePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankItemValuePlugin.class);
		RuneLite.main(args);
	}
}
