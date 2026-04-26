package com.timmie.mightyarchitect.control.composition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.timmie.mightyarchitect.control.Schematic;
import com.timmie.mightyarchitect.control.SchematicRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CompositionLibrary {

	private static final List<Entry> entries = new ArrayList<>();

	public static List<Entry> getEntries() {
		return entries;
	}

	public static Entry findById(UUID id) {
		for (Entry e : entries)
			if (e.composition.id.equals(id))
				return e;
		return null;
	}

	public static void add(SavedComposition composition) {
		Entry existing = findById(composition.id);
		if (existing != null)
			entries.remove(existing);
		entries.add(new Entry(composition));
		persist();
	}

	public static void remove(UUID id) {
		Iterator<Entry> it = entries.iterator();
		while (it.hasNext()) {
			Entry e = it.next();
			if (e.composition.id.equals(id)) {
				it.remove();
				persist();
				return;
			}
		}
	}

	public static void rename(UUID id, String newName) {
		Entry e = findById(id);
		if (e == null)
			return;
		e.composition.name = newName;
		persist();
	}

	public static void loadFromDisk() {
		entries.clear();
		for (SavedComposition c : CompositionStorage.load())
			entries.add(new Entry(c));
	}

	public static void clearMemory() {
		entries.clear();
	}

	public static void persist() {
		List<SavedComposition> out = new ArrayList<>(entries.size());
		for (Entry e : entries)
			out.add(e.composition);
		CompositionStorage.save(out);
	}

	public static void renderAllVisible(PoseStack ms, MultiBufferSource buffer) {
		for (Entry e : entries) {
			if (!e.visible)
				continue;
			e.ensureRenderer().render(ms, buffer);
		}
	}

	public static void tick() {
		for (Entry e : entries)
			if (e.visible && e.renderer != null)
				e.renderer.tick();
	}

	public static class Entry {
		public final SavedComposition composition;
		public boolean visible;
		private SchematicRenderer renderer;

		public Entry(SavedComposition composition) {
			this.composition = composition;
			this.visible = false;
		}

		public SchematicRenderer ensureRenderer() {
			if (renderer == null) {
				renderer = new SchematicRenderer();
				Schematic view = Schematic.fromMaterialized(composition.anchor, composition.bounds, composition.blocks);
				renderer.display(view);
			}
			return renderer;
		}

		public void invalidateRenderer() {
			renderer = null;
		}
	}
}
