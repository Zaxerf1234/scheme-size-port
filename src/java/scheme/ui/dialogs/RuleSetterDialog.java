package scheme.ui.dialogs;

import arc.scene.event.FocusListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static arc.Core.*;
import static mindustry.Vars.*;
import scheme.tools.admins.Mindurka;

import static scheme.SchemeVars.admins;

public class RuleSetterDialog extends BaseDialog {

    private String searchText = "";
    private int customTeamId = -1;
    private Table main;

    private void buildWaves(Table t, Rules rules) {
        check(t, "rules.waves", rules.waves, v -> apply("waves", v));
        check(t, "rules.wavesending", rules.waveSending, v -> apply("waveSending", v)).disabled(!rules.waves);
        check(t, "rules.wavetimer", rules.waveTimer, v -> apply("waveTimer", v)).disabled(!rules.waves);
        check(t, "rules.waitForWaveToEnd", rules.waitEnemies, v -> apply("waitEnemies", v)).disabled(!rules.waves || !rules.waveTimer);
        check(t, "rules.randomwaveai", rules.randomWaveAI, v -> apply("randomWaveAI", v)).disabled(!rules.waves);
        check(t, "rules.wavespawnatcores", rules.wavesSpawnAtCores, v -> apply("wavesSpawnAtCores", v)).disabled(!rules.waves);
        check(t, "rules.airUseSpawns", rules.airUseSpawns, v -> apply("airUseSpawns", v)).disabled(!rules.waves);
        integer(t, "rules.wavelimit", rules.winWave, v -> applyInt("winWave", v)).disabled(!rules.waves);
        number(t, "rules.wavespacing", rules.waveSpacing / 60f, v -> applyFloat("waveSpacing", v * 60f)).disabled(!rules.waves || !rules.waveTimer);
        number(t, "rules.initialwavespacing", rules.initialWaveSpacing / 60f, v -> applyFloat("initialWaveSpacing", v * 60f)).disabled(!rules.waves || !rules.waveTimer);
        number(t, "rules.dropzoneradius", rules.dropZoneRadius / tilesize, v -> applyFloat("dropZoneRadius", v * tilesize)).disabled(!rules.waves);
    }

    private void buildResources(Table t, Rules rules) {
        check(t, "rules.alloweditworldprocessors", rules.allowEditWorldProcessors, v -> apply("allowEditWorldProcessors", v));
        check(t, "rules.infiniteresources", rules.infiniteResources, v -> apply("infiniteResources", v));
        check(t, "rules.onlydepositcore", rules.onlyDepositCore, v -> apply("onlyDepositCore", v));
        check(t, "rules.derelictrepair", rules.derelictRepair, v -> apply("derelictRepair", v));
        check(t, "rules.reactorexplosions", rules.reactorExplosions, v -> apply("reactorExplosions", v));
        check(t, "rules.schematic", rules.schematicsAllowed, v -> apply("schematicsAllowed", v));
        check(t, "rules.coreincinerates", rules.coreIncinerates, v -> apply("coreIncinerates", v));
        check(t, "rules.cleanupdeadteams", rules.cleanupDeadTeams, v -> apply("cleanupDeadTeams", v)).disabled(!rules.pvp);
        check(t, "rules.disableworldprocessors", rules.disableWorldProcessors, v -> apply("disableWorldProcessors", v));
        check(t, "rules.ghostblocks", rules.ghostBlocks, v -> apply("ghostBlocks", v));
        check(t, "rules.logicunitcontrol", rules.logicUnitControl, v -> apply("logicUnitControl", v));
        check(t, "rules.logicunitbuild", rules.logicUnitBuild, v -> apply("logicUnitBuild", v));
        check(t, "rules.allowcoreunloaders", rules.allowCoreUnloaders, v -> apply("allowCoreUnloaders", v));
        number(t, "rules.buildcostmultiplier", rules.buildCostMultiplier, v -> applyFloat("buildCostMultiplier", v)).disabled(rules.infiniteResources);
        number(t, "rules.buildspeedmultiplier", rules.buildSpeedMultiplier, v -> applyFloat("buildSpeedMultiplier", v));
        number(t, "rules.deconstructrefundmultiplier", rules.deconstructRefundMultiplier, v -> applyFloat("deconstructRefundMultiplier", v)).disabled(rules.infiniteResources);
        number(t, "rules.blockhealthmultiplier", rules.blockHealthMultiplier, v -> applyFloat("blockHealthMultiplier", v));
        number(t, "rules.blockdamagemultiplier", rules.blockDamageMultiplier, v -> applyFloat("blockDamageMultiplier", v));
        number(t, "rules.objectivetimermultiplier", rules.objectiveTimerMultiplier, v -> applyFloat("objectiveTimerMultiplier", v));
        bannedButton(t, "bannedblocks", content.blocks(), rules.bannedBlocks);
        check(t, "rules.hidebannedblocks", rules.hideBannedBlocks, v -> apply("hideBannedBlocks", v));
        check(t, "bannedblocks.whitelist", rules.blockWhitelist, v -> apply("blockWhitelist", v));
        bannedButton(t, "revealedblocks", content.blocks(), rules.revealedBlocks);
    }

