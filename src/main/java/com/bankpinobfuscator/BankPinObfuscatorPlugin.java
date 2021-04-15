package com.bankpinobfuscator;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class BankPinObfuscatorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BankPinObfuscatorConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
		// SCRIPT IS 683, widget child dynamic 1 on 16, 18, 20, etc
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	BankPinObfuscatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankPinObfuscatorConfig.class);
	}

	private boolean checkPINButtons;

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == 653)
		{
			checkPINButtons = true;
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (checkPINButtons)
		{
			Widget w = client.getWidget(WidgetInfo.BANK_PIN_CONTAINER.getGroupId(), 16);
			if (w == null)
			{
				return;
			}

			Widget w2 = w.getChild(1);

			if (w2 == null || w2.getText().equals(""))
			{
				return;
			}

			for (int i = 0; i < 10; i++)
			{
				Widget b = client.getWidget(WidgetInfo.BANK_PIN_CONTAINER.getGroupId(), 16 + i * 2);
				if (b != null)
				{
					Widget b2 = b.getChild(1);
					b2.setOnTimerListener();
					b2.setText("a " + b2.getText());
				}
			}

			checkPINButtons = false;
		}
	}
}
