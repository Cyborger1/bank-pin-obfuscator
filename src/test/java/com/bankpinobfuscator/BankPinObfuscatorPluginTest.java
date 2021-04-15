package com.bankpinobfuscator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankPinObfuscatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankPinObfuscatorPlugin.class);
		RuneLite.main(args);
	}
}