    private void buildUnits(Table t, Rules rules) {
        check(t, "rules.instantbuild", rules.instantBuild, v -> apply("instantBuild", v));
        check(t, "rules.possessionallowed", rules.possessionAllowed, v -> apply("possessionAllowed", v));
        check(t, "rules.unitcapvariable", rules.unitCapVariable, v -> apply("unitCapVariable", v));
        check(t, "rules.unitpayloadsexplode", rules.unitPayloadsExplode, v -> apply("unitPayloadsExplode", v));
        check(t, "rules.unitpayloadupdate", rules.unitPayloadUpdate, v -> apply("unitPayloadUpdate", v));
        check(t, "rules.unitammo", rules.unitAmmo, v -> apply("unitAmmo", v));
        check(t, "rules.disableunitcap", rules.disableUnitCap, v -> apply("disableUnitCap", v));
        integer(t, "rules.unitcap", rules.unitCap, v -> applyInt("unitCap", v)).disabled(rules.disableUnitCap);
        number(t, "rules.unitdamagemultiplier", rules.unitDamageMultiplier, v -> applyFloat("unitDamageMultiplier", v));
        number(t, "rules.unitcrashdamagemultiplier", rules.unitCrashDamageMultiplier, v -> applyFloat("unitCrashDamageMultiplier", v));
        number(t, "rules.unithealthmultiplier", rules.unitHealthMultiplier, v -> applyFloat("unitHealthMultiplier", v));
        number(t, "rules.unitbuildspeedmultiplier", rules.unitBuildSpeedMultiplier, v -> applyFloat("unitBuildSpeedMultiplier", v));
        number(t, "rules.unitcostmultiplier", rules.unitCostMultiplier, v -> applyFloat("unitCostMultiplier", v));
        number(t, "rules.unitminespeedmultiplier", rules.unitMineSpeedMultiplier, v -> applyFloat("unitMineSpeedMultiplier", v));
        bannedButton(t, "bannedunits", content.units(), rules.bannedUnits);
        check(t, "bannedunits.whitelist", rules.unitWhitelist, v -> apply("unitWhitelist", v));
    }

    private void buildEnemy(Table t, Rules rules) {
        check(t, "rules.attack", rules.attackMode, v -> apply("attackMode", v));
        check(t, "rules.corecapture", rules.coreCapture, v -> apply("coreCapture", v));
        check(t, "rules.placerangecheck", rules.placeRangeCheck, v -> apply("placeRangeCheck", v));
        check(t, "rules.polygoncoreprotection", rules.polygonCoreProtection, v -> apply("polygonCoreProtection", v));
        check(t, "rules.coredestroyclear", rules.coreDestroyClear, v -> apply("coreDestroyClear", v));
        number(t, "rules.enemycorebuildradius", rules.enemyCoreBuildRadius / tilesize, v -> applyFloat("enemyCoreBuildRadius", v * tilesize)).disabled(rules.polygonCoreProtection);
    }

