package br.com.gamemods.forge.idtransition;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemTransition extends Item {

	public ReplacementConfig defaultReplacement;
	public Map<Integer, ReplacementConfig> metaReplacement;

	public ItemTransition(int id) {
		super(id);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
	}
}
