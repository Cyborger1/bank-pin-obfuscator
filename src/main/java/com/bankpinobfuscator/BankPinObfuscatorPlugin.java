package com.bankpinobfuscator;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	// TEST MAP
	private Map<Integer, String>[] MAPS =
		new ImmutableMap[]{
			ImmutableMap.<Integer, String>builder()
				.put(0, "A0")
				.put(1, "A1")
				.put(2, "A2")
				.put(3, "A3")
				.put(4, "A4")
				.put(5, "A5")
				.put(6, "A6")
				.put(7, "A7")
				.put(8, "ITEM{10}")
				.put(9, "ITEM{5318:10}")
				.build(),
			ImmutableMap.<Integer, String>builder()
				.put(0, "B0")
				.put(1, "B1")
				.put(2, "B2")
				.put(3, "B3")
				.put(4, "B4")
				.put(5, "B5")
				.put(6, "B6")
				.put(7, "B7")
				.put(8, "B8")
				.put(9, "B9")
				.build(),
			ImmutableMap.<Integer, String>builder()
				.put(0, "C0")
				.put(1, "C1")
				.put(2, "C2")
				.put(3, "C3")
				.put(4, "C4")
				.put(5, "C5")
				.put(6, "C6")
				.put(7, "C7")
				.put(8, "C8")
				.put(9, "C9")
				.build(),
			ImmutableMap.<Integer, String>builder()
				.put(0, "C0")
				.put(1, "C1")
				.put(2, "C2")
				.put(3, "C3")
				.put(4, "C4")
				.put(5, "C5")
				.put(6, "C6")
				.put(7, "C7")
				.put(8, "C8")
				.put(9, "C9")
				.build()
		};

	private Pattern PATTERN = Pattern.compile("ITEM\\{(\\d+)(?::(\\d+))?}");

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (event.getEventName().equals("bankpinButtonSetup"))
		{
			int[] intStack = client.getIntStack();
			int intStackSize = client.getIntStackSize();

			final int compId = intStack[intStackSize - 2];
			final int buttonId = intStack[intStackSize - 1];

			// Since this is callback happens immediately after cc_setonop in the rs2asm,
			// it just so happens that the value immediately after the stack pointer is still
			// the current PIN step identifier, or the third operand of the aforementioned cc_setonop...
			// May god have mercy on my soul for this transgression.
			final int pinStep = intStack[intStackSize];

			if (pinStep < 0 || pinStep > 3)
			{
				return;
			}

			Widget button = client.getWidget(compId);

			if (button == null)
			{
				return;
			}

			Widget buttonRect = button.getChild(0);
			Widget buttonText = button.getChild(1);

			// Replace the timer on the button
			int tickToSetText = client.getGameCycle() + 5;
			buttonText.setOriginalX(0);
			buttonText.setXPositionMode(1);
			buttonText.setOriginalY(0);
			buttonText.setYPositionMode(1);
			buttonText.revalidate();
			buttonText.setOnTimerListener((JavaScriptCallback) e ->
			{
				if (client.getGameCycle() >= tickToSetText)
				{
					String newText = MAPS[pinStep].get(buttonId);
					int id = -1;
					int quant = -1;

					Matcher m = PATTERN.matcher(newText);
					if (m.matches())
					{
						try
						{
							id = Integer.parseInt(m.group(1));
							if (m.group(2) != null)
							{
								quant = Integer.parseInt(m.group(2));
							}
						}
						catch (Exception exp)
						{
							log.debug("Invalid Item/Quant: '" + newText + "'.");
						}
					}

					// Remove listener
					buttonText.setOnTimerListener((Object[]) null);
					buttonText.setText(newText);

					if (id > -1)
					{
						buttonText.setItemId(id);
						buttonText.setItemQuantity(quant);
						buttonText.setType(5);
						buttonText.setOriginalWidth(36);
						buttonText.setOriginalHeight(32);
					}
					else
					{
						buttonText.setType(4);
						buttonText.setOriginalWidth(buttonRect.getWidth());
						buttonText.setOriginalHeight(buttonRect.getHeight());
					}
					buttonText.revalidate();
				}
			});
		}
	}
}