    private void buildEnvironment(Table t, Rules rules) {
        check(t, "rules.explosions", rules.damageExplosions, v -> apply("damageExplosions", v));
        check(t, "rules.fire", rules.fire, v -> apply("fire", v));
        check(t, "rules.fog", rules.fog, v -> apply("fog", v));
        check(t, "rules.staticfog", rules.staticFog, v -> apply("staticFog", v)).disabled(!rules.fog);
        check(t, "rules.lighting", rules.lighting, v -> apply("lighting", v));
        check(t, "rules.borderdarkness", rules.borderDarkness, v -> apply("borderDarkness", v));
        check(t, "rules.showspawns", rules.showSpawns, v -> apply("showSpawns", v));
        check(t, "rules.limitarea", rules.limitMapArea, v -> apply("limitMapArea", v));
        integer(t, "rules.limitx", rules.limitX, v -> applyInt("limitX", v)).disabled(!rules.limitMapArea);
        integer(t, "rules.limity", rules.limitY, v -> applyInt("limitY", v)).disabled(!rules.limitMapArea);
        integer(t, "rules.limitwidth", rules.limitWidth, v -> applyInt("limitWidth", v)).disabled(!rules.limitMapArea);
        integer(t, "rules.limitheight", rules.limitHeight, v -> applyInt("limitHeight", v)).disabled(!rules.limitMapArea);
        check(t, "rules.disableoutsidearea", rules.disableOutsideArea, v -> apply("disableOutsideArea", v)).disabled(!rules.limitMapArea);
        number(t, "rules.solarmultiplier", rules.solarMultiplier, v -> applyFloat("solarMultiplier", v));
        number(t, "rules.dragmultiplier", rules.dragMultiplier, v -> applyFloat("dragMultiplier", v));
    }

    private void buildTeams(Table t, Rules rules) {
        if (admins instanceof Mindurka) return;
        t.table(row -> {
            row.left();
            row.add("Team ID (0-255):").left().padRight(5);
            TextField teamField = row.field(customTeamId >= 0 ? String.valueOf(customTeamId) : "", s -> {}).width(80f).valid(s -> {
                if (s.isEmpty()) return true;
                int v = Strings.parseInt(s, -1);
                return v >= 0 && v <= 255;
            }).left().get();
            teamField.addListener(new FocusListener() {
                @Override
                public void keyboardFocusChanged(FocusListener.FocusEvent event, arc.scene.Element element, boolean focused) {
                    if (!focused) {
                        int id = Strings.parseInt(teamField.getText(), -1);
                        customTeamId = (id >= 0 && id <= 255) ? id : -1;
                        rebuild();
                    }
                }
            });
        }).pad(6).left().fillX();
        t.row();

        if (customTeamId >= 0) {
            Team team = Team.all[customTeamId];
            Rules.TeamRule tr = rules.teams.get(team);
            t.add("[#" + team.color + "]#" + customTeamId + " " + team.localized() + "[]").color(team.color).pad(6).padTop(12).left().row();
            t.image().color(team.color).height(2f).pad(2).padLeft(10).padRight(10).fillX().row();
            buildTeamSection(t, team, tr, rules);
        }

        for (Team team : Team.baseTeams) {
            if (team.data() == null) continue;
            if (team.data().cores.size == 0 && team != rules.defaultTeam && team != rules.waveTeam) continue;
            if (team.id == customTeamId) continue;

            Rules.TeamRule tr = rules.teams.get(team);
            t.add("[#" + team.color + "]" + team.localized() + "[]").color(team.color).pad(6).padTop(12).left().row();
            t.image().color(team.color).height(2f).pad(2).padLeft(10).padRight(10).fillX().row();
            buildTeamSection(t, team, tr, rules);
        }
    }

