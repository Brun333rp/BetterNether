package paulevs.betternether.items;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import paulevs.betternether.registry.NetherItems;

public class BNSword extends SwordItem {
	public BNSword(Tier material, int durability, int attackDamage, float attackSpeed) {
		super(material, attackDamage, attackSpeed, NetherItems.defaultSettings().fireResistant());
	}
}
