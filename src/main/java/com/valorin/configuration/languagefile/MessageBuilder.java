package com.valorin.configuration.languagefile;

import static com.valorin.Main.getInstance;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.valorin.Main;
import com.valorin.configuration.ConfigManager;

public class MessageBuilder {

	/*
	 * Chinese:中文消息，必须 player:玩家，必须 v:变量，非必须，默认无 vl：变量的表示，非必须，默认无 prefix:是否前缀，必须，默认无
	 */
	public static String gmLog(String Chinese, Player player, String v, String[] vl,
			boolean prefix, boolean hdTitle) // 获取一个语言项
	{
		if (Chinese.length() == 0) {
			return "";
		}
		LanguageFileLoader languageFileLoader = getInstance().getLanguageFileLoader();
		Map<File, List<String>> lang = languageFileLoader.getLang();
		List<File> languagesList = languageFileLoader.getLanguagesList();
		List<String> defaultLang = languageFileLoader.getDefaultLang();

		if (defaultLang.contains(Chinese)) {// 这条中文属于默认消息，通过验证
			String finalMessage = ""; // 最终要输出的消息
			int number = 0;
			for (int i = 0; i < defaultLang.size(); i++) {
				if (defaultLang.get(i).equals(Chinese)) {
					number = i;
				}
			}
			if (player == null) {
				ConfigManager configManager = getInstance().getConfigManager();
				boolean exist = false;
				if (configManager.getDefaultLanguage() != null) {
					for (File f : languagesList) {
						if (f.getName().replace(".txt", "")
								.equals(configManager.getDefaultLanguage())) {
							exist = true;
							finalMessage = lang.get(f).get(number);
						}
					}
				}
				if (!exist) {
					finalMessage = defaultLang.get(number);
				}
			} else {
				String plang = getInstance().getCacheHandler()
						.getLanguageFile().get(player.getName());
				if (plang == null) {
					String configDefaultLang = getInstance().getConfigManager()
							.getDefaultLanguage();
					boolean exist = false;
					if (configDefaultLang != null) {
						for (File f : languagesList) {
							if (f.getName().replace(".txt", "")
									.equals(configDefaultLang)) {
								exist = true;
								finalMessage = lang.get(f).get(number);
							}
						}
					}
					if (!exist) {
						finalMessage = defaultLang.get(number);
					}
				} else {
					boolean isDefaultLang = true;
					if (languagesList != null) {
						for (File f : languagesList) {
							if (f.getName().split("\\.")[0].equals(plang)) {
								if (number > lang.get(f).size() - 1) {
									return "";
								}
								finalMessage = lang.get(f).get(number);
								isDefaultLang = false;
								break;
							}
						}
					}
					if (isDefaultLang) {
						finalMessage = defaultLang.get(number);
					}
				}
			}
			// 终端处理
			if (vl != null) {
				String[] vll = v.split("\\ ");
				for (int i = 0; i < vll.length; i++) {
					finalMessage = finalMessage.replace("{" + vll[i] + "}",
							vl[i]);
				}
			}
			finalMessage = finalMessage.replace("&", "§");
			if (player != null) {
				finalMessage = SymbolsExecutor.execute(finalMessage);// 替换符号
			} else {
				if (hdTitle) {
					finalMessage = SymbolsExecutor.execute(finalMessage);// 替换符号
				} else {
					List<String> symbolsMark = Main.getInstance()
							.getSymbolLoader().getSymbolsMark();
					for (String s : symbolsMark) {
						finalMessage = finalMessage.replace(s, "");
					}
				}
			}
			if (prefix) {
				finalMessage = Main.getInstance().getConfigManager()
						.getPrefix()
						+ finalMessage;
			}
			return finalMessage;
		} else {
			return Chinese+"§8Error:This message hasn't registered!(消息未注册，可能是编码问题，请联系腐竹建议TA为语言文件转码)";
		}
	}


	public static String getRankingString(int rank, List<String> dataList,
									boolean isWin) {
		if (rank + 1 <= dataList.size()) {
			String playerName = dataList.get(rank).split("\\|")[0];
			BigDecimal bg = BigDecimal.valueOf(Double.parseDouble(dataList.get(rank)
					.split("\\|")[1]));
			double value = bg.setScale(1, BigDecimal.ROUND_HALF_UP)
					.doubleValue();

			switch (rank) {
				case 0:
					return isWin ? gmLog("&b&l[n1] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b&l[n1] &f{player} &7[right] &a{value}",
							null, "player value", new String[]{playerName,
									"" + value}, false, true);
				case 1:
					return isWin ? gmLog("&e&l[n2] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&e&l[n2] &f{player} &7[right] &a{value}",
							null, "player value", new String[]{playerName,
									"" + value}, false, true);
				case 2:
					return isWin ? gmLog("&6&l[n3] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&6&l[n3] &f{player} &7[right] &a{value}",
							null, "player value", new String[]{playerName,
									"" + value}, false, true);
				case 3:
					return isWin ? gmLog("&b[n4] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n4] &f{player} &7[right] &a{value}", null,
							"player value", new String[]{playerName,
									"" + value}, false, true);
				case 4:
					return isWin ? gmLog("&b[n5] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n5] &f{player} &7[right] &a{value}", null,
							"player value", new String[]{playerName,
									"" + value}, false, true);
				case 5:
					return isWin ? gmLog("&b[n6] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n6] &f{player} &7[right] &a{value}", null,
							"player value", new String[]{playerName,
									"" + value}, false, true);
				case 6:
					return isWin ? gmLog("&b[n7] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n7] &f{player} &7[right] &a{value}", null,
							"player value", new String[]{playerName,
									"" + value}, false, true);
				case 7:
					return isWin ? gmLog("&b[n8] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n8] &f{player} &7[right] &a{value}", null,
							"player value", new String[]{playerName,
									"" + value}, false, true);
				case 8:
					return isWin ? gmLog("&b[n9] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n9] &f{player} &7[right] &a{value}", null,
							"player value", new String[]{playerName,
									"" + value}, false, true);
				case 9:
					return isWin ? gmLog("&b[n10] &f{player} &7[right] &a{value}场",
							null, "player value", new String[]{playerName,
									"" + (int) value}, false, true)
							: gmLog("&b[n10] &f{player} &7[right] &a{value}",
							null, "player value", new String[]{playerName,
									"" + value}, false, true);
			}
		}
		return null;
	}
}