    private void buildTeamSection(Table t, Team team, Rules.TeamRule tr, Rules rules) {
        int tid = team.id;
        check(t, "rules.infiniteammo", tr.infiniteAmmo, v -> applyTeam(tid, "infiniteAmmo", v));
        check(t, "rules.cheat", tr.cheat, v -> applyTeam(tid, "cheat", v));
        check(t, "rules.fillitems", tr.fillItems, v -> applyTeam(tid, "fillItems", v));
        check(t, "rules.infiniteresources", tr.infiniteResources, v -> applyTeam(tid, "infiniteResources", v));
        check(t, "rules.rtsai", tr.rtsAi, v -> { applyTeam(tid, "rtsAi", v); rebuild(); }).disabled(team == rules.defaultTeam);
        integer(t, "rules.rtsminsquadsize", tr.rtsMinSquad, v -> applyTeamInt(tid, "rtsMinSquad", v)).disabled(team == rules.defaultTeam || !tr.rtsAi);
        integer(t, "rules.rtsmaxsquadsize", tr.rtsMaxSquad, v -> applyTeamInt(tid, "rtsMaxSquad", v)).disabled(team == rules.defaultTeam || !tr.rtsAi);
        number(t, "rules.rtsminattackweight", tr.rtsMinWeight, v -> applyTeamFloat(tid, "rtsMinWeight", v)).disabled(team == rules.defaultTeam || !tr.rtsAi);
        check(t, "rules.buildai", tr.buildAi, v -> { applyTeam(tid, "buildAi", v); rebuild(); });
        number(t, "rules.buildaitier", tr.buildAiTier, v -> applyTeamFloat(tid, "buildAiTier", v)).disabled(!tr.buildAi);
        number(t, "rules.blockhealthmultiplier", tr.blockHealthMultiplier, v -> applyTeamFloat(tid, "blockHealthMultiplier", v));
        number(t, "rules.blockdamagemultiplier", tr.blockDamageMultiplier, v -> applyTeamFloat(tid, "blockDamageMultiplier", v));
        number(t, "rules.buildspeedmultiplier", tr.buildSpeedMultiplier, v -> applyTeamFloat(tid, "buildSpeedMultiplier", v));
        number(t, "rules.unitdamagemultiplier", tr.unitDamageMultiplier, v -> applyTeamFloat(tid, "unitDamageMultiplier", v));
        number(t, "rules.unitcrashdamagemultiplier", tr.unitCrashDamageMultiplier, v -> applyTeamFloat(tid, "unitCrashDamageMultiplier", v));
        number(t, "rules.unithealthmultiplier", tr.unitHealthMultiplier, v -> applyTeamFloat(tid, "unitHealthMultiplier", v));
        number(t, "rules.unitbuildspeedmultiplier", tr.unitBuildSpeedMultiplier, v -> applyTeamFloat(tid, "unitBuildSpeedMultiplier", v));
        number(t, "rules.unitcostmultiplier", tr.unitCostMultiplier, v -> applyTeamFloat(tid, "unitCostMultiplier", v));
        number(t, "rules.unitminespeedmultiplier", tr.unitMineSpeedMultiplier, v -> applyTeamFloat(tid, "unitMineSpeedMultiplier", v));
        number(t, "rules.extracorebuildradius", tr.extraCoreBuildRadius / tilesize, v -> applyTeamFloat(tid, "extraCoreBuildRadius", v * tilesize)).disabled(!rules.polygonCoreProtection);
    }

