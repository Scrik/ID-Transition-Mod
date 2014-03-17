package br.com.gamemods.forge.idtransition;

import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;

public class ReplacementConfig {
	public boolean block = true;
	public int newId = 1;
	public int newMetadata = 0;
	public boolean keepMetadata = true;
	public boolean killTileEntity = false;
	public boolean replacePlacedBlocks = true;
	public int itemDropID = 0;
	public int itemDropMetadata = 0;
	public int itemBlockIDReplacement = 0;
	public int itemBlockMetadataReplacement = 0;
	public boolean itemBlockKeepMetadata = true;
	public boolean keepItemNBT = true;
	
	public void load(ConfigCategory category)
	{
		block = category.parent.get("IsBlockID").getBoolean(block);
		newId = category.get("NewID").getInt(newId);
		newMetadata = category.get("NewMetadata").getInt(newMetadata);
		keepMetadata = category.get("KeepMetadata").getBoolean(keepMetadata);
		replacePlacedBlocks = category.get("ReplacePlacedBlocks").getBoolean(replacePlacedBlocks);
		itemDropID = category.get("ItemDropID").getInt(itemDropID);
		itemDropMetadata = category.get("ItemDropMetadata").getInt(itemDropMetadata);
		itemBlockIDReplacement = category.get("ItemBlockIDReplacement").getInt(itemBlockIDReplacement);
		itemBlockMetadataReplacement = category.get("ItemBlockMetadataReplacement").getInt(itemBlockMetadataReplacement);
		itemBlockKeepMetadata = category.get("ItemBlockKeepMetadata").getBoolean(itemBlockKeepMetadata);
		keepItemNBT = category.get("KeepItemNBT").getBoolean(keepItemNBT);
	}
}
