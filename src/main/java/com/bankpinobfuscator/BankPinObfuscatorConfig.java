package com.bankpinobfuscator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankpinobfuscator")
public interface BankPinObfuscatorConfig extends Config
{
	@ConfigItem(
		keyName = "overwriteDefaultMapOnLaunch",
		name = "Overwrite Map File on Launch",
		description = "If the map file fails to load, overwrites the default map on plugin launch."
	)
	default boolean overwriteDefaultMapOnLaunch()
	{
		return false;
	}
}