    private void buildAdvanced(Table t, Rules rules) {
        check(t, "rules.cangameover", rules.canGameOver, v -> apply("canGameOver", v));
        check(t, "rules.editor", rules.editor, v -> apply("editor", v));
        check(t, "rules.pvp", rules.pvp, v -> apply("pvp", v));
        check(t, "rules.pvpautopause", rules.pvpAutoPause, v -> apply("pvpAutoPause", v)).disabled(!rules.pvp);
        check(t, "rules.pausedisabled", rules.pauseDisabled, v -> apply("pauseDisabled", v));
        check(t, "rules.alloweditrules", rules.allowEditRules, v -> apply("allowEditRules", v));
        check(t, "rules.allowenvironmentdeconstruct", rules.allowEnvironmentDeconstruct, v -> apply("allowEnvironmentDeconstruct", v));
        check(t, "rules.allowlogicdata", rules.allowLogicData, v -> apply("allowLogicData", v));
        number(t, "rules.itemdepositcooldown", rules.itemDepositCooldown, v -> applyFloat("itemDepositCooldown", v));
    }

    public RuleSetterDialog() {
        super("Rule Setter");
        addCloseButton();
        hidden(() -> {
            if (scene.getKeyboardFocus() != null) scene.setKeyboardFocus(null);
        });
    }

    @Override
    public Dialog show() {
        super.show();
        cont.clear();

        cont.table(t -> {
            t.add("@search").padRight(10);
            TextField field = t.field(searchText, text -> {
                searchText = text.trim().toLowerCase();
                rebuild();
            }).width(200f).pad(8).get();
            field.setCursorPosition(searchText.length());
            scene.setKeyboardFocus(field);
            t.button(Icon.cancel, Styles.emptyi, () -> {
                searchText = "";
                rebuild();
            }).padLeft(10f).size(35f);
        }).fillX().row();

        cont.pane(m -> main = m).left().grow();
        rebuild();

        return this;
    }

    private void rebuild() {
        main.clear();
        main.top().defaults().pad(4).left();

        Rules rules = Vars.state.rules;

        category("rules.title.waves", t -> buildWaves(t, rules));
        category("rules.title.resourcesbuilding", t -> buildResources(t, rules));
        category("rules.title.unit", t -> buildUnits(t, rules));
        category("rules.title.enemy", t -> buildEnemy(t, rules));
        category("rules.title.environment", t -> buildEnvironment(t, rules));
        category("rules.title.teams", t -> buildTeams(t, rules));
        category("rules.title.advanced", t -> buildAdvanced(t, rules));
    }

    private boolean matchesSearch(String key) {
        if (searchText.isEmpty()) return true;
        String text = bundle.has(key) ? bundle.get(key).toLowerCase() : key.toLowerCase();
        return text.contains(searchText) || key.toLowerCase().contains(searchText);
    }

    private void category(String titleKey, arc.func.Cons<Table> builder) {
        Table inner = new Table();
        inner.left().defaults().left().pad(2);
        builder.get(inner);

        if (inner.getChildren().size == 0) return;

        String title = bundle.has(titleKey) ? bundle.get(titleKey) : titleKey;
        main.add(title).color(Pal.accent).pad(6).padLeft(10).fillX().left().row();
        main.image().color(Pal.accent).height(3f).pad(6).padLeft(10).padRight(10).fillX().row();
        main.add(inner).fillX().padLeft(10).row();
    }

    private FieldCell check(Table t, String key, boolean value, arc.func.Boolc onChange) {
        if (!matchesSearch(key)) return FieldCell.EMPTY;

        String label = bundle.has(key) ? bundle.get(key) : key;
        Cell<CheckBox> cell = t.check(label, value, val -> {
            onChange.get(val);
        }).pad(6).left();
        t.row();
        return new FieldCell(cell);
    }

    private FieldCell number(Table t, String key, float value, arc.func.Floatc onChange) {
        if (!matchesSearch(key)) return FieldCell.EMPTY;

        String label = bundle.has(key) ? bundle.get(key) : key;
        Cell<Table> cell = t.table(row -> {
            row.left();
            row.add(label).left().padRight(5);
            TextField field = row.field(Strings.autoFixed(value, 2), s -> {}).width(120f).valid(Strings::canParseFloat).left().get();
            field.addListener(new FocusListener() {
                @Override
                public void keyboardFocusChanged(FocusListener.FocusEvent event, arc.scene.Element element, boolean focused) {
                    if (!focused && field.isValid()) {
                        float f = Strings.parseFloat(field.getText(), value);
                        onChange.get(f);
                    }
                }
            });
        }).pad(6).left().fillX();
        t.row();
        return new FieldCell(cell);
    }

