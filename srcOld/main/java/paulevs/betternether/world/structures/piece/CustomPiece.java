package paulevs.betternether.world.structures.piece;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;

public abstract class CustomPiece extends StructurePiece {
	protected CustomPiece(StructurePieceType type, int i) {
		super(type, i);
	}

	protected CustomPiece(StructurePieceType type, CompoundTag tag) {
		super(type, tag);
	}
}