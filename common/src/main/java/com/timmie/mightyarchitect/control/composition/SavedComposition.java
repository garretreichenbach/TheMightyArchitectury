package com.timmie.mightyarchitect.control.composition;

import com.timmie.mightyarchitect.control.compose.Cuboid;
import com.timmie.mightyarchitect.control.compose.GroundPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SavedComposition {

	public final UUID id;
	public String name;
	public final BlockPos anchor;
	public final Cuboid bounds;
	public final CompoundTag groundPlanNbt;
	public final Map<BlockPos, BlockState> blocks;

	public SavedComposition(UUID id, String name, BlockPos anchor, Cuboid bounds,
			CompoundTag groundPlanNbt, Map<BlockPos, BlockState> blocks) {
		this.id = id;
		this.name = name;
		this.anchor = anchor;
		this.bounds = bounds;
		this.groundPlanNbt = groundPlanNbt;
		this.blocks = blocks;
	}

	public CompoundTag writeToNbt(CompoundTag tag) {
		tag.putUUID("id", id);
		tag.putString("name", name);
		tag.putLong("anchor", anchor.asLong());
		tag.put("bounds", bounds.writeToNbt(new CompoundTag()));
		tag.put("plan", groundPlanNbt);

		ListTag posList = new ListTag();
		ListTag stateList = new ListTag();
		for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) {
			posList.add(net.minecraft.nbt.LongTag.valueOf(e.getKey().asLong()));
			stateList.add(NbtUtils.writeBlockState(e.getValue()));
		}
		tag.put("pos", posList);
		tag.put("state", stateList);
		return tag;
	}

	public static SavedComposition readFromNbt(CompoundTag tag, Level level) {
		UUID id = tag.getUUID("id");
		String name = tag.getString("name");
		BlockPos anchor = BlockPos.of(tag.getLong("anchor"));
		Cuboid bounds = Cuboid.readFromNbt(tag.getCompound("bounds"));
		CompoundTag plan = tag.getCompound("plan");

		HolderLookup<net.minecraft.world.level.block.Block> lookup = level.holderLookup(Registries.BLOCK);
		Map<BlockPos, BlockState> blocks = new HashMap<>();
		ListTag posList = tag.getList("pos", Tag.TAG_LONG);
		ListTag stateList = tag.getList("state", Tag.TAG_COMPOUND);
		int n = Math.min(posList.size(), stateList.size());
		for (int i = 0; i < n; i++) {
			BlockPos p = BlockPos.of(((net.minecraft.nbt.LongTag) posList.get(i)).getAsLong());
			BlockState s;
			try {
				s = NbtUtils.readBlockState(lookup, stateList.getCompound(i));
			} catch (Exception ex) {
				s = Blocks.AIR.defaultBlockState();
			}
			blocks.put(p, s);
		}

		return new SavedComposition(id, name, anchor, bounds, plan, blocks);
	}

	public GroundPlan loadGroundPlan() {
		return GroundPlan.readFromNbt(groundPlanNbt);
	}
}
