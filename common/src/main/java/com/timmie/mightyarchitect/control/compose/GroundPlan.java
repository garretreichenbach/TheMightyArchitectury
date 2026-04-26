package com.timmie.mightyarchitect.control.compose;

import com.timmie.mightyarchitect.control.design.DesignTheme;
import com.timmie.mightyarchitect.control.design.ThemeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class GroundPlan {

	public static final int MAX_LAYERS = 5;

	public DesignTheme theme;
	private List<Stack> stacks;
	private List<Room> interior;
	
	public GroundPlan(DesignTheme theme) {
		this.theme = theme;
		stacks = new ArrayList<>();
		interior = new LinkedList<>();
	}

	public List<Room> getInterior() {
		interior.clear();
		forEachStack(stack -> {
			if (stack instanceof CylinderStack)
				return;
			 
			stack.forEach(room -> {
				interior.add(room.getInterior());				
			});
		});
		return interior;
	}

	public void addStack(Stack stack) {
		stacks.add(stack);
	}
	
	public Stack getStackAtPos(BlockPos localPos) {		
		for (Stack stack : stacks) {
			Room room = stack.getRoomAtPos(localPos);
			if (room != null)
				return stack;
		}
		return null;
	}
	
	public Room getRoomAtPos(BlockPos localPos) {
		for (Stack stack : stacks) {
			Room room = stack.getRoomAtPos(localPos);
			if (room != null)
				return room;
		}
		return null;
	}
	
	public boolean isEmpty() {
		return stacks.isEmpty();
	}
	
	public void remove(Stack stack) {
		stacks.remove(stack);
	}
	
	public void forEachStack(Consumer<? super Stack> action) {
		stacks.forEach(action);
	}
	
	public void forEachRoom(Consumer<? super Room> action) {
		stacks.forEach(stack -> stack.forEach(action));
	}

	public CompoundTag writeToNbt(CompoundTag tag) {
		tag.putString("theme", theme.getDisplayName());
		ListTag stackList = new ListTag();
		for (Stack stack : stacks)
			stackList.add(stack.writeToNbt(new CompoundTag()));
		tag.put("stacks", stackList);
		return tag;
	}

	public static GroundPlan readFromNbt(CompoundTag tag) {
		String themeName = tag.getString("theme");
		DesignTheme theme = null;
		for (DesignTheme candidate : ThemeStorage.getAllThemes()) {
			if (candidate.getDisplayName().equals(themeName)) {
				theme = candidate;
				break;
			}
		}
		if (theme == null)
			return null;
		GroundPlan plan = new GroundPlan(theme);
		ListTag stackList = tag.getList("stacks", Tag.TAG_COMPOUND);
		for (int i = 0; i < stackList.size(); i++) {
			Stack stack = Stack.readFromNbt(stackList.getCompound(i), theme);
			if (stack != null)
				plan.stacks.add(stack);
		}
		return plan;
	}

}
