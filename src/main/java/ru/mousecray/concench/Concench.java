package ru.mousecray.concench;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = Concench.ID, name = Concench.NAME, version = Concench.VER)
public class Concench {

	public static final HashMap<String, List<ConcenchObj>> enchantments = new HashMap<String, List<ConcenchObj>>();
	public static Random random = new Random();

	public static final String ID = "concench";
	public static final String NAME = "Conclusion Enchantments";
	public static final String VER = "1.0";

	public static Logger logger;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		logger.info("Sucessfully initialized mod");
	}

	@Mod.EventHandler
	public void serverStopping(@SuppressWarnings("unused") FMLServerStoppingEvent event) {
		logger.info("Unloading conclusion enchantments...");
		enchantments.clear();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		File world = event.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
		try {
			File modFile = new File(world, "concench");
			if (modFile.mkdir()) {
				logger.info("Creating conclusion enchantments...");
				Files.write(new File(modFile, "item.bone.txt").toPath(),
						new String("crit wow normal attack 50").getBytes(), StandardOpenOption.CREATE_NEW);
				loadEnch(modFile);
			} else
				loadEnch(modFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadEnch(File folder) throws IOException {
		logger.info("Loading conclusion enchantments...");
		for (File file : folder.listFiles()) {
			List<String> elements = Files.readAllLines(file.toPath());
			List<ConcenchObj> itemEnchants = new ArrayList<ConcenchObj>();
			int skip = 0;
			for (String currEnch : elements) {
				String[] enchPar = currEnch.split(" ");
				if (enchPar.length != 5)
					++skip;
				else {
					try {
						EnchType type = EnchType.fromString(enchPar[0]);
						String name = enchPar[1];
						EnchCategory category = EnchCategory.fromString(enchPar[2]);
						EnchAction action = EnchAction.fromString(enchPar[3]);
						String value = enchPar[4];

						if (type == null || category == null || action == null || value == null || value.equals(""))
							++skip;
						else
							itemEnchants.add(new ConcenchObj(type, name, category, action, value));

					} catch (@SuppressWarnings("unused") Exception e) {
						++skip;
					}
				}
				if (skip > 0)
					logger.warn("Skipping {} bad options in file <" + file.getName() + ">", skip);
			}
			enchantments.put(file.getName().substring(0, file.getName().length() - 4), itemEnchants);
		}
	}
}