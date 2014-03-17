package br.com.gamemods.forge.idtransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

@Mod(modid="IdTransitionMod", version="1.0", name="ID Transtion")
public class IdTransitionMod {
	
	public static Logger log;
	private Configuration config;
	private Set<String> delayedTypes = new HashSet<String>();
	
	private Property getOrSet(ConfigCategory category, String name, String defaultVal, Type type)
	{
		Property prop = category.get(name);
		if(prop == null) prop = new Property(name, defaultVal, type);
		category.put(name, prop);
		return prop;
	}
	
	private void setDefaultValues(ConfigCategory category)
	{
		Property prop = getOrSet(category, "IsBlockID", "true", Type.BOOLEAN);
		prop.comment = "true if this is a BlockID, false if this is an ItemID";
	}
	
	private void setDefaultMetaValues(ConfigCategory category)
	{
		Property prop = getOrSet(category, "NewID", "1", Type.INTEGER);
		prop.comment = "The new Type ID";
		
		prop = getOrSet(category, "KeepMetadata", "true", Type.BOOLEAN);
		prop.comment = "Should I keep the original metadata or change it?";
		
		prop = getOrSet(category, "NewMetadata", "0", Type.INTEGER);
		prop.comment = "If KeepMetadata is false, what's the new MetaData?";
		
		prop = getOrSet(category, "KillTileEntity", "false", Type.BOOLEAN);
		prop.comment = "Should I kill TileEntities at the same location as the blocks?";
		
		prop = getOrSet(category, "ReplacePlacedBlocks", "true", Type.BOOLEAN);
		prop.comment = "Should I replace the placed blocks to the new ID? Note: The blocks will be changed in random ticks";
		
		prop = getOrSet(category, "ItemDropID", "0", Type.INTEGER);
		prop.comment = "What Item ID should be dropped when we break the block? 0 = the same as NewID";
		
		prop = getOrSet(category, "ItemBlockIDReplacement", "0", Type.INTEGER);
		prop.comment = "Valid only for Blocks. I'll convert blocks in item form to this ID. 0 = same as NewID";
		
		prop = getOrSet(category, "ItemBlockMetadataReplacement", "0", Type.INTEGER);
		prop.comment = "Valid only for Blocks. I'll change blocks in item form to this metadata. 0 = same as NewMetadata";
		
		prop = getOrSet(category, "ItemBlockKeepMetadata", "true", Type.BOOLEAN);
		prop.comment = "Valid only for Blocks. Should I keep the metadata of blocks in item form?";
		
		prop = getOrSet(category, "KeepItemNBT", "true", Type.BOOLEAN);
		prop.comment = "Should I keep the NBT data of items of this type?";
		
		prop = getOrSet(category, "ItemDropMetadata", "0", Type.INTEGER);
		prop.comment = "What Item Metadata should be dropped when we break the block? 0 = the same as NewMetadata";
		
		/*prop = getOrSet(category, "ItemDropMinQuantity", "1", Type.INTEGER);
		prop.comment = "What's the minimum ammont of items dropped when this block is broken?";
		
		prop = getOrSet(category, "ItemDropMaxQuantity", "1", Type.INTEGER);
		prop.comment = "What's the maximum ammont of items dropped when this block is broken?";
		
		prop = getOrSet(category, "BlockHardness", "1.5", Type.DOUBLE);
		prop.comment = "What's the hardness of the block? Higher values take more time to break";
		
		prop = getOrSet(category, "BlockResistance", "10.0", Type.DOUBLE);
		prop.comment = "What's the explosion resistance of this block?";*/
		
		category.put(prop.getName(), prop);
	}
	
