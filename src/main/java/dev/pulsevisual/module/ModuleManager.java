package dev.pulsevisual.module;

import dev.pulsevisual.module.impl.*;
import java.util.List;

public final class ModuleManager {
    public static final String[] CATEGORIES = {"General", "View Model", "Swing", "Player", "Trails", "HUD", "Theme", "Fire", "Pearl Tracer"};
    public static final String[] TABS={"Combat","Render","Cosmetic"};
    public static final int[][] GROUPS={{0,2},{1,7,8,5},{3,4,6}};
    private static final List<Module> MODULES = List.of(
            new GeneralModule(), new ViewModelModule(), new SwingModule(), new ChinaHatModule(),
            new TrailModule(), new HudModule(), new ThemeModule(),new FireModule(),new PearlModule());
    private ModuleManager() { }
    public static List<Module> in(String category) { return MODULES.stream().filter(m -> m.category().equals(category)).toList(); }
}