    private FieldCell integer(Table t, String key, int value, arc.func.Intc onChange) {
        if (!matchesSearch(key)) return FieldCell.EMPTY;

        String label = bundle.has(key) ? bundle.get(key) : key;
        Cell<Table> cell = t.table(row -> {
            row.left();
            row.add(label).left().padRight(5);
            TextField field = row.field(String.valueOf(value), s -> {}).width(120f).valid(Strings::canParseInt).left().get();
            field.addListener(new FocusListener() {
                @Override
                public void keyboardFocusChanged(FocusListener.FocusEvent event, arc.scene.Element element, boolean focused) {
                    if (!focused && field.isValid()) {
                        int i = Strings.parseInt(field.getText(), value);
                        onChange.get(i);
                    }
                }
            });
        }).pad(6).left().fillX();
        t.row();
        return new FieldCell(cell);
    }

    private <T extends UnlockableContent> void bannedButton(Table t, String key, Seq<T> allContent, ObjectSet<T> banned) {
        if (!matchesSearch(key)) return;

        String label = bundle.has(key) ? bundle.get(key) : key;
        int columns = mobile ? 5 : 10;
        float iconSize = 48f;

        t.button(label + " [accent](" + banned.size + ")[]", () -> {
            BaseDialog dialog = new BaseDialog(label);
            dialog.addCloseButton();
            dialog.cont.pane(pane -> {
                pane.margin(0f, 24f, 0f, 24f);
                int[] count = {0};
                allContent.each(item -> {
                    ImageButton btn = pane.button(new TextureRegionDrawable(item.uiIcon), Styles.clearTogglei, iconSize, () -> {
                        if (banned.contains(item)) banned.remove(item);
                        else banned.add(item);
                        syncRules();
                    }).size(iconSize + 8f).tooltip(item.localizedName).update(b -> b.setChecked(banned.contains(item))).get();

                    if (++count[0] % columns == 0) pane.row();
                });
            }).grow();
            dialog.show();
        }).left().width(300f).pad(6);
        t.row();
    }

    private void apply(String field, boolean value) {
        admins.manageRuleBool(value, field);
        rebuild();
    }

    private void applyFloat(String field, float value) {
        admins.manageRuleStr(String.valueOf(value), field);
    }

    private void applyInt(String field, int value) {
        admins.manageRuleStr(String.valueOf(value), field);
    }

    private void applyTeam(int teamId, String field, boolean value) {
        admins.manageTeamRuleBool(teamId, value, field);
        rebuild();
    }

    private void applyTeamFloat(int teamId, String field, float value) {
        admins.manageTeamRuleStr(teamId, String.valueOf(value), field);
    }

    private void applyTeamInt(int teamId, String field, int value) {
        admins.manageTeamRuleStr(teamId, String.valueOf(value), field);
    }

    private void syncRules() {
        try {
            Call.setRules(Vars.state.rules);
        } catch (Exception ignored) {}
    }

    private static class FieldCell {
        static final FieldCell EMPTY = new FieldCell(null);
        private final Cell<?> cell;
        FieldCell(Cell<?> cell) { this.cell = cell; }
        FieldCell disabled(boolean disabled) {
            if (cell == null) return this;
            arc.scene.Element elem = cell.get();
            if (elem == null) return this;
            elem.touchable = disabled ? arc.scene.event.Touchable.disabled : arc.scene.event.Touchable.enabled;
            elem.color.a(disabled ? 0.5f : 1f);
            return this;
        }
    }
}