	private void defineDefaultCategories(Configuration config)
	{
		ConfigCategory category = config.getCategory("model");
		category.setComment("This is a model category, copy and paste this category into a new root category, use the type id as category name");
		setDefaultValues(category);
		
		category = config.getCategory("model"+config.CATEGORY_SPLITTER+"default");
		category.setComment("This is the default behaviour for all metadatas in this type id, if you want an metadata specific behaviour copy this subcategory and change the name to the metadata number");
		setDefaultMetaValues(category);
		
		category = config.getCategory("17");
		category.setComment("This is an example of how we would convert Wood (block id 17) to Wool (block id 35). This example will be ignored.");
		setDefaultValues(category);
		
		category = config.getCategory("17"+config.CATEGORY_SPLITTER+"default");
		category.setComment("The default behavior is to will be to preserve metadata");
		setDefaultMetaValues(category);
		
		category = config.getCategory("17"+config.CATEGORY_SPLITTER+"1");
		category.setComment("But in this example Spruce Wood (17:1) would be converted into a Stone Bricks block (block id 98)");
		setDefaultMetaValues(category);
	}
	
	private void registerType(Configuration config, String categoryName)
	{
		log.info("Registering type "+categoryName);
		setDefaultValues(config.getCategory(categoryName));
		ConfigCategory defaultCategory = config.getCategory(categoryName+config.CATEGORY_SPLITTER+"default");
		setDefaultMetaValues(defaultCategory);
		
		boolean isBlock = config.get(categoryName, "IsBlockID", true).getBoolean(true);
		
		log.info("Loading default replacement for "+categoryName);
		ReplacementConfig defaultReplacement = new ReplacementConfig();
		defaultReplacement.load(defaultCategory);
		
		Map<Integer, ReplacementConfig> metaReplacement = new HashMap<Integer, ReplacementConfig>();
		
		boolean doRandomTicks = defaultReplacement.killTileEntity || defaultReplacement.replacePlacedBlocks;
		
		for(ConfigCategory category: defaultCategory.parent.getChildren())
		{
			String subcategoryName = category.getQualifiedName();
			if(!subcategoryName.matches("^[0-9]+$"))
				continue;
			
			log.info("Loading meta replacement "+categoryName+":"+subcategoryName);
			setDefaultMetaValues(category);
			ReplacementConfig replacement = new ReplacementConfig();
			replacement.load(category);
			metaReplacement.put(Integer.parseInt(subcategoryName), replacement);
			
			if(defaultReplacement.killTileEntity || defaultReplacement.replacePlacedBlocks)
				doRandomTicks = true;
		}
		
		int id = Integer.parseInt(categoryName);
		
		if(isBlock)
		{
			Block other = Block.blocksList[id];
			if(other != null)
			{
				UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(other);
				log.severe("[CONFLICT] Ignoring definition for "+categoryName+" because this block is already registered by "+other.getUnlocalizedName()+(uid != null?" UID "+uid.modId+":"+uid.name:""));
				return;
			}
			
			BlockTransition transition = new BlockTransition(id);
			transition.defaultReplacement = defaultReplacement;
			transition.metaReplacement = metaReplacement;
			
			if(doRandomTicks) transition.setTickRandomly(true);
			
			log.info("Registering Block ID "+categoryName);
			GameRegistry.registerBlock(transition, ItemBlockTransition.class, "transition"+transition.blockID);
		}
		else
		{
			Item other = Item.itemsList[id];
			if(other != null)
			{
				UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(other);
				log.severe("[CONFLICT] Ignoring definition for "+categoryName+" because this item is already registered by "+other.getUnlocalizedName()+(uid != null?" UID "+uid.modId+":"+uid.name:""));
				return;
			}
			
			ItemTransition transition = new ItemTransition(id);
			transition.defaultReplacement = defaultReplacement;
			transition.metaReplacement = metaReplacement;
			
			log.info("Registering Item ID "+categoryName);
			GameRegistry.registerItem(transition, "transition"+transition.itemID);
		}
	}
	
	private void defineBlockIds(Configuration config)
	{
		for(String categoryName: config.getCategoryNames())
		{
			if(categoryName.contains(config.CATEGORY_SPLITTER) || !categoryName.matches("^[0-9]+$")) continue;
			log.info("Loading definition for "+categoryName);
			
			if(config.get(categoryName, "IsBlockID", true).getBoolean(true))
			{
				registerType(config, categoryName);	
			}
		}
	}
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		log = event.getModLog();
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		log.info("Loading config file");
		config.load();
		
		log.info("Creating default categories");
		defineDefaultCategories(config);
		
		log.info("Loading definitions");
		defineBlockIds(config);

		config.save();
	}
}
