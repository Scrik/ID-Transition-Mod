package br.com.gamemods.forge.idtransition;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemBlockTransition extends ItemBlock {

	public ItemBlockTransition(int par1) {
		super(par1);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int index, boolean currentItem) {
		if(!(entity instanceof EntityPlayer)) return;
		if(1==1) return;
		EntityPlayer player = (EntityPlayer) entity;
		
		Block b = Block.blocksList[getBlockID()];
		if(b == null || !(b instanceof BlockTransition))
			return;
		
		BlockTransition block = (BlockTransition) b;
		
		ReplacementConfig config = block.metaReplacement.get(stack.getItemDamage());
		if(config == null) config = block.defaultReplacement;
		
		int id = config.itemBlockIDReplacement;
		if(id <= 0) id = config.newId;
		
		int meta;
		
		if(config.itemBlockKeepMetadata)
		{
			meta = stack.getItemDamage();
		}
		else if(config.itemBlockMetadataReplacement > 0)
		{
			meta = config.itemBlockMetadataReplacement;
		}
		else if(config.keepMetadata)
		{
			meta = stack.getItemDamage();
		}
		else if(config.newMetadata > 0)
		{
			meta = config.newMetadata;
		}
		else
		{
			meta = stack.getItemDamage();
		}
		
		ItemStack newStack = new ItemStack(id, stack.stackSize, meta);
		
		if(config.keepItemNBT && stack.stackTagCompound != null)
		{
			newStack.stackTagCompound = (NBTTagCompound) stack.stackTagCompound.copy();
		}
		
		IdTransitionMod.log.info("Changing "+player.username+"'s inventory index "+index+" from "+stack.itemID+":"+stack.getItemDamage()+" to "+newStack.itemID+":"+newStack.getItemDamage()+". NBT data was"+(!config.keepItemNBT?" DELETED":(stack.stackTagCompound == null?" NULL":" CLONED")));
		player.inventory.setInventorySlotContents(index, newStack);
	}
}
