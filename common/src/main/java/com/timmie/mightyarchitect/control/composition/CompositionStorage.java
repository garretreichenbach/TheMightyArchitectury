package com.timmie.mightyarchitect.control.composition;

import com.timmie.mightyarchitect.TheMightyArchitect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CompositionStorage {

	private static final String FILE_NAME = "compositions.dat";

	public static Path resolveStoragePath() {
		Minecraft mc = Minecraft.getInstance();
		MinecraftServer integrated = mc.getSingleplayerServer();
		if (integrated != null) {
			Path worldRoot = integrated.getWorldPath(LevelResource.ROOT);
			return worldRoot.resolve(TheMightyArchitect.ID).resolve(FILE_NAME);
		}
		ServerData remote = mc.getCurrentServer();
		if (remote != null) {
			String key = sanitize(remote.ip);
			return Paths.get(TheMightyArchitect.ID, "servers", key, FILE_NAME);
		}
		return Paths.get(TheMightyArchitect.ID, "unknown", FILE_NAME);
	}

	private static String sanitize(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_')
				sb.append(c);
			else
				sb.append('_');
		}
		String out = sb.toString();
		return out.isEmpty() ? "unknown" : out;
	}

	public static List<SavedComposition> load() {
		Path path = resolveStoragePath();
		List<SavedComposition> result = new ArrayList<>();
		if (!Files.exists(path))
			return result;

		Level level = Minecraft.getInstance().level;
		if (level == null)
			return result;

		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			CompoundTag root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
			ListTag list = root.getList("entries", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				try {
					result.add(SavedComposition.readFromNbt(list.getCompound(i), level));
				} catch (Exception ex) {
					TheMightyArchitect.logger.warn("Failed to read composition entry " + i, ex);
				}
			}
		} catch (IOException ex) {
			TheMightyArchitect.logger.warn("Failed to load compositions from " + path, ex);
		}
		return result;
	}

	public static void save(List<SavedComposition> compositions) {
		Path path = resolveStoragePath();
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException ex) {
			TheMightyArchitect.logger.error("Failed to create composition directory " + path.getParent(), ex);
			return;
		}

		CompoundTag root = new CompoundTag();
		ListTag list = new ListTag();
		for (SavedComposition c : compositions)
			list.add(c.writeToNbt(new CompoundTag()));
		root.put("entries", list);

		try (OutputStream out = Files.newOutputStream(path,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			NbtIo.writeCompressed(root, out);
		} catch (IOException ex) {
			TheMightyArchitect.logger.error("Failed to write compositions to " + path, ex);
		}
	}
}
