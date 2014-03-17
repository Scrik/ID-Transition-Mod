package br.com.gamemods.forge.idtransition;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;

public class BlockTransition extends Block {

	public ReplacementConfig defaultReplacement;
	public Map<Integer, ReplacementConfig> metaReplacement;
	
	public BlockTransition(int id) {
		super(id, Material.rock);
	}
	
	public void tick(World world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		ReplacementConfig config = metaReplacement.get(meta);
		if(config == null) config = defaultReplacement;
		
		if(config.killTileEntity)
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);
			if(tile != null)
			{
				IdTransitionMod.log.info("Invalidating TileEntity at "+x+","+y+","+z+" as configured for "+blockID+":"+meta);
				tile.invalidate();
			}
		}
		
		if(config.replacePlacedBlocks)
		{
			int newMeta = meta;
			if(!config.keepMetadata && config.newMetadata > 0)
				newMeta = config.newMetadata;
			
			Block newBlock = Block.blocksList[config.newId];
			if(newBlock == null)
			{
				IdTransitionMod.log.warning("Block "+blockID+":"+meta+" at "+x+","+y+","+z+" was NOT changed to "+config.newId+":"+newMeta+" because the new block was not found!");
				return;
			}
			
			IdTransitionMod.log.info("Block "+blockID+":"+meta+" at "+x+","+y+","+z+" was changed to "+config.newId+":"+newMeta);
			world.setBlock(x, y, z, config.newId, newMeta, 3);
		}
	}
	
	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		tick(world,x,y,z);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		tick(world,x,y,z);
		return false;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID) {
		tick(world,x,y,z);
	}
	
	@Override
	public void onNeighborTileChange(World world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		tick(world,x,y,z);
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random par5Random) {
		tick(world,x,y,z);
	}
}
