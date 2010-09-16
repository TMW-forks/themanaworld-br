/*
 * TMWServ to eAthena Converter (c) 2008 Jared Adams
 * License: GPL, v2 or later
 */

package converter;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.TreeSet;

import tiled.core.*;
//import tiled.plugins.tmw.*;

public class Process {
    private static final String baseFolder = "server-data/";
    private static final File _baseFolder = new File(baseFolder);
    private static final String scriptDirectory = "npc/";
    private static final String mobFile = "_mobs.txt";
    private static final String warpFile = "_warps.txt";
    private static final String importFile = "_import.txt";

    private static WLKInterface wlk = null;

    public static void prepWLK(File folder) {
        try {
            wlk = new WLKInterface(folder);
        } catch (NoClassDefFoundError ncdfe) {}
    }

    private static String getProp(Properties props, String name, String def) {
        if (name == null) return def;
        for (java.util.Map.Entry<Object, Object> entry : props.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey().toString())) {
                return entry.getValue().toString();
            }
        }
        return def;
    }

    private static int getProp(Properties props, String name, int def) {
        if (name == null) return def;
        try {
            return Integer.parseInt(getProp(props, name, "?"));
        } catch (Exception e) {}
        return def;
    }

    private static int[] resolveBounds(Rectangle in, boolean warp) {
        int x = in.x / 32;
        int y = in.y / 32;
        int width = in.width / 32;
        int height = in.height / 32;
        if (!warp) {
            if (width > 1) --width;
            if (height > 1) --height;
        }
        x += width / 2;
        y += height / 2;
        if (warp) {
            width -= 2;
            height -= 2;
        }
        return new int[]{x, y, width, height};
    }

	private static void handleWarp(PrintWriter out, String map, String name, Rectangle bounds, Properties props) {
		if (out == null) return;
		String dest = getProp(props, "dest_map", null);
		if (dest == null) return;
		int x = getProp(props, "dest_x", -1);
		int x32 = getProp(props, "dest_x32", -1);
		if( x>=0 ) x /= 32;
		else if( x32>=0 ) x = x32;
		else return;
		int y = getProp(props, "dest_y", -1);
		int y32 = getProp(props, "dest_y32", -1);
		if( y>=0 ) y /= 32;
		else if( y32>=0 ) y = y32;
		else return;
		int[] shape = resolveBounds(bounds, true);
		System.out.printf("Usable warp found: %s\n", name);
		out.printf("%s.gat,%d,%d\twarp\t%s\t%d,%d,%s.gat,%d,%d\n", map, shape[0], shape[1], name, shape[2], shape[3], dest, x, y);
	}

	private static Mob handleMob(PrintWriter out, String map, String name, Rectangle bounds, Properties props) {
		if (out == null) return new Mob(-1);
		int mob = getProp(props, "monster_id", -1);
		if (mob < 0) return new Mob(-1);
		mob += 1002;
		int max = getProp(props, "max_beings", 1);
		int time1 = getProp(props, "eA_spawn", 0);
		int time2 = getProp(props, "eA_death", 0);
		int[] shape = resolveBounds(bounds, false);
		System.out.printf("Usable mob found: %s (%d)\n", name, mob);
		out.printf("%s.gat,%d,%d,%d,%d\tmonster\t%s\t%d,%d,%d,%d,Mob%s::On%d\n", map, shape[0], shape[1], shape[2], shape[3], name, mob, max, time1, time2, map, mob);

		Mob retMob = new Mob(mob);
		String script = getProp(props, "script", null);
		if(script!=null)
			retMob.getScripts().add(script);
		return retMob;
	}

    private static void processObject(MapObject mo, String map, PrintWriter warpOut, PrintWriter mobOut, TreeSet<Mob> mobs) {
        if (mo == null) return;
        String name = mo.getName();
        String type = mo.getType();
        Rectangle bounds = new Rectangle(mo.getBounds());
        Properties props = mo.getProperties();

        if (type.equalsIgnoreCase("warp")) {
            handleWarp(warpOut, map, name, bounds, props);
        } else if (type.equalsIgnoreCase("spawn")) {
            mobs.add(handleMob(mobOut, map, name, bounds, props));
        }
    }

    private static void processObjects(Iterator<MapObject> objs, String map, PrintWriter warpOut, PrintWriter mobOut, TreeSet<Mob> mobs) {
        MapObject mo;
        while (objs.hasNext()) {
            mo = objs.next();
            if (mo == null) continue;
            processObject(mo, map, warpOut, mobOut, mobs);
        }
    }

    private static void processFiles(File folder, List<String> out) {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                processFiles(folder, out);
            } else if (!f.getName().equals(importFile)) {
                out.add("npc: " + f.getPath().substring(_baseFolder.getPath().length() + 1));
            }
        }
    }

    static public String processMap(String name, Map map, PrintWriter summary, HashMap<Integer, Mob> hash) {
        if (name == null) return null;
        if (map == null) return null;

        Properties props = map.getProperties();
        String title = getProp(props, "name", "");
        if (summary != null) {
            summary.printf("\tName: '%s'\n", title);
            summary.printf("\tMusic: '%s'\n", getProp(props, "music", ""));
            summary.printf("\tMinimap: '%s'\n", getProp(props, "minimap", ""));
        }
        String folderName =  scriptDirectory + name;
        if (title.length() > 0) {
            folderName += "_" + title.replaceAll("\\s", "_").replaceAll("[^A-Za-z0-9\\-_]", "");
            title = name + " " + title;
        } else {
            title = name;
        }

        System.out.println(title);

        if (wlk != null) wlk.write(name, map);

        File folder = new File(baseFolder + folderName);
        folder.mkdirs();
        PrintWriter warpOut = Main.getWriter(new File(folder, warpFile));
        PrintWriter mobOut = Main.getWriter(new File(folder, mobFile));

        warpOut.printf("// %s warps\n\n", title);
        mobOut.printf("// %s mobs\n\n", title);

        TreeSet<Mob> mobs = new TreeSet<Mob>();
        processObjects(map.getObjects(), name, warpOut, mobOut, mobs);
        for (MapLayer layer : map) {
            if (layer instanceof ObjectGroup) {
                processObjects(((ObjectGroup) layer).getObjects(), name, warpOut, mobOut, mobs);
            }
        }

        warpOut.flush();
        warpOut.close();

        TreeSet<MobContagem> lCont = new TreeSet<MobContagem>();

		System.out.println("Starting mob points");
		mobOut.printf("\n%s.gat,0,0,0\tscript\tMob%1$s\t-1,{\n\n", name);
		for( Mob mob : mobs ) {
			if( mob.getId()==-1) continue;
			mobOut.printf("On%d:\n\tset @mobID, %d;\n\tcallfunc \"MobPoints\";\n", mob.getId(), mob.getId());
			Mob mob2 = hash.get(mob.getId());
			if(mob2!=null) {
				for( MobContagem mob3 : mob2.getMobContagems() ) {
					mobOut.printf("\tcallsub %s;\n", mob3.getCallsub());
					if(lCont.contains(mob3)==false)
						lCont.add(mob3);
				}
				for( String script : mob2.getScripts() ) {
					mobOut.printf("\t%s\n", script);
				}
			}
			for( String script : mob.getScripts() ) {
				mobOut.printf("\t%s\n", script);
			}
			mobOut.printf("\tbreak;\n\n");
		}
		for( MobContagem cont : lCont ) {
			mobOut.printf("%s:\n", cont.getCallsub());
			mobOut.printf("\tif(%s) return;\n", cont.getReturn());
			mobOut.printf("\tcallfunc \"mobContagem\", %d, %s, %s;\n", cont.getMax(), cont.getVarMobs(), cont.getVarFlag());
			mobOut.printf("\tset %s, @mobs;\n", cont.getVarMobs());
			mobOut.printf("\tset %s, @flag;\n", cont.getVarFlag());
			mobOut.printf("\treturn;\n\n");
		}
		mobOut.printf("}\n");
		System.out.println("Finished mob points");

        mobOut.flush();
        mobOut.close();

        File _import = new File(folder, importFile);
        List<String> output_elements = new ArrayList<String>();
        processFiles(folder, output_elements);
        PrintWriter importOut = Main.getWriter(_import);
        importOut.printf("map: %s.gat\n", name);
        Collections.sort(output_elements);
        for (String s : output_elements)
                importOut.println(s);
        importOut.flush();
        importOut.close();

        return folderName;
    }

    public static void writeMasterImport(String[] folders) {
        File master = new File(baseFolder + scriptDirectory + "_import.txt");
        PrintWriter out = Main.getWriter(master);
        if (out == null) return;

        List<String> output_elements = new ArrayList<String>();

        for (String folder : folders) {
            if (folder == null) continue;
            output_elements.add("import: " + folder + "/_import.txt");
        }

        Collections.sort(output_elements);
        for (String s : output_elements)
                out.println(s);

        out.flush();
        out.close();
    }

}
