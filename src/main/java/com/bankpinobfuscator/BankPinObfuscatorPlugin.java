package com.bankpinobfuscator;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
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
	private ClientThread clientThread;

	@Inject
	private BankPinObfuscatorConfig config;

	@Provides
	BankPinObfuscatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankPinObfuscatorConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	/*
	https://github.com/runelite/runelite/blob/1fb85dfbb9e79ebd73792df97562dd4939f8e9bb/runelite-client/src/main/java/net/runelite/client/plugins/bank/BankPlugin.java
	 */

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		int[] intStack = client.getIntStack();
		int intStackSize = client.getIntStackSize();

		if (event.getEventName().equals("bankpinButtonSetup"))
		{
			final int compId = intStack[intStackSize - 2];
			final int buttonId = intStack[intStackSize - 1];
			Widget button = client.getWidget(compId);

			if (button == null)
			{
				return;
			}

			Widget buttonRect = button.getChild(0);
			Widget buttonText = button.getChild(1);

			// Replace the timer on the button
			int tickToSetText = client.getGameCycle() + 5;
			buttonText.setOriginalX(buttonRect.getWidth() / 2 - (buttonText.getWidth() / 2));
			buttonText.setOriginalY(buttonRect.getHeight() / 2 - (buttonText.getHeight() / 2));
			buttonText.revalidate();
			buttonText.setOnTimerListener((JavaScriptCallback) e ->
				clientThread.invokeLater(() ->
				{
					if (client.getGameCycle() >= tickToSetText)
					{
						// Remove listener
						buttonText.setOnTimerListener((Object[]) null);

						buttonText.setText("a " + buttonId);
					}
				}));
		}
	}
}